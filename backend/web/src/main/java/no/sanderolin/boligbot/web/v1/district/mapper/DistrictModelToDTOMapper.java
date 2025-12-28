package no.sanderolin.boligbot.web.v1.district.mapper;

import no.sanderolin.boligbot.dao.model.DistrictModel;
import no.sanderolin.boligbot.web.v1.district.response.DistrictDTO;

public class DistrictModelToDTOMapper {

    public static DistrictDTO toDTO(DistrictModel districtModel) {
        return new DistrictDTO(
                districtModel.getId(),
                districtModel.getName()
        );
    }
}
