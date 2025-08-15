package no.sanderolin.boligbot.dao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "housing")
public class HousingModel {

    @Id
    @Column(name = "rental_object_id", nullable = false, unique = true)
    private String rentalObjectId;

    private String address;

    private String name;

    @Column(name = "unit_number")
    private String unitNumber;

    @Column(name = "housing_type")
    private String housingType;

    private String city;

    private String district;

    @Column(name = "area_sqm", precision = 6, scale = 2)
    private BigDecimal areaSqm;

    @Column(name = "price_per_month")
    private int pricePerMonth;

    @Column(name = "is_available")
    private boolean isAvailable;

    @Column(name = "available_from_date")
    private LocalDate availableFromDate;
}