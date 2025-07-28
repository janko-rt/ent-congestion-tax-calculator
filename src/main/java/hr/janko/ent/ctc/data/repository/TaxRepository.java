package hr.janko.ent.ctc.data.repository;

import hr.janko.ent.ctc.data.model.TaxModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxRepository extends JpaRepository<TaxModel, Long> {
}
