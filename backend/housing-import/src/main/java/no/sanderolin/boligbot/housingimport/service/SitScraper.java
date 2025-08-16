package no.sanderolin.boligbot.housingimport.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import no.sanderolin.boligbot.dao.model.HousingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SitScraper {

    @Value("${sit.graphql.url}")
    private String SIT_GRAPHQL_URL;

    private final GraphQLHousingMapper graphQLHousingMapper;
    private RestClient restClient;

    @PostConstruct
    public void initializeRestClient() {
        this.restClient = RestClient.builder()
                .baseUrl(SIT_GRAPHQL_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "BoligBot/1.0")
                .build();
    }

    public List<HousingModel> scrapeHousingEntities(String query) throws Exception {
        String response = restClient
                .post()
                .body(query)
                .retrieve()
                .body(String.class);
        if (response == null || response.isEmpty()) {
            throw new RuntimeException("Received empty response from SIT GraphQL API");
        }
        return graphQLHousingMapper.map(response);
    }
}
