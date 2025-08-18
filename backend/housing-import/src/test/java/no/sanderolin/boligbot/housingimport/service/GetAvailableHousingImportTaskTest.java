package no.sanderolin.boligbot.housingimport.service;

import no.sanderolin.boligbot.dao.repository.HousingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAvailableHousingImportTaskTest {

    @Mock private SitGraphQLClient sitGraphQLClient;
    @Mock private GraphQLHousingMapper graphQLHousingMapper;
    @Mock private HousingRepository housingRepository;
    @InjectMocks private GetAvailableHousingImportTask importTask;

    private final String mockGraphQLResponse = "graphql_response";
    private List<String> availableHousingIds;

    @BeforeEach
    void setUp() {
        availableHousingIds = List.of("HOUSING-001", "HOUSING-002", "HOUSING-003");
    }

    @Test
    void importAvailableHousing_WithAvailableHousingIds_ShouldUpdateAvailability() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockGraphQLResponse);
        when(graphQLHousingMapper.mapHousingIds(mockGraphQLResponse)).thenReturn(availableHousingIds);
        when(housingRepository.bulkUpdateAvailability(availableHousingIds, true)).thenReturn(3);
        when(housingRepository.markUnavailableNotInIds(availableHousingIds)).thenReturn(2);

        importTask.importAvailableHousing();

        verify(sitGraphQLClient).executeGraphQLQuery(anyString());
        verify(graphQLHousingMapper).mapHousingIds(mockGraphQLResponse);
        verify(housingRepository).bulkUpdateAvailability(availableHousingIds, true);
        verify(housingRepository).markUnavailableNotInIds(availableHousingIds);
    }

    @Test
    void importAvailableHousing_WithEmptyResponse_ShouldExitEarly() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockGraphQLResponse);
        when(graphQLHousingMapper.mapHousingIds(mockGraphQLResponse)).thenReturn(Collections.emptyList());

        importTask.importAvailableHousing();

        verify(sitGraphQLClient).executeGraphQLQuery(anyString());
        verify(graphQLHousingMapper).mapHousingIds(mockGraphQLResponse);
        verifyNoInteractions(housingRepository);
    }

    @Test
    void importAvailableHousing_WithGraphQLClientException_ShouldPropagateAsHousingImportException() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString()))
                .thenThrow(new RuntimeException("GraphQL connection error"));

        assertThatThrownBy(() -> importTask.importAvailableHousing())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Failed to fetch housing ids from GraphQL API")
                .hasCauseInstanceOf(RuntimeException.class);

        verifyNoInteractions(graphQLHousingMapper);
        verifyNoInteractions(housingRepository);
    }

    @Test
    void importAvailableHousing_WithHousingImportException_ShouldPropagateException() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString()))
                .thenThrow(new HousingImportException("API unavailable"));

        assertThatThrownBy(() -> importTask.importAvailableHousing())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Failed to fetch housing ids from GraphQL API");

        verifyNoInteractions(graphQLHousingMapper);
        verifyNoInteractions(housingRepository);
    }

    @Test
    void importAvailableHousing_WithGraphQLMapperException_ShouldPropagateAsHousingImportException() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockGraphQLResponse);
        when(graphQLHousingMapper.mapHousingIds(mockGraphQLResponse))
                .thenThrow(new RuntimeException("Mapping error"));

        assertThatThrownBy(() -> importTask.importAvailableHousing())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Failed to fetch housing ids from GraphQL API")
                .hasCauseInstanceOf(RuntimeException.class);

        verifyNoInteractions(housingRepository);
    }

    @Test
    void importAvailableHousing_WithRepositoryException_ShouldWrapAsUnexpectedException() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockGraphQLResponse);
        when(graphQLHousingMapper.mapHousingIds(mockGraphQLResponse)).thenReturn(availableHousingIds);
        when(housingRepository.bulkUpdateAvailability(any(), anyBoolean()))
                .thenThrow(new RuntimeException("Database connection error"));

        assertThatThrownBy(() -> importTask.importAvailableHousing())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Unexpected error during housing import")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void importAvailableHousing_WithSecondRepositoryCallException_ShouldWrapAsUnexpectedException() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockGraphQLResponse);
        when(graphQLHousingMapper.mapHousingIds(mockGraphQLResponse)).thenReturn(availableHousingIds);
        when(housingRepository.bulkUpdateAvailability(any(), anyBoolean())).thenReturn(3);
        when(housingRepository.markUnavailableNotInIds(any()))
                .thenThrow(new RuntimeException("Database error on second call"));

        assertThatThrownBy(() -> importTask.importAvailableHousing())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Unexpected error during housing import")
                .hasCauseInstanceOf(RuntimeException.class);
    }
}