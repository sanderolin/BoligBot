package no.sanderolin.boligbot.dao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "housing")
public class HousingModel extends ImportableEntity{

    @Id
    @Column(name = "rental_object_id", nullable = false, unique = true)
    private String rentalObjectId;

    private String address;

    private String name;

    @Column(name = "housing_type")
    private String housingType;

    private String city;

    private String district;

    @Column(name = "area_sqm", precision = 6, scale = 2)
    private BigDecimal areaSqm;

    @Column(name = "price_per_month")
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