package no.sanderolin.boligbot.service.district;

import lombok.RequiredArgsConstructor;
import no.sanderolin.boligbot.dao.model.CityModel;
import no.sanderolin.boligbot.dao.model.DistrictModel;
import no.sanderolin.boligbot.dao.repository.CityRepository;
import no.sanderolin.boligbot.dao.repository.DistrictRepository;
import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DistrictService {

    private final DistrictRepository districtRepository;
    private final CityRepository cityRepository;

    public List<DistrictModel> getDistricts() {
        return districtRepository.findAll();
    }

    public List<DistrictModel> getDistrictsByCityId(long cityId) {
        if (!cityRepository.existsById(cityId)) {
            throw new ObjectNotFoundException(
                    "City with id " + cityId + " not found",
                    CityModel.class
            );
        }
        return districtRepository.findAllByCityId(cityId);
    }
}
