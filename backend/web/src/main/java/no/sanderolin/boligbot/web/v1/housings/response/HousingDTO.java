package no.sanderolin.boligbot.web.v1.housings.response;

import no.sanderolin.boligbot.dao.model.HousingModel;

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
        LocalDate availableFromDate) {

    public static HousingDTO createFromModel(HousingModel model) {
        return new HousingDTO(
                model.getRentalObjectId(),
                model.getAddress(),
                model.getName(),
                model.getHousingType(),
                model.getCity(),
                model.getDistrict(),
                model.getAreaSqm(),
                model.getPricePerMonth(),
                model.isAvailable(),
                model.getAvailableFromDate()
        );
    }
}
