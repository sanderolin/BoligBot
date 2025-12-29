package no.sanderolin.boligbot.service.housingType;

import lombok.RequiredArgsConstructor;
import no.sanderolin.boligbot.dao.model.HousingTypeModel;
import no.sanderolin.boligbot.dao.repository.HousingTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HousingTypeService {

    private final HousingTypeRepository housingTypeRepository;

    public List<HousingTypeModel> getAllHousingTypes() {
        return housingTypeRepository.findAll();
    }
}
