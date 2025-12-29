package no.sanderolin.boligbot.service.housingType;

import no.sanderolin.boligbot.dao.model.HousingTypeModel;
import no.sanderolin.boligbot.dao.repository.HousingTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HousingTypeServiceTest {

    @Mock private HousingTypeRepository housingTypeRepository;
    @InjectMocks private HousingTypeService housingTypeService;

    @Test
    void getAllHousingTypes_shouldReturnAllHousingTypes() {
        List<HousingTypeModel> housingTypeModels = List.of(
                new HousingTypeModel(),
                new HousingTypeModel()
        );
        when(housingTypeRepository.findAll()).thenReturn(housingTypeModels);
        List<HousingTypeModel> result = housingTypeService.getAllHousingTypes();
        assertThat(result)
                .usingRecursiveAssertion()
                .isEqualTo(housingTypeModels);
    }
}
