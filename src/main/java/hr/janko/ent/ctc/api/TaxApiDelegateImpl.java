package hr.janko.ent.ctc.api;

import hr.janko.ent.ctc.generated.api.TaxApiDelegate;
import hr.janko.ent.ctc.generated.model.ResponseDto;
import hr.janko.ent.ctc.generated.model.TaxRequestDto;
import hr.janko.ent.ctc.service.TaxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaxApiDelegateImpl implements TaxApiDelegate {

    private final TaxService taxService;

    @Override
    public ResponseEntity<ResponseDto> apiV1TaxTaxIncomingVehiclePost(TaxRequestDto taxRequestDto) {
        taxService.saveTax();
        return ResponseEntity.ok().build();
    }
}
