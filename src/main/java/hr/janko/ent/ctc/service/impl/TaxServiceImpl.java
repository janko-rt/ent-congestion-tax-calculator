package hr.janko.ent.ctc.service.impl;

import hr.janko.ent.ctc.data.model.TaxModel;
import hr.janko.ent.ctc.data.model.TaxStatus;
import hr.janko.ent.ctc.data.repository.TaxRepository;
import hr.janko.ent.ctc.service.TaxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaxServiceImpl implements TaxService {

    private final List<String> VALID_STATUSES_FOR_TAX_VALIDATION = List.of(TaxStatus.DUE.name(), TaxStatus.NEW.name());
    private final BigDecimal MAXIMUM_DAILY_TAX_SUM = new BigDecimal("60.00");
    private final TaxRepository taxRepository;

    @Override
    public TaxModel saveTax(TaxModel taxModel) {
        return taxRepository.save(taxModel);
    }

    @Override
    @Transactional
    public TaxModel validateSingleChargeRule(TaxModel taxModel) {
        LocalDateTime start = taxModel.getRecordedDate().minusHours(1L);

        //This doesn't make sense to do in DB since even if people drive around a lot it's highly unlikely that just doing these filters through stream() would cause problems, but this is more of a showcase
        List<TaxModel> taxesInTheLastHour = taxRepository.fetchValidTaxesInLastHourForPlateNumber(taxModel.getRegistrationPlateNumber(), start, taxModel.getRecordedDate(), VALID_STATUSES_FOR_TAX_VALIDATION);

        TaxModel highestAmountTax = taxesInTheLastHour.stream().max(Comparator.comparing(TaxModel::getAmount)).get();

        if (highestAmountTax.getTaxStatus() == TaxStatus.NEW) {
            taxesInTheLastHour.stream().filter(tax -> tax.getTaxStatus() != TaxStatus.NEW).forEach(tax -> tax.setTaxStatus(TaxStatus.CANCELED));
            taxRepository.saveAll(taxesInTheLastHour);
        } else {
            taxModel.setTaxStatus(TaxStatus.CANCELED);
            taxRepository.save(taxModel);
        }

        return taxModel;
    }

    @Override
    public TaxModel checkMaximumLimitForDayIsNotReached(TaxModel taxModel) {
        LocalDateTime startOfDay = taxModel.getRecordedDate().toLocalDate().atStartOfDay();

        List<TaxModel> taxesInTheLastDay = taxRepository.fetchValidTaxesInLastHourForPlateNumber(taxModel.getRegistrationPlateNumber(), startOfDay, taxModel.getRecordedDate(), VALID_STATUSES_FOR_TAX_VALIDATION);

        BigDecimal sumForToday = taxesInTheLastDay.stream().map(TaxModel::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if(MAXIMUM_DAILY_TAX_SUM.compareTo(sumForToday) < 0){
            taxModel.setTaxStatus(TaxStatus.CANCELED);//Perhaps we'd want to lower the amount here, however it might be correct to add another column to preserve originally requested amount
            taxModel.setErrorMessage("Total sum of taxes would be exceeded");
        }

        return taxModel;
    }
}
