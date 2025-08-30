package no.sanderolin.boligbot.housingimport.service;

import no.sanderolin.boligbot.dao.repository.HousingRepository;
import no.sanderolin.boligbot.housingimport.dto.HousingAvailabilityDTO;
import no.sanderolin.boligbot.housingimport.exception.HousingImportException;
import no.sanderolin.boligbot.housingimport.util.GraphQLHousingMapper;
import no.sanderolin.boligbot.housingimport.util.SitGraphQLClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HousingAvailabilityImportServiceTest {

    @Mock private SitGraphQLClient sitGraphQLClient;
    @Mock private GraphQLHousingMapper graphQLHousingMapper;
    @Mock private HousingRepository housingRepository;
    @InjectMocks private HousingAvailabilityImportService importTask;

    private final String mockGraphQLResponse = "graphql_response";
    private List<HousingAvailabilityDTO> availableHousings;
    private List<String> availableHousingIds;

    @BeforeEach
    void setUp() {
        availableHousings = List.of(new HousingAvailabilityDTO("1", LocalDate.now()));
        availableHousingIds = List.of("1");
        lenient().when(housingRepository.count()).thenReturn(6000L);
    }

    @Test
    void runImport_ShouldUpdateAvailability() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockGraphQLResponse);
        when(graphQLHousingMapper.mapHousingAvailability(mockGraphQLResponse)).thenReturn(availableHousings);
        when(housingRepository.bulkUpdateAvailability(availableHousingIds, true)).thenReturn(3);
        when(housingRepository.markUnavailableNotInIds(availableHousingIds)).thenReturn(2);

        importTask.runImport();

        verify(sitGraphQLClient).executeGraphQLQuery(anyString());
        verify(graphQLHousingMapper).mapHousingAvailability(mockGraphQLResponse);
        verify(housingRepository).bulkUpdateAvailability(availableHousingIds, true);
        verify(housingRepository).markUnavailableNotInIds(availableHousingIds);
    }

    @Test
    void runImport_WithEmptyResponse_ShouldExitEarly() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockGraphQLResponse);
        when(graphQLHousingMapper.mapHousingAvailability(mockGraphQLResponse)).thenReturn(Collections.emptyList());

        importTask.runImport();

        verify(sitGraphQLClient).executeGraphQLQuery(anyString());
        verify(graphQLHousingMapper).mapHousingAvailability(mockGraphQLResponse);
        verify(housingRepository, never()).bulkUpdateAvailability(any(), anyBoolean());
        verify(housingRepository, never()).markUnavailableNotInIds(any());
    }

    @Test
    void runImport_WithGraphQLClientException_ShouldPropagateAsHousingImportException() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString()))
                .thenThrow(new RuntimeException("GraphQL connection error"));

        assertThatThrownBy(() -> importTask.runImport())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Failed to fetch availability from GraphQL API")
                .hasCauseInstanceOf(RuntimeException.class);

        verifyNoInteractions(graphQLHousingMapper);
        verify(housingRepository, never()).bulkUpdateAvailability(any(), anyBoolean());
        verify(housingRepository, never()).markUnavailableNotInIds(any());
    }

    @Test
    void runImport_WithHousingImportException_ShouldPropagateException() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString()))
                .thenThrow(new HousingImportException("API unavailable"));

        assertThatThrownBy(() -> importTask.runImport())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Failed to fetch availability from GraphQL API");

        verifyNoInteractions(graphQLHousingMapper);
        verify(housingRepository, never()).bulkUpdateAvailability(any(), anyBoolean());
        verify(housingRepository, never()).markUnavailableNotInIds(any());
    }

    @Test
    void runImport_WithGraphQLMapperException_ShouldPropagateAsHousingImportException() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockGraphQLResponse);
        when(graphQLHousingMapper.mapHousingAvailability(mockGraphQLResponse))
                .thenThrow(new RuntimeException("Mapping error"));

        assertThatThrownBy(() -> importTask.runImport())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Failed to fetch availability from GraphQL API")
                .hasCauseInstanceOf(RuntimeException.class);

        verify(housingRepository, never()).bulkUpdateAvailability(any(), anyBoolean());
        verify(housingRepository, never()).markUnavailableNotInIds(any());
    }

    @Test
    void runImport_WithRepositoryException_ShouldWrapAsUnexpectedException() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockGraphQLResponse);
        when(graphQLHousingMapper.mapHousingAvailability(mockGraphQLResponse)).thenReturn(availableHousings);
        when(housingRepository.bulkUpdateAvailability(any(), anyBoolean()))
                .thenThrow(new RuntimeException("Database connection error"));

        assertThatThrownBy(() -> importTask.runImport())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Unexpected error during availability import")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void runImport_WithSecondRepositoryCallException_ShouldWrapAsUnexpectedException() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockGraphQLResponse);
        when(graphQLHousingMapper.mapHousingAvailability(mockGraphQLResponse)).thenReturn(availableHousings);
        when(housingRepository.bulkUpdateAvailability(any(), anyBoolean())).thenReturn(3);
        when(housingRepository.markUnavailableNotInIds(any()))
                .thenThrow(new RuntimeException("Database error on second call"));

        assertThatThrownBy(() -> importTask.runImport())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Unexpected error during availability import")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void runImport_WithEmptyDatabase_ShouldExitImport() {
        when(housingRepository.count()).thenReturn(0L);

        importTask.runImport();

        verifyNoInteractions(sitGraphQLClient);
        verifyNoInteractions(graphQLHousingMapper);
        verify(housingRepository, never()).bulkUpdateAvailability(any(), anyBoolean());
        verify(housingRepository, never()).markUnavailableNotInIds(any());
    }
}