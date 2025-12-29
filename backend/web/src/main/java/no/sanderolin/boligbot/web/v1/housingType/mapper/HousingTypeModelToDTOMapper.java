package no.sanderolin.boligbot.web.v1.housingType.mapper;

import no.sanderolin.boligbot.dao.model.HousingTypeModel;
import no.sanderolin.boligbot.web.v1.housingType.response.HousingTypeDTO;

public class HousingTypeModelToDTOMapper {

    public static HousingTypeDTO toDTO(HousingTypeModel housingTypeModel) {
        return new HousingTypeDTO(
                housingTypeModel.getId(),
                housingTypeModel.getName()
        );
    }
}
