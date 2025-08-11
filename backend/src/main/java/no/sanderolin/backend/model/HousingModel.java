package no.sanderolin.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "housing")
public class HousingModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;

    private String name;

    @Column(name = "unit_number")
    private String unitNumber;

    @Column(name = "housing_type")
    private String housingType;

    private String city;

    private String district;

    @Column(name = "area_sqm")
    private BigDecimal areaSqm;

    @Column(name = "price_per_month")
    private int pricePerMonth;

    @Column(name = "available_status")
    private String availableStatus;

    @Column(name = "available_from_date")
    private LocalDate availableFromDate;
}
