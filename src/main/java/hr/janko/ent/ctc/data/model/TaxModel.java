package hr.janko.ent.ctc.data.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Entity(name = "tax")
@SequenceGenerator(name = "tax_generator", sequenceName = "tax_seq", allocationSize = 1)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxModel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tax_generator")
    private Long id;

    @Column(name = "registration_plate_number")
    private String registrationPlateNumber;

    @Column(name = "recorded_date")
    private LocalDateTime recordedDate;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "currency")
    private String currency;//In case there is ever a currency switch it is good to have record of which currency was charged

    @Column(name = "tax_status")
    @Enumerated(EnumType.STRING)
    private TaxStatus taxStatus;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "tax_timetable_id")
    private Long taxTimetableId;
}
