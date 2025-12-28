package no.sanderolin.boligbot.web.v1.housing.mapper;

import no.sanderolin.boligbot.dao.model.HousingModel;
import no.sanderolin.boligbot.web.v1.housing.response.HousingDTO;

public class HousingModelToDTOMapper {

    public static HousingDTO toDTO(HousingModel model) {
        return new HousingDTO(
                model.getRentalObjectId(),
                model.getAddress(),
                model.getName(),
                model.getHousingType().getName(),
                model.getDistrict().getCity().getName(),
                model.getDistrict().getName(),
                model.getAreaSqm(),
                model.getPricePerMonth(),
                model.isAvailable(),
                model.getAvailableFromDate()
        );
    }
}
