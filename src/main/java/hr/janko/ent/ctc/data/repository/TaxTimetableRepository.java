package hr.janko.ent.ctc.data.repository;

import hr.janko.ent.ctc.data.model.TaxTimetableModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxTimetableRepository extends JpaRepository<TaxTimetableModel, Long> {
}
