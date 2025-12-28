package no.sanderolin.boligbot.web.v1.housing.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HousingDTO (
        String rentalObjectId,
        String address,
        String name,
        String housingType,
        String city,
        String district,
        BigDecimal areaSqm,
        int pricePerMonth,
        boolean isAvailable,
        LocalDate availableFromDate)
{}
