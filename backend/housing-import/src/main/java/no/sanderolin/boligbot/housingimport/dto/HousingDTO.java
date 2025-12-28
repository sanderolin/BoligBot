package no.sanderolin.boligbot.housingimport.dto;

import java.math.BigDecimal;

public record HousingDTO (
        String rentalObjectId,
        String name,
        String address,
        String housingTypeName,
        String cityName,
        String districtName,
        BigDecimal areaSqm,
        int pricePerMonth
) {
}
