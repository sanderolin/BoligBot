package no.sanderolin.boligbot.housingimport.service;

import no.sanderolin.boligbot.dao.model.HousingModel;
import no.sanderolin.boligbot.housingimport.exception.HousingImportException;
import no.sanderolin.boligbot.housingimport.util.GraphQLHousingMapper;
import no.sanderolin.boligbot.housingimport.util.SitGraphQLClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void fetchHousingEntitiesFromGraphQL_ShouldCallClientAndMapResponse() {
        String mockResponse = "{\"data\":{\"sanity_allEnhet\":[]}}";
        List<HousingModel> mapped = List.of(new HousingModel(), new HousingModel());

        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockResponse);
        when(graphQLHousingMapper.mapHousingEntities(mockResponse)).thenReturn(mapped);

        List<HousingModel> result = fetcher.fetchHousingEntitiesFromGraphQL();

        assertThat(result).isSameAs(mapped);
        verify(sitGraphQLClient).executeGraphQLQuery(anyString());
        verify(graphQLHousingMapper).mapHousingEntities(mockResponse);
        verifyNoMoreInteractions(sitGraphQLClient, graphQLHousingMapper);
    }

    @Test
    void fetchHousingEntitiesFromGraphQL_WhenClientThrowsHousingImportException_ShouldPropagate() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString()))
                .thenThrow(new HousingImportException("network fail"));

        assertThatThrownBy(() -> fetcher.fetchHousingEntitiesFromGraphQL())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("network fail");

        verify(sitGraphQLClient).executeGraphQLQuery(anyString());
        verifyNoInteractions(graphQLHousingMapper);
    }

    @Test
    void fetchHousingEntitiesFromGraphQL_WhenClientThrowsIllegalArgumentException_ShouldWrap() {
        when(sitGraphQLClient.executeGraphQLQuery(anyString()))
                .thenThrow(new IllegalArgumentException("bad query"));

        assertThatThrownBy(() -> fetcher.fetchHousingEntitiesFromGraphQL())
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

        assertThatThrownBy(() -> fetcher.fetchHousingEntitiesFromGraphQL())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("mapping fail");

        verify(sitGraphQLClient).executeGraphQLQuery(anyString());
        verify(graphQLHousingMapper).mapHousingEntities(mockResponse);
    }

    @Test
    void fetchHousingEntitiesFromGraphQL_WhenMapperThrowsIllegalArgumentException_ShouldWrap() {
        String mockResponse = "{\"data\":{\"sanity_allEnhet\":[]}}";
        when(sitGraphQLClient.executeGraphQLQuery(anyString())).thenReturn(mockResponse);
        when(graphQLHousingMapper.mapHousingEntities(mockResponse))
                .thenThrow(new IllegalArgumentException("bad json"));

        assertThatThrownBy(() -> fetcher.fetchHousingEntitiesFromGraphQL())
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Failed to fetch housing data from external API")
                .hasCauseInstanceOf(IllegalArgumentException.class);

        verify(sitGraphQLClient).executeGraphQLQuery(anyString());
        verify(graphQLHousingMapper).mapHousingEntities(mockResponse);
    }
}