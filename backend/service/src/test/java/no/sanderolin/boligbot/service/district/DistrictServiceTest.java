package no.sanderolin.boligbot.service.district;

import no.sanderolin.boligbot.dao.model.DistrictModel;
import no.sanderolin.boligbot.dao.repository.CityRepository;
import no.sanderolin.boligbot.dao.repository.DistrictRepository;
import org.hibernate.ObjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DistrictServiceTest {

    @Mock private DistrictRepository districtRepository;
    @Mock private CityRepository cityRepository;
    @InjectMocks private DistrictService districtService;

    @Test
    void getDistricts_shouldReturnAllDistricts() {
        List<DistrictModel> districts = Arrays.asList(
                new DistrictModel(),
                new DistrictModel()
        );
        when(districtRepository.findAll()).thenReturn(districts);
        List<DistrictModel> result = districtService.getDistricts();
        assertThat(result)
                .usingRecursiveAssertion()
                .isEqualTo(districts);
    }

    @Test
    void getDistrictsByCityId_WithExistingCityId_shouldReturnDistricts() {
        List<DistrictModel> districts = List.of(
                new DistrictModel(),
                new DistrictModel()
        );
        long cityId = 1L;
        when(cityRepository.existsById(eq(cityId))).thenReturn(true);
        when(districtRepository.findAllByCityId(eq(cityId))).thenReturn(districts);

        List<DistrictModel> result = districtService.getDistrictsByCityId(cityId);
        assertThat(result)
                .usingRecursiveAssertion()
                .isEqualTo(districts);
    }

    @Test
    void getDistrictsByCityId_WithNonExistingCityId_shouldReturnEmptyList() {
        long cityId = 1L;
        when(cityRepository.existsById(eq(cityId))).thenReturn(false);
        assertThrows(ObjectNotFoundException.class, () ->
                districtService.getDistrictsByCityId(cityId)
        );
        verifyNoInteractions(districtRepository);
    }
}
