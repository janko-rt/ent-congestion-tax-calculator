package hr.janko.ent.ctc.service.impl;

import hr.janko.ent.ctc.data.model.CityModel;
import hr.janko.ent.ctc.data.model.TaxModel;
import hr.janko.ent.ctc.data.model.TaxStatus;
import hr.janko.ent.ctc.data.model.TaxTimetableModel;
import hr.janko.ent.ctc.generated.model.TaxRequestDto;
import hr.janko.ent.ctc.service.CityService;
import hr.janko.ent.ctc.service.TaxProcessingService;
import hr.janko.ent.ctc.service.TaxService;
import hr.janko.ent.ctc.service.TaxTimetableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaxProcessingServiceImpl implements TaxProcessingService {

    private final TaxService taxService;
    private final TaxTimetableService taxTimetableService;
    private final CityService cityService;

    @Override
    public TaxModel processIncomingTaxRequest(TaxRequestDto taxRequestDto) {
        //This might benefit from locks in case it is possible that there are 2 request done at the same time.
        // On multi pod cluster a JDBCLockRegistry would be a good solution. Out of scope for this

        TaxModel taxModel = saveInitialTaxModel(taxRequestDto);


        Optional<String> errorMessageOptional = validateRequest(taxRequestDto);//Simple validation, in theory it could be entire validation and maybe a list of validation failures

        if (errorMessageOptional.isPresent()) {
            String errorMessage = errorMessageOptional.get();
            log.error("Error validating: {} for uuid {}", errorMessage, taxModel.getUuid());
            taxModel.setTaxStatus(TaxStatus.ERROR);
            taxModel.setErrorMessage(errorMessage);
            return taxService.saveTax(taxModel);
        }

        try {//This could use more polish with time
            Optional<CityModel> cityModel = validateAndEnrichWithCityData(taxRequestDto, taxModel);
            if (cityModel.isEmpty()) {
                return taxModel;
            }

            validateTaxTablesAndEnrichWithAmount(taxModel, cityModel.get());
            if (taxModel.getTaxStatus() != TaxStatus.NEW) {
                return taxModel;
            }

            int dayOfTheWeek = taxModel.getRecordedDate().getDayOfWeek().getValue();
            if (dayOfTheWeek > 5) {
                log.debug("Taxes are not charged during the weekend, ignoring");
                taxModel.setTaxStatus(TaxStatus.IGNORED);
                taxModel.setErrorMessage("Weekend");
                return taxModel;
            }

            taxModel = taxService.validateSingleChargeRule(taxModel);
            if (taxModel.getTaxStatus() != TaxStatus.NEW) {
                return taxModel;
            }

            taxModel = taxService.checkMaximumLimitForDayIsNotReached(taxModel);

            if (taxModel.getTaxStatus() == TaxStatus.NEW) {
                taxModel.setTaxStatus(TaxStatus.DUE);
            }

        } catch (Exception e) {
            log.error("Error occurred while processing uuid {}", taxModel.getUuid(), e);
            taxModel.setTaxStatus(TaxStatus.ERROR);
            taxModel.setErrorMessage("Error occurred while processing " + e.getMessage());
        } finally {
            taxService.saveTax(taxModel);
        }

        return taxModel;
    }

    private void validateTaxTablesAndEnrichWithAmount(TaxModel taxModel, CityModel cityModel) {
        LocalTime recordedTime = taxModel.getRecordedDate().toLocalTime();
        List<TaxTimetableModel> taxTimeTables = cityModel.getTaxTimetables()
                .stream()
                .filter(taxTimetableModel -> recordedTime.isAfter(taxTimetableModel.getStartTime()) &&
                        recordedTime.isBefore(taxTimetableModel.getEndTime()))
                .toList();

        if (taxTimeTables.size() != 1) {
            log.error("Tax timetable list for uuid {} does not contain exactly one record, instead it contains {}", taxModel.getUuid(), taxTimeTables.size());
            taxModel.setTaxStatus(TaxStatus.ERROR);
            taxModel.setErrorMessage("Incorrect number of tax tables");
            return;
        }

        TaxTimetableModel taxTimetableModel = taxTimeTables.get(0);
        taxModel.setAmount(taxTimetableModel.getAmount());
        taxModel.setTaxTimetableId(taxTimetableModel.getId());
    }

    private Optional<CityModel> validateAndEnrichWithCityData(TaxRequestDto taxRequestDto, TaxModel taxModel) {
        Optional<CityModel> cityOptional = cityService.fetchById(taxRequestDto.getCityId().longValue());
        if (cityOptional.isEmpty()) {
            log.error("City not found for id {} for uuid {}", taxRequestDto.getCityId(), taxModel.getUuid());
            taxModel.setTaxStatus(TaxStatus.ERROR);
            taxModel.setErrorMessage("City not found");
            return Optional.empty();
        }
        CityModel cityModel = cityOptional.get();
        taxModel.setCurrency(cityModel.getCurrency());
        return cityOptional;
    }

    private Optional<String> validateRequest(TaxRequestDto taxRequestDto) {
        if (taxRequestDto.getRegistrationPlateNumber() == null) {
            return Optional.of("Registration plate number missing");
        }
        if (taxRequestDto.getCityId() == null) {
            return Optional.of("City id missing");
        }
        if (taxRequestDto.getTimestamp() == null) {
            return Optional.of("Timestamp missing");
        }
        if (taxRequestDto.getTimestamp().getYear() != 2013) {
            return Optional.of("Timestamp is not in 2013");
        }
        return Optional.empty();
    }

    private TaxModel saveInitialTaxModel(TaxRequestDto taxRequestDto) {
        TaxModel taxModel = TaxModel.builder()
                .taxStatus(TaxStatus.NEW)
                .recordedDate(taxRequestDto.getTimestamp())
                .registrationPlateNumber(taxRequestDto.getRegistrationPlateNumber())
                .build();

        String uuid = UUID.randomUUID().toString();
        taxModel.setUuid(uuid);

        return taxService.saveTax(taxModel);
    }
}
