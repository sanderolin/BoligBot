package no.sanderolin.boligbot.housingimport.service;

import no.sanderolin.boligbot.housingimport.dto.HousingDTO;
import no.sanderolin.boligbot.housingimport.exception.HousingImportException;
import no.sanderolin.boligbot.housingimport.util.GraphQLHousingMapper;
import no.sanderolin.boligbot.housingimport.util.SitGraphQLClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HousingCatalogFetcherTest {

    @Mock private SitGraphQLClient sitGraphQLClient;
    @Mock private GraphQLHousingMapper graphQLHousingMapper;
    @InjectMocks private HousingCatalogFetcher fetcher;

    @Test
    void fetchHousingsFromGraphQL_ShouldCallClientAndMapResponse() {
        String mockResponse = "{\"data\":{\"sanity_allEnhet\":[]}}";
        List<HousingDTO> mapped = List.of(
                new HousingDTO("1", "2", "3", "4",
                "5", "6", new BigDecimal(7), 8),
                new HousingDTO("9", "10", "11", "12",
                        "13", "14", new BigDecimal(15), 16)
                );

        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockResponse);
        when(graphQLHousingMapper.mapHousingEntities(mockResponse)).thenReturn(mapped);

        List<HousingDTO> result = fetcher.fetchHousingsFromGraphQL();

        assertThat(result).isSameAs(mapped);
        verify(sitGraphQLClient).executeGraphQLQuery(anyString());
        verify(graphQLHousingMapper).mapHousingEntities(mockResponse);
        verifyNoMoreInteractions(sitGraphQLClient, graphQLHousingMapper);
    }

    @Test
    void fetchHousingEntitiesFromGraphQL_WhenClientThrowsHousingImportException_ShouldPropagate() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString()))
                .thenThrow(new HousingImportException("network fail"));

        assertThatThrownBy(() -> fetcher.fetchHousingsFromGraphQL())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("network fail");

        verify(sitGraphQLClient).executeGraphQLQuery(anyString());
        verifyNoInteractions(graphQLHousingMapper);
    }

    @Test
    void fetchHousingsFromGraphQL_WhenClientThrowsIllegalArgumentException_ShouldWrap() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString()))
                .thenThrow(new IllegalArgumentException("bad query"));

        assertThatThrownBy(() -> fetcher.fetchHousingsFromGraphQL())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Failed to fetch housing data from external API")
                .hasCauseInstanceOf(IllegalArgumentException.class);

        verify(sitGraphQLClient).executeGraphQLQuery(anyString());
        verifyNoInteractions(graphQLHousingMapper);
    }

    @Test
    void fetchHousingEntitiesFromGraphQL_WhenMapperThrowsHousingImportException_ShouldPropagate() {
        String mockResponse = "{\"data\":{\"sanity_allEnhet\":[]}}";
        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockResponse);
        when(graphQLHousingMapper.mapHousingEntities(mockResponse))
                .thenThrow(new HousingImportException("mapping fail"));

        assertThatThrownBy(() -> fetcher.fetchHousingsFromGraphQL())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("mapping fail");

        verify(sitGraphQLClient).executeGraphQLQuery(anyString());
        verify(graphQLHousingMapper).mapHousingEntities(mockResponse);
    }

    @Test
    void fetchHousingsFromGraphQL_WhenMapperThrowsIllegalArgumentException_ShouldWrap() {
        String mockResponse = "{\"data\":{\"sanity_allEnhet\":[]}}";
        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockResponse);
        when(graphQLHousingMapper.mapHousingEntities(mockResponse))
                .thenThrow(new IllegalArgumentException("bad json"));

        assertThatThrownBy(() -> fetcher.fetchHousingsFromGraphQL())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Failed to fetch housing data from external API")
                .hasCauseInstanceOf(IllegalArgumentException.class);

        verify(sitGraphQLClient).executeGraphQLQuery(anyString());
        verify(graphQLHousingMapper).mapHousingEntities(mockResponse);
    }
}