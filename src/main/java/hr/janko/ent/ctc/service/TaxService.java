package hr.janko.ent.ctc.service;

import hr.janko.ent.ctc.data.model.TaxModel;
import hr.janko.ent.ctc.generated.model.TaxRequestDto;

public interface TaxService {

    TaxModel saveTax(TaxModel taxModel);

    TaxModel checkForPlateNumberLastHour(TaxModel taxModel);

    TaxModel checkMaximumLimitForDayIsNotReached(TaxModel taxModel);
}
