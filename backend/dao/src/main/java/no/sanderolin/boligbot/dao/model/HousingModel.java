package no.sanderolin.boligbot.dao.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "housings")
public class HousingModel extends ImportableEntity {

    @Id
    @Column(name = "rental_object_id", nullable = false, unique = true, updatable = false)
    private String rentalObjectId;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "housing_type_id", nullable = false)
    private HousingTypeModel housingType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "district_id", nullable = false)
    private DistrictModel district;

    @Column(name = "area_sqm", precision = 6, scale = 2, nullable = false)
    private BigDecimal areaSqm;

    @Column(name = "price_per_month", nullable = false)
    private int pricePerMonth;

    @Column(name = "is_available")
    private boolean isAvailable = false;

    @Column(name = "available_from_date")
    private LocalDate availableFromDate;

    @Transient
    public CityModel getCity() {
        return district == null ? null : district.getCity();
    }
}