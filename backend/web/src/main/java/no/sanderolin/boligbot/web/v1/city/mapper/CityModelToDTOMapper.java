package no.sanderolin.boligbot.web.v1.city.mapper;

import no.sanderolin.boligbot.dao.model.CityModel;
import no.sanderolin.boligbot.web.v1.city.response.CityDTO;

public class CityModelToDTOMapper {

    public static CityDTO toDTO(CityModel cityModel) {
        return new CityDTO(
                cityModel.getId(),
                cityModel.getName()
        );
    }
}
