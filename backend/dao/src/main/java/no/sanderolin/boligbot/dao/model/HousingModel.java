package no.sanderolin.boligbot.dao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "housings")
public class HousingModel extends ImportableEntity{

    @Id
    @Column(name = "rental_object_id", nullable = false, unique = true)
    private String rentalObjectId;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String name;

    @Column(name = "housing_type", nullable = false)
    private String housingType;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String district;

    @Column(name = "area_sqm", precision = 6, scale = 2, nullable = false)
    private BigDecimal areaSqm;

    @Column(name = "price_per_month", nullable = false)
    private int pricePerMonth;

    @Column(name = "is_available")
    private boolean isAvailable = false;

    @Column(name = "available_from_date")
    private LocalDate availableFromDate;

    /**
     * Compares only the business data fields (excludes audit fields like timestamps).
     * Used to determine if an entity has changed during import.
     */
    public boolean dataEquals(HousingModel other) {
        if (other == null || getClass() != other.getClass()) return false;

        return Objects.equals(this.name, other.name)
               && Objects.equals(this.address, other.address)
               && Objects.equals(this.housingType, other.housingType)
               && Objects.equals(this.city, other.city)
               && Objects.equals(this.district, other.district)
               && areaSqmEquals(this.areaSqm, other.areaSqm)
               && this.pricePerMonth == other.pricePerMonth
               && this.isAvailable == other.isAvailable
               && Objects.equals(this.availableFromDate, other.availableFromDate);
    }

    private boolean areaSqmEquals(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.compareTo(b) == 0;
    }
}