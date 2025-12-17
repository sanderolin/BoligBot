package no.sanderolin.boligbot.housingimport.service;

import no.sanderolin.boligbot.dao.model.HousingModel;
import no.sanderolin.boligbot.dao.repository.HousingRepository;
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
    @InjectMocks private HousingCatalogImportService importTask;

    private HousingModel existingHousing;
    private HousingModel newHousing;
    private HousingModel updatedHousing;
    private Instant timestamp;

    @BeforeEach
    void setUp() {
        timestamp = Instant.now();
        existingHousing = createHousingModel(
                "EXISTING-001",
                "Old Name",
                "Old Address",
                "Old Type",
                "Old City",
                "Old District",
                BigDecimal.valueOf(10.0),
                1000
        );
        existingHousing.setCreatedAt(timestamp.minus(1, ChronoUnit.DAYS));
        existingHousing.setLastModifiedAt(timestamp.minus(1, ChronoUnit.DAYS));
        existingHousing.setLastImportedAt(timestamp.minus(1, ChronoUnit.DAYS));

        newHousing = createHousingModel(
                "NEW-001",
                "New Housing",
                "New Address",
                "New Type",
                "New City",
                "New District",
                BigDecimal.valueOf(15.0),
                2000
        );

        updatedHousing = createHousingModel(
                existingHousing.getRentalObjectId(),
                "Updated Name",
                "Updated Address",
                "Updated Type",
                "Updated City",
                "Updated District",
                BigDecimal.valueOf(12.0),
                1500
        );
    }

    @Test
    void runImport_ShouldCreateNewRecords() {
        when(catalogFetcher.fetchHousingEntitiesFromGraphQL()).thenReturn(List.of(newHousing));

        when(housingRepository.findAllByRentalObjectIdIn(eq(List.of(newHousing.getRentalObjectId()))))
                .thenReturn(Collections.emptyList());
        when(housingRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        importTask.runImport();

        verify(catalogFetcher).fetchHousingEntitiesFromGraphQL();
        verify(housingRepository).findAllByRentalObjectIdIn(List.of(newHousing.getRentalObjectId()));
        verify(housingRepository).saveAll(argThat(arg -> {
            List<HousingModel> models = (List<HousingModel>) arg;
            HousingModel model = models.getFirst();
            return models.size() == 1
                    && model.getRentalObjectId().equals(newHousing.getRentalObjectId())
                    && model.getCreatedAt() != null
                    && model.getLastModifiedAt() != null
                    && model.getLastImportedAt() != null
                    && model.getCreatedAt().isAfter(timestamp)
                    && model.getLastModifiedAt().isAfter(timestamp)
                    && model.getLastImportedAt().isAfter(timestamp);
        }));
    }


    @Test
    void runImport_WithExistingUnchangedHousing_ShouldOnlyUpdateImportTime() {
        HousingModel unchangedHousing = createHousingModel(
                existingHousing.getRentalObjectId(),
                existingHousing.getName(),
                existingHousing.getAddress(),
                existingHousing.getHousingType(),
                existingHousing.getCity(),
                existingHousing.getDistrict(),
                existingHousing.getAreaSqm(),
                existingHousing.getPricePerMonth()
        );

        when(catalogFetcher.fetchHousingEntitiesFromGraphQL()).thenReturn(List.of(unchangedHousing));
        when(housingRepository.findAllByRentalObjectIdIn(eq(List.of(unchangedHousing.getRentalObjectId())))).thenReturn(List.of(existingHousing));
        when(housingRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        importTask.runImport();

        verify(housingRepository).saveAll(argThat(list -> {
            List<HousingModel> models = (List<HousingModel>) list;
            HousingModel unchangedHousingModel = models.getFirst();
            return models.size() == 1
                    && unchangedHousingModel.getName().equals(existingHousing.getName())
                    && unchangedHousingModel.getAddress().equals(existingHousing.getAddress())
                    && unchangedHousingModel.getLastModifiedAt().equals(timestamp.minus(1, ChronoUnit.DAYS))
                    && unchangedHousingModel.getLastImportedAt().isAfter(timestamp);
        }));
    }

    @Test
    void runImport_ShouldUpdateFields() {
        when(catalogFetcher.fetchHousingEntitiesFromGraphQL()).thenReturn(List.of(updatedHousing));
        when(housingRepository.findAllByRentalObjectIdIn(eq(List.of(updatedHousing.getRentalObjectId())))).thenReturn(List.of(existingHousing));
        when(housingRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        importTask.runImport();

        verify(housingRepository).saveAll(argThat(list -> {
            List<HousingModel> models = (List<HousingModel>) list;
            HousingModel updated = models.getFirst();
            return models.size() == 1
                    && updated.getName().equals(updatedHousing.getName())
                    && updated.getAddress().equals(updatedHousing.getAddress())
                    && updated.getHousingType().equals(updatedHousing.getHousingType())
                    && updated.getCity().equals(updatedHousing.getCity())
                    && updated.getDistrict().equals(updatedHousing.getDistrict())
                    && updated.getAreaSqm().equals(updatedHousing.getAreaSqm())
                    && updated.getPricePerMonth() == updatedHousing.getPricePerMonth()
                    && updated.getCreatedAt().equals(timestamp.minus(1, ChronoUnit.DAYS))
                    && updated.getLastModifiedAt().isAfter(timestamp)
                    && updated.getLastImportedAt().isAfter(timestamp);
        }));
    }

    @Test
    void runImport_WithEmptyResponse_ShouldExitEarly() {
        when(catalogFetcher.fetchHousingEntitiesFromGraphQL()).thenReturn(Collections.emptyList());
        importTask.runImport();

        verify(catalogFetcher).fetchHousingEntitiesFromGraphQL();
        verifyNoInteractions(housingRepository);
    }

    @Test
    void runImport_WithHousingImportException_ShouldPropagateException() {
        when(catalogFetcher.fetchHousingEntitiesFromGraphQL())
                .thenThrow(new HousingImportException("exception"));

        assertThatThrownBy(() -> importTask.runImport())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("exception");

        verifyNoInteractions(housingRepository);
    }

    @Test
    void runImport_WithUnexpectedException_ShouldWrapException() {
        when(catalogFetcher.fetchHousingEntitiesFromGraphQL())
                .thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> importTask.runImport())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Unexpected error during housing import")
                .hasCauseInstanceOf(RuntimeException.class);

        verifyNoInteractions(housingRepository);
    }

    @Test
    void runImport_WithDatabaseException_ShouldWrapException() {
        when(catalogFetcher.fetchHousingEntitiesFromGraphQL()).thenReturn(List.of(newHousing));
        when(housingRepository.findAllByRentalObjectIdIn(any()))
                .thenThrow(new RuntimeException("Database connection error"));

        assertThatThrownBy(() -> importTask.runImport())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Unexpected error during housing import")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    private HousingModel createHousingModel(String rentalObjectId, String name, String address,
                                            String housingType, String city, String district,
                                            BigDecimal area, int price) {
        HousingModel model = new HousingModel();
        model.setRentalObjectId(rentalObjectId);
        model.setName(name);
        model.setAddress(address);
        model.setHousingType(housingType);
        model.setCity(city);
        model.setDistrict(district);
        model.setAreaSqm(area);
        model.setPricePerMonth(price);
        return model;
    }
}