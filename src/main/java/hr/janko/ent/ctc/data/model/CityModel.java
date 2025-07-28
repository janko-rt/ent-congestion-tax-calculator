package hr.janko.ent.ctc.data.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity(name = "city")
@SequenceGenerator(name = "city_generator", sequenceName = "city_seq", allocationSize = 1)
public class CityModel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "city_generator")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "currency")
    private String currency;//This should probably be on level of country, however I feel for this assignment this should be fine

    @Column(name = "zone_id")
    private String zoneId;

    @OneToMany(mappedBy = "cityModel")
    private List<TaxTimetableModel> taxTimetables;//These should maybe be "city" level time tables, maybe there are some general rules for whole districts/states or the country
}
