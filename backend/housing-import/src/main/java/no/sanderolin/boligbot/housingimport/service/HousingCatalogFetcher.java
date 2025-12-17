package no.sanderolin.boligbot.housingimport.service;

import lombok.RequiredArgsConstructor;
import no.sanderolin.boligbot.dao.model.HousingModel;
import no.sanderolin.boligbot.housingimport.exception.HousingImportException;
import no.sanderolin.boligbot.housingimport.util.GraphQLHousingMapper;
import no.sanderolin.boligbot.housingimport.util.SitGraphQLClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HousingCatalogFetcher {

    private final SitGraphQLClient sitGraphQLClient;
    private final GraphQLHousingMapper graphQLHousingMapper;

    @Retryable(
            retryFor = {HousingImportException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public List<HousingModel> fetchHousingEntitiesFromGraphQL() {
        String getHousingEntitiesQuery = """
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
                "sort": [
                  { "_id": "ASC" }
                ],
                "limit": 0,
                "offset": 0
              },
              "query": "query GetHousingItems($input: Sanity_EnhetFilter, $limit: Int, $offset: Int) { sanity_allEnhet(where: $input, limit: $limit, offset: $offset) { rentalObjectId name building { address } area price category { displayName { no en } } studentby { name studiested { name } } kollektiv { name } } }"
            }
            """;
        try {
            String response = sitGraphQLClient.executeGraphQLQuery(getHousingEntitiesQuery);
            return graphQLHousingMapper.mapHousingEntities(response);
        } catch (Exception e) {
            throw (e instanceof HousingImportException ex)
                    ? ex
                    : new HousingImportException("Failed to fetch housing data from external API", e);
        }
    }

    @Recover
    public List<HousingModel> recover(Exception e) {
        throw new HousingImportException("Catalog fetch failed after retries", e);
    }
}
