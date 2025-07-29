package hr.janko.ent.ctc.data.repository;

import hr.janko.ent.ctc.data.model.TaxModel;
import hr.janko.ent.ctc.data.model.TaxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface TaxRepository extends JpaRepository<TaxModel, Long> {

    @Query(nativeQuery = true, value = """
            SELECT * FROM tax t
            WHERE t.recorded_date <= :end AND t.recorded_date >= :start AND t.registration_plate_number = :registrationPlateNumber AND t.tax_status in :expectedStatuses
            """)
    List<TaxModel> fetchValidTaxesInLastHourForPlateNumber(String registrationPlateNumber, LocalDateTime start, LocalDateTime end, List<String> expectedStatuses);
}
