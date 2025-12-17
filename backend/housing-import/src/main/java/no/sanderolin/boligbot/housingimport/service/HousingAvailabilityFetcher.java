package no.sanderolin.boligbot.housingimport.service;

import lombok.RequiredArgsConstructor;
import no.sanderolin.boligbot.housingimport.dto.HousingAvailabilityDTO;
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
public class HousingAvailabilityFetcher {

    private final SitGraphQLClient sitGraphQLClient;
    private final GraphQLHousingMapper graphQLHousingMapper;


    @Retryable(
            retryFor = {HousingImportException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public List<HousingAvailabilityDTO> fetchAvailabilityFromGraphQL() {
        String getHousingEntitiesQuery = """
            {
              "operationName": "GetHousingIds",
              "variables": {
                "input": {
                  "showUnavailable": false,
                  "offset": 0
                }
              },
              "query": "query GetHousingIds($input: GetHousingsInput!) { housings(filter: $input) { housingRentalObjects { rentalObjectId, availableFrom } } }"
            }
            """;
        try {
            String response = sitGraphQLClient.executeGraphQLQuery(getHousingEntitiesQuery);
            return graphQLHousingMapper.mapHousingAvailability(response);
        } catch (Exception e) {
            throw (e instanceof HousingImportException ex)
                    ? ex
                    : new HousingImportException("Failed to fetch availability from GraphQL API", e);
        }
    }

    @Recover
    public List<HousingAvailabilityDTO> recover(Exception e) {
        throw new HousingImportException("Availability fetch failed after retries", e);
    }
}
