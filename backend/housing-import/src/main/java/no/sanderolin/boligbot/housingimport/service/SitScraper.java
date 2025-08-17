package no.sanderolin.boligbot.housingimport.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.sanderolin.boligbot.dao.model.HousingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SitScraper {

    @Value("${sit.graphql.url}")
    private String sitGraphqlUrl;

    private final GraphQLHousingMapper graphQLHousingMapper;
    private RestClient restClient;

    @PostConstruct
    public void initializeRestClient() {
        this.restClient = RestClient.builder()
                .baseUrl(sitGraphqlUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "BoligBot/1.0")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
        log.info("Initialized SIT scraper with URL: {}", sitGraphqlUrl);
    }

    public List<HousingModel> scrapeHousingEntities(String query) {
        validateQuery(query);
        try {
            String response = restClient
                    .post()
                    .body(query)
                    .retrieve()
                    .body(String.class);

            validateResponse(response);

            return graphQLHousingMapper.map(response);
        } catch (RestClientException e) {
            log.error("Network error while calling SIT GraphQL API: {}", e.getMessage());
            throw new HousingImportException("Failed to communicate with SIT GraphQL API", e);
        } catch (Exception e) {
            log.error("Unexpected error during scraping: {}", e.getMessage());
            throw new HousingImportException("Failed to scrape housing data", e);
        }
    }

    private void validateQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("GraphQL query cannot be null or empty");
        }
    }

    private void validateResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new HousingImportException("Received empty response from SIT GraphQL API");
        }

        if (response.contains("\"errors\"")) {
            log.warn("GraphQL response contains errors: {}", response);
            throw new HousingImportException("GraphQL API returned errors: " + response);
        }
    }
}
