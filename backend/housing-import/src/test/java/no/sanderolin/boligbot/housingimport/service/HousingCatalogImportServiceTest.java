package no.sanderolin.boligbot.housingimport.service;

import no.sanderolin.boligbot.dao.model.CityModel;
import no.sanderolin.boligbot.dao.model.DistrictModel;
import no.sanderolin.boligbot.dao.model.HousingModel;
import no.sanderolin.boligbot.dao.model.HousingTypeModel;
import no.sanderolin.boligbot.dao.repository.CityRepository;
import no.sanderolin.boligbot.dao.repository.DistrictRepository;
import no.sanderolin.boligbot.dao.repository.HousingRepository;
import no.sanderolin.boligbot.dao.repository.HousingTypeRepository;
import no.sanderolin.boligbot.housingimport.dto.HousingDTO;
import no.sanderolin.boligbot.housingimport.exception.HousingImportException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HousingCatalogImportServiceTest {

    @Mock private HousingCatalogFetcher catalogFetcher;

    @Mock private HousingRepository housingRepository;
    @Mock private CityRepository cityRepository;
    @Mock private DistrictRepository districtRepository;
    @Mock private HousingTypeRepository housingTypeRepository;

    @InjectMocks private HousingCatalogImportService importTask;

    private Instant beforeRun;

    private CityModel existingCity;
    private DistrictModel existingDistrict;
    private HousingTypeModel existingHousingType;

    private HousingModel existingHousing;

    @BeforeEach
    void setUp() {
        beforeRun = Instant.now().minus(1, ChronoUnit.SECONDS);
        existingCity = city(1L, "Old City");
        existingDistrict = district(10L, "Old District", existingCity);
        existingHousingType = housingType(20L, "Old Type");

        existingHousing = housingModel(
                "EXISTING-001",
                "Old Name",
                "Old Address",
                existingHousingType,
                existingDistrict,
                BigDecimal.valueOf(10.0),
                1000
        );

        Instant old = Instant.now().minus(1, ChronoUnit.DAYS);
        existingHousing.setCreatedAt(old);
        existingHousing.setLastModifiedAt(old);
        existingHousing.setLastImportedAt(old);
    }

    @Test
    void runImport_ShouldCreateNewRecords() {
        HousingDTO incoming = dto(
                "NEW-001",
                "New Housing",
                "New Address",
                "New Type",
                "New City",
                "New District",
                BigDecimal.valueOf(15.0),
                2000
        );

        when(catalogFetcher.fetchHousingsFromGraphQL()).thenReturn(List.of(incoming));

        when(cityRepository.findAllByNameIn(eq(Collections.singleton("New City"))))
                .thenReturn(Collections.emptyList())
                .thenReturn(List.of(city(2L, "New City")));
        when(cityRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        when(housingTypeRepository.findAllByNameIn(eq(Collections.singleton("New Type"))))
                .thenReturn(Collections.emptyList())
                .thenReturn(List.of(housingType(3L, "New Type")));
        when(housingTypeRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        when(districtRepository.findAllByCityIdIn(eq(Collections.singleton(2L))))
                .thenReturn(Collections.emptyList())
                .thenReturn(List.of(district(4L, "New District", city(2L, "New City"))));
        when(districtRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        when(housingRepository.findAllByRentalObjectIdIn(eq(List.of("NEW-001"))))
                .thenReturn(Collections.emptyList());
        when(housingRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        importTask.runImport();

        verify(housingRepository).saveAll(argThat(arg -> {
            List<HousingModel> models = (List<HousingModel>) arg;
            if (models.size() != 1) return false;

            HousingModel created = models.getFirst();
            return created.getRentalObjectId().equals("NEW-001")
                    && created.getName().equals("New Housing")
                    && created.getAddress().equals("New Address")
                    && created.getHousingType() != null
                    && created.getHousingType().getName().equals("New Type")
                    && created.getDistrict() != null
                    && created.getDistrict().getName().equals("New District")
                    && created.getCity() != null
                    && created.getCity().getName().equals("New City")
                    && created.getAreaSqm().compareTo(BigDecimal.valueOf(15.0)) == 0
                    && created.getPricePerMonth() == 2000
                    && created.getCreatedAt() != null
                    && created.getLastModifiedAt() != null
                    && created.getLastImportedAt() != null
                    && created.getCreatedAt().isAfter(beforeRun)
                    && created.getLastModifiedAt().isAfter(beforeRun)
                    && created.getLastImportedAt().isAfter(beforeRun);
        }));
    }

    @Test
    void runImport_WithExistingUnchangedHousing_ShouldOnlyUpdateImportTime_AndNotSaveHousing() {
        HousingDTO incoming = dto(
                existingHousing.getRentalObjectId(),
                existingHousing.getName(),
                existingHousing.getAddress(),
                existingHousing.getHousingType().getName(),
                existingHousing.getCity().getName(),
                existingHousing.getDistrict().getName(),
                existingHousing.getAreaSqm(),
                existingHousing.getPricePerMonth()
        );

        when(catalogFetcher.fetchHousingsFromGraphQL()).thenReturn(List.of(incoming));

        when(cityRepository.findAllByNameIn(eq(Collections.singleton(existingCity.getName()))))
                .thenReturn(List.of(existingCity));
        when(housingTypeRepository.findAllByNameIn(eq(Collections.singleton(existingHousingType.getName()))))
                .thenReturn(List.of(existingHousingType));
        when(districtRepository.findAllByCityIdIn(eq(Collections.singleton(existingCity.getId()))))
                .thenReturn(List.of(existingDistrict));

        when(housingRepository.findAllByRentalObjectIdIn(eq(List.of(existingHousing.getRentalObjectId()))))
                .thenReturn(List.of(existingHousing));

        Instant oldLastModifiedAt = existingHousing.getLastModifiedAt();
        Instant oldLastImportedAt = existingHousing.getLastImportedAt();

        importTask.runImport();

        assertThat(existingHousing.getLastImportedAt())
                .isAfter(oldLastImportedAt);
        assertThat(existingHousing.getLastModifiedAt())
                .isEqualTo(oldLastModifiedAt);

        verify(housingRepository, never()).saveAll(any());
    }

    @Test
    void runImport_WithExistingChangedHousing_ShouldUpdateFields_AndNotSaveHousing() {
        HousingDTO incoming = dto(
                existingHousing.getRentalObjectId(),
                "Updated Name",
                "Updated Address",
                "Updated Type",
                "Updated City",
                "Updated District",
                BigDecimal.valueOf(12.0),
                1500
        );

        CityModel updatedCity = city(11L, "Updated City");
        DistrictModel updatedDistrict = district(12L, "Updated District", updatedCity);
        HousingTypeModel updatedType = housingType(13L, "Updated Type");

        when(catalogFetcher.fetchHousingsFromGraphQL()).thenReturn(List.of(incoming));

        when(cityRepository.findAllByNameIn(eq(Collections.singleton("Updated City"))))
                .thenReturn(List.of(updatedCity));
        when(housingTypeRepository.findAllByNameIn(eq(Collections.singleton("Updated Type"))))
                .thenReturn(List.of(updatedType));
        when(districtRepository.findAllByCityIdIn(eq(Collections.singleton(updatedCity.getId()))))
                .thenReturn(List.of(updatedDistrict));

        when(housingRepository.findAllByRentalObjectIdIn(eq(List.of(existingHousing.getRentalObjectId()))))
                .thenReturn(List.of(existingHousing));

        Instant oldLastModifiedAt = existingHousing.getLastModifiedAt();
        Instant oldLastImportedAt = existingHousing.getLastImportedAt();

        importTask.runImport();

        assertThat(existingHousing.getName()).isEqualTo("Updated Name");
        assertThat(existingHousing.getAddress()).isEqualTo("Updated Address");
        assertThat(existingHousing.getHousingType().getName()).isEqualTo("Updated Type");
        assertThat(existingHousing.getDistrict().getName()).isEqualTo("Updated District");
        assertThat(existingHousing.getCity().getName()).isEqualTo("Updated City");
        assertThat(existingHousing.getAreaSqm().compareTo(BigDecimal.valueOf(12.0))).isZero();
        assertThat(existingHousing.getPricePerMonth()).isEqualTo(1500);

        assertThat(existingHousing.getLastImportedAt()).isAfter(oldLastImportedAt);
        assertThat(existingHousing.getLastModifiedAt()).isAfter(oldLastModifiedAt);

        verify(housingRepository, never()).saveAll(any());
    }

    @Test
    void runImport_WithEmptyResponse_ShouldExitEarly() {
        when(catalogFetcher.fetchHousingsFromGraphQL()).thenReturn(Collections.emptyList());

        importTask.runImport();

        verify(catalogFetcher).fetchHousingsFromGraphQL();
        verifyNoInteractions(housingRepository, cityRepository, districtRepository, housingTypeRepository);
    }

    @Test
    void runImport_WithHousingImportException_ShouldPropagateException() {
        when(catalogFetcher.fetchHousingsFromGraphQL())
                .thenThrow(new HousingImportException("exception"));

        assertThatThrownBy(() -> importTask.runImport())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("exception");

        verifyNoInteractions(housingRepository, cityRepository, districtRepository, housingTypeRepository);
    }

    @Test
    void runImport_WithUnexpectedException_ShouldWrapException() {
        when(catalogFetcher.fetchHousingsFromGraphQL())
                .thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> importTask.runImport())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Unexpected error during housing import")
                .hasCauseInstanceOf(RuntimeException.class);

        verifyNoInteractions(housingRepository, cityRepository, districtRepository, housingTypeRepository);
    }

    @Test
    void runImport_WithDatabaseException_ShouldWrapException() {
        HousingDTO incoming = dto(
                "NEW-001",
                "New Housing",
                "New Address",
                "New Type",
                "New City",
                "New District",
                BigDecimal.valueOf(15.0),
                2000
        );

        when(catalogFetcher.fetchHousingsFromGraphQL()).thenReturn(List.of(incoming));
        when(cityRepository.findAllByNameIn(any())).thenThrow(new RuntimeException("Database connection error"));

        assertThatThrownBy(() -> importTask.runImport())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Unexpected error during housing import")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    private HousingDTO dto(
            String rentalObjectId,
            String name,
            String address,
            String housingTypeName,
            String cityName,
            String districtName,
            BigDecimal areaSqm,
            int pricePerMonth
    ) {
        return new HousingDTO(
                rentalObjectId,
                name,
                address,
                housingTypeName,
                cityName,
                districtName,
                areaSqm,
                pricePerMonth
        );
    }

    private CityModel city(Long id, String name) {
        CityModel c = new CityModel();
        c.setId(id);
        c.setName(name);
        return c;
    }

    private DistrictModel district(Long id, String name, CityModel city) {
        DistrictModel d = new DistrictModel();
        d.setId(id);
        d.setName(name);
        d.setCity(city);
        return d;
    }

    private HousingTypeModel housingType(Long id, String name) {
        HousingTypeModel ht = new HousingTypeModel();
        ht.setId(id);
        ht.setName(name);
        return ht;
    }

    private HousingModel housingModel(
            String rentalObjectId,
            String name,
            String address,
            HousingTypeModel housingType,
            DistrictModel district,
            BigDecimal areaSqm,
            int pricePerMonth
    ) {
        HousingModel h = new HousingModel();
        h.setRentalObjectId(rentalObjectId);
        h.setName(name);
        h.setAddress(address);
        h.setHousingType(housingType);
        h.setDistrict(district);
        h.setAreaSqm(areaSqm);
        h.setPricePerMonth(pricePerMonth);
        return h;
    }
}