package hr.janko.ent.ctc.service;

import hr.janko.ent.ctc.data.model.TaxModel;

public interface TaxService {

    TaxModel saveTax(TaxModel taxModel);

    TaxModel validateSingleChargeRule(TaxModel taxModel);

    TaxModel checkMaximumLimitForDayIsNotReached(TaxModel taxModel);
}
