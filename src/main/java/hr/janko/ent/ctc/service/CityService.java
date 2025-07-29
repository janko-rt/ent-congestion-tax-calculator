package hr.janko.ent.ctc.service;

import hr.janko.ent.ctc.data.model.CityModel;
import hr.janko.ent.ctc.data.model.TaxModel;

import java.util.Optional;

public interface CityService {

    Optional<CityModel> fetchById(Long id);

}
