package no.sanderolin.boligbot.housingimport.service;

import no.sanderolin.boligbot.housingimport.dto.HousingAvailabilityDTO;
import no.sanderolin.boligbot.housingimport.exception.HousingImportException;
import no.sanderolin.boligbot.housingimport.util.GraphQLHousingMapper;
import no.sanderolin.boligbot.housingimport.util.SitGraphQLClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HousingAvailabilityFetcherTest {

    @Mock private SitGraphQLClient sitGraphQLClient;
    @Mock private GraphQLHousingMapper graphQLHousingMapper;
    @InjectMocks private HousingAvailabilityFetcher fetcher;

    @Test
    void fetchAvailabilityFromGraphQL_ShouldCallClientAndMapResponse() {
        String mockResponse = "{\"data\":{\"housings\":{\"housingRentalObjects\":[]}}}";
        List<HousingAvailabilityDTO> mapped = List.of(
                new HousingAvailabilityDTO("1", LocalDate.of(2025, 12, 1)),
                new HousingAvailabilityDTO("2", null)
        );

        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockResponse);
        when(graphQLHousingMapper.mapHousingAvailability(mockResponse)).thenReturn(mapped);

        List<HousingAvailabilityDTO> result = fetcher.fetchAvailabilityFromGraphQL();

        assertThat(result).isSameAs(mapped);
        verify(sitGraphQLClient).executeGraphQLQuery(anyString());
        verify(graphQLHousingMapper).mapHousingAvailability(mockResponse);
        verifyNoMoreInteractions(sitGraphQLClient, graphQLHousingMapper);
    }

    @Test
    void fetchAvailabilityFromGraphQL_WhenClientThrowsHousingImportException_ShouldPropagate() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString()))
                .thenThrow(new HousingImportException("network fail"));

        assertThatThrownBy(() -> fetcher.fetchAvailabilityFromGraphQL())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("network fail");

        verify(sitGraphQLClient).executeGraphQLQuery(anyString());
        verifyNoInteractions(graphQLHousingMapper);
    }

    @Test
    void fetchAvailabilityFromGraphQL_WhenClientThrowsIllegalArgumentException_ShouldWrap() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString()))
                .thenThrow(new IllegalArgumentException("bad query"));

        assertThatThrownBy(() -> fetcher.fetchAvailabilityFromGraphQL())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Failed to fetch availability from GraphQL API")
                .hasCauseInstanceOf(IllegalArgumentException.class);

        verify(sitGraphQLClient).executeGraphQLQuery(anyString());
        verifyNoInteractions(graphQLHousingMapper);
    }

    @Test
    void fetchAvailabilityFromGraphQL_WhenMapperThrowsHousingImportException_ShouldPropagate() {
        String mockResponse = "{\"data\":{\"housings\":{\"housingRentalObjects\":[]}}}";

        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockResponse);
        when(graphQLHousingMapper.mapHousingAvailability(mockResponse))
                .thenThrow(new HousingImportException("mapping fail"));

        assertThatThrownBy(() -> fetcher.fetchAvailabilityFromGraphQL())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("mapping fail");

        verify(sitGraphQLClient).executeGraphQLQuery(anyString());
        verify(graphQLHousingMapper).mapHousingAvailability(mockResponse);
    }

    @Test
    void fetchAvailabilityFromGraphQL_WhenMapperThrowsIllegalArgumentException_ShouldWrap() {
        String mockResponse = "{\"data\":{\"housings\":{\"housingRentalObjects\":[]}}}";

        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockResponse);
        when(graphQLHousingMapper.mapHousingAvailability(mockResponse))
                .thenThrow(new IllegalArgumentException("bad json"));

        assertThatThrownBy(() -> fetcher.fetchAvailabilityFromGraphQL())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Failed to fetch availability from GraphQL API")
                .hasCauseInstanceOf(IllegalArgumentException.class);

        verify(sitGraphQLClient).executeGraphQLQuery(anyString());
        verify(graphQLHousingMapper).mapHousingAvailability(mockResponse);
    }
}
