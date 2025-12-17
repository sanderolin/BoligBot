package no.sanderolin.boligbot.housingimport.util;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import no.sanderolin.boligbot.housingimport.exception.HousingImportException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class SitGraphQLClient {

    @Value("${sit.graphql.url}")
    private String sitGraphQLURL;

    private RestClient restClient;

    @PostConstruct
    public void initializeRestClient() {
        this.restClient = RestClient.builder()
                .baseUrl(sitGraphQLURL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "BoligBot/1.0")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String executeGraphQLQuery(String query) {
        validateQuery(query);
        try {
            String response = restClient
                    .post()
                    .body(query)
                    .retrieve()
                    .body(String.class);

            validateResponse(response);

            return response;
        } catch (RestClientException e) {
            throw new HousingImportException("SIT GraphQL call failed", e);
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

        if (response.contains("errors")) {
            throw new HousingImportException("GraphQL API returned errors: " + response);
        }
    }
}
