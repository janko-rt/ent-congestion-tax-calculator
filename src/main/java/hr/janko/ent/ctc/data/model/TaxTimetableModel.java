package hr.janko.ent.ctc.data.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Entity(name = "tax_timetable")
@SequenceGenerator(name = "tax_timetable_generator", sequenceName = "tax_timetable_seq", allocationSize = 1)
public class TaxTimetableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tax_timetable_generator")
    private Long id;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "city_id")
    private Long cityId;

    @ManyToOne
    @JoinColumn(name = "city_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_city", value = ConstraintMode.PROVIDER_DEFAULT), insertable = false, updatable = false)
    private CityModel cityModel;
}
