package no.sanderolin.boligbot.service.city;

import lombok.RequiredArgsConstructor;
import no.sanderolin.boligbot.dao.model.CityModel;
import no.sanderolin.boligbot.dao.repository.CityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

    public List<CityModel> getCities() {
        return cityRepository.findAll();
    }
}
