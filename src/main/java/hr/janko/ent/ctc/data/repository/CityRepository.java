package hr.janko.ent.ctc.data.repository;

import hr.janko.ent.ctc.data.model.CityModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<CityModel, Long> {
}
