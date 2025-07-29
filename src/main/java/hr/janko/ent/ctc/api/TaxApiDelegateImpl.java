package hr.janko.ent.ctc.api;

import hr.janko.ent.ctc.data.model.TaxModel;
import hr.janko.ent.ctc.data.model.TaxStatus;
import hr.janko.ent.ctc.generated.api.TaxApiDelegate;
import hr.janko.ent.ctc.generated.model.ErrorDto;
import hr.janko.ent.ctc.generated.model.TaxRequestDto;
import hr.janko.ent.ctc.service.TaxProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaxApiDelegateImpl implements TaxApiDelegate {

    private final TaxProcessingService taxProcessingService;

    @Override
    public ResponseEntity apiV1TaxTaxIncomingVehiclePost(TaxRequestDto taxRequestDto) {
        log.debug("Processing for plate {} and city id {} at time: {}", taxRequestDto.getRegistrationPlateNumber(), taxRequestDto.getCityId(), taxRequestDto.getTimestamp());
        TaxModel result = taxProcessingService.processIncomingTaxRequest(taxRequestDto);

        if (result.getTaxStatus() == TaxStatus.ERROR) {
            ErrorDto errorDto = new ErrorDto();
            errorDto.setCode("500");
            errorDto.setMessage(result.getErrorMessage());
            return ResponseEntity.internalServerError().body(errorDto);
        }

        return ResponseEntity.ok().build();
    }
}
