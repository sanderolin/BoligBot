package no.sanderolin.backend.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class GetAllHousingScraper {

    private final RestClient restClient;

    private static final String GET_ALL_HOUSING_ITEMS_QUERY = """
            {
              "operationName": "GetHousingItems",
              "variables": {
                "input": {
                  "category": {
                    "displayName": {
                      "no": {
                        "neq": "Parkering"
                      }
                    }
                  }
                },
                "limit": 0,
                "offset": 0
              },
              "query": "query GetHousingItems($input: Sanity_EnhetFilter, $limit: Int, $offset: Int) { sanity_allEnhet(where: $input, limit: $limit, offset: $offset) { rentalObjectId name building { address } area price category { displayName { no en } } studentby { name studiested { name } } kollektiv { name } } }"
            }
            """;

    public GetAllHousingScraper(RestClient.Builder builder, @Value("${sit.graphql.url}") String baseUrl) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Scheduled(cron = "0 0 16 * * *")
    public void scrapeHousingItems() {
        try {
            String response = restClient.post().body(GET_ALL_HOUSING_ITEMS_QUERY).retrieve().body(String.class);
            System.out.println(response);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @PostConstruct
    public void runAtStartup() {
        scrapeHousingItems();
    }
}