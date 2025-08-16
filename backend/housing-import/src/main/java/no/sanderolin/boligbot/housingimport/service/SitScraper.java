package no.sanderolin.boligbot.housingimport.service;

import lombok.RequiredArgsConstructor;
import no.sanderolin.boligbot.dao.model.HousingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Iterator;

@Component
@RequiredArgsConstructor
public class SitScraper {

    @Value("${sit.graphql.url}")
    private String SIT_GRAPHQL_URL;

    private final GraphQLHousingMapper graphQLHousingMapper;

    public Iterator<HousingModel> scrapeHousingItems(String query) throws Exception {
        RestClient restClient = RestClient.builder()
                .baseUrl(SIT_GRAPHQL_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        String response = restClient.post().body(query).retrieve().body(String.class);
        return graphQLHousingMapper.map(response);
    }
}
