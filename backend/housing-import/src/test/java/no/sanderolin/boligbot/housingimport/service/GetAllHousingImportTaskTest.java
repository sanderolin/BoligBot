package no.sanderolin.boligbot.housingimport.service;

import no.sanderolin.boligbot.dao.model.HousingModel;
import no.sanderolin.boligbot.dao.repository.HousingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllHousingImportTaskTest {

    @Mock
    private SitGraphQLClient sitGraphQLClient;

    @Mock
    private GraphQLHousingMapper graphQLHousingMapper;

    @Mock
    private HousingRepository housingRepository;

    @InjectMocks
    private GetAllHousingImportTask importTask;

    private HousingModel existingHousing;
    private HousingModel newHousing;
    private HousingModel updatedHousing;
    private final String mockGraphQLResponse = "response";
    private final String expectedQuery = "query";
    private final LocalDateTime timestamp = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        existingHousing = createHousingModel("EXISTING-001", "Old Name", "Old Address",
                "Old Type", "Old City", "Old District", BigDecimal.valueOf(10.0), 1000);
        existingHousing.setCreatedAt(timestamp.minusDays(1));
        existingHousing.setLastModifiedAt(timestamp.minusDays(1));
        existingHousing.setLastImportedAt(timestamp.minusDays(1));

        newHousing = createHousingModel("NEW-001", "New Housing", "New Address",
                "New Type", "New City", "New District", BigDecimal.valueOf(15.0), 2000);

        updatedHousing = createHousingModel(existingHousing.getRentalObjectId(), "Updated Name", "Updated Address",
                "Updated Type", "Updated City", "Updated District", BigDecimal.valueOf(12.0), 1500);
    }

    @Test
    void importAllHousing_WithNewHousing_ShouldCreateNewRecords() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockGraphQLResponse);
        when(graphQLHousingMapper.mapHousingEntities(eq(mockGraphQLResponse))).thenReturn(List.of(newHousing));
        when(housingRepository.findAllByRentalObjectIdIn(eq(List.of(newHousing.getRentalObjectId())))).thenReturn(Collections.emptyList());
        when(housingRepository.saveAll(any())).thenReturn(List.of(newHousing));
        importTask.importAllHousing();

        verify(sitGraphQLClient).executeGraphQLQuery(anyString());
        verify(graphQLHousingMapper).mapHousingEntities(mockGraphQLResponse);
        verify(housingRepository).findAllByRentalObjectIdIn(List.of(newHousing.getRentalObjectId()));
        verify(housingRepository).saveAll(argThat(list -> {
            List<HousingModel> models = (List<HousingModel>) list;
            return models.size() == 1 &&
                   models.getFirst().getRentalObjectId().equals(newHousing.getRentalObjectId()) &&
                   models.getFirst().getCreatedAt() != null &&
                   models.getFirst().getLastModifiedAt() != null &&
                   models.getFirst().getLastImportedAt() != null;
        }));
    }

    @Test
    void importAllHousing_WithExistingUnchangedHousing_ShouldOnlyUpdateImportTime() {
        HousingModel unchangedHousing = createHousingModel(existingHousing.getRentalObjectId(), existingHousing.getName(),
                existingHousing.getAddress(), existingHousing.getHousingType(), existingHousing.getCity(),
                existingHousing.getDistrict(), existingHousing.getAreaSqm(), existingHousing.getPricePerMonth());

        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockGraphQLResponse);
        when(graphQLHousingMapper.mapHousingEntities(eq(mockGraphQLResponse))).thenReturn(List.of(unchangedHousing));
        when(housingRepository.findAllByRentalObjectIdIn(eq(List.of(unchangedHousing.getRentalObjectId())))).thenReturn(List.of(existingHousing));
        when(housingRepository.saveAll(any())).thenReturn(List.of(existingHousing));

        importTask.importAllHousing();

        verify(housingRepository).saveAll(argThat(list -> {
            List<HousingModel> models = (List<HousingModel>) list;
            HousingModel unchangedHousingModel = models.getFirst();
            return models.size() == 1 &&
                   unchangedHousingModel.getLastModifiedAt().isEqual(timestamp.minusDays(1)) &&
                   unchangedHousingModel.getLastImportedAt().isAfter(timestamp);
        }));
    }

    @Test
    void importAllHousing_WithExistingChangedHousing_ShouldUpdateFields() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockGraphQLResponse);
        when(graphQLHousingMapper.mapHousingEntities(eq(mockGraphQLResponse))).thenReturn(List.of(updatedHousing));
        when(housingRepository.findAllByRentalObjectIdIn(eq(List.of(updatedHousing.getRentalObjectId())))).thenReturn(List.of(existingHousing));
        when(housingRepository.saveAll(any())).thenReturn(List.of(updatedHousing));

        importTask.importAllHousing();

        verify(housingRepository).saveAll(argThat(list -> {
            List<HousingModel> models = (List<HousingModel>) list;
            HousingModel updated = models.getFirst();
            return models.size() == 1 &&
                   updated.getName().equals(updatedHousing.getName()) &&
                   updated.getAddress().equals(updatedHousing.getAddress()) &&
                   updated.getHousingType().equals(updatedHousing.getHousingType()) &&
                   updated.getCity().equals(updatedHousing.getCity()) &&
                   updated.getDistrict().equals(updatedHousing.getDistrict()) &&
                   updated.getAreaSqm().equals(updatedHousing.getAreaSqm()) &&
                   updated.getPricePerMonth() == updatedHousing.getPricePerMonth() &&
                   updated.getCreatedAt().isEqual(timestamp.minusDays(1)) &&
                   updated.getLastModifiedAt().isAfter(timestamp) &&
                   updated.getLastImportedAt().isAfter(timestamp);
        }));
    }

    @Test
    void importAllHousing_WithEmptyResponse_ShouldExitEarly() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockGraphQLResponse);
        when(graphQLHousingMapper.mapHousingEntities(mockGraphQLResponse)).thenReturn(Collections.emptyList());

        importTask.importAllHousing();

        verify(sitGraphQLClient).executeGraphQLQuery(anyString());
        verify(graphQLHousingMapper).mapHousingEntities(mockGraphQLResponse);
        verifyNoInteractions(housingRepository);
    }

    @Test
    void importAllHousing_WithHousingImportException_ShouldPropagateException() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString()))
                .thenThrow(new HousingImportException("exception"));

        assertThatThrownBy(() -> importTask.importAllHousing())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Failed to fetch housing data from external API");

        verifyNoInteractions(graphQLHousingMapper);
        verifyNoInteractions(housingRepository);
    }

    @Test
    void importAllHousing_WithGraphQLMapperException_ShouldPropagateException() {
        // Given
        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockGraphQLResponse);
        when(graphQLHousingMapper.mapHousingEntities(mockGraphQLResponse))
                .thenThrow(new HousingImportException("exception"));

        assertThatThrownBy(() -> importTask.importAllHousing())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Failed to fetch housing data from external API");

        verifyNoInteractions(housingRepository);
    }

    @Test
    void importAllHousing_WithDatabaseException_ShouldWrapException() {
        // Given
        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockGraphQLResponse);
        when(graphQLHousingMapper.mapHousingEntities(mockGraphQLResponse)).thenReturn(List.of(newHousing));
        when(housingRepository.findAllByRentalObjectIdIn(any()))
                .thenThrow(new RuntimeException("Database connection error"));

        // When & Then
        assertThatThrownBy(() -> importTask.importAllHousing())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Unexpected error during housing import")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    // Helper methods
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