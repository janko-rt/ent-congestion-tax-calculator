package hr.janko.ent.ctc.data;

import hr.janko.ent.ctc.data.model.TaxTimetableModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxRuleRepository extends JpaRepository<TaxTimetableModel, Long> {
}
