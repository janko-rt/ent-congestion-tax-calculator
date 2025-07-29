package hr.janko.ent.ctc.service;

import hr.janko.ent.ctc.data.model.TaxModel;
import hr.janko.ent.ctc.generated.model.TaxRequestDto;
import org.springframework.http.ResponseEntity;

public interface TaxProcessingService {

    TaxModel processIncomingTaxRequest(TaxRequestDto taxRequestDto);
}
