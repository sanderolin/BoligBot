package no.sanderolin.boligbot.service.city;

import no.sanderolin.boligbot.dao.model.CityModel;
import no.sanderolin.boligbot.dao.repository.CityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock private CityRepository cityRepository;
    @InjectMocks private CityService cityService;

    @Test
    void getCities_shouldReturnAllCities() {
        List<CityModel> mockResult = List.of(
                new CityModel(),
                new CityModel()
        );
        when(cityRepository.findAll()).thenReturn(mockResult);

        List<CityModel> result = cityService.getCities();
        assertThat(result)
                .usingRecursiveAssertion()
                .isEqualTo(mockResult);
    }
}