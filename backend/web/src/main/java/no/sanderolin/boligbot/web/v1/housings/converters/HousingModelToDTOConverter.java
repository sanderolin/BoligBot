package no.sanderolin.boligbot.web.v1.housings.converters;

import no.sanderolin.boligbot.dao.model.HousingModel;
import no.sanderolin.boligbot.web.v1.housings.response.HousingDTO;

public class HousingModelToDTOConverter {

    public static HousingDTO toDTO(HousingModel model) {
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
