package no.sanderolin.boligbot.housingimport.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.sanderolin.boligbot.dao.repository.HousingRepository;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetAvailableHousingImportTask {

    private final SitGraphQLClient sitGraphQLClient;
    private final GraphQLHousingMapper graphQLHousingMapper;
    private final HousingRepository housingRepository;

    /**
     * Scheduled task to import available housing ids from the SIT GraphQL API.
     * Default: every 10 seconds
     */
    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void importAvailableHousing() {
        log.info("Starting available housing import process");
        LocalDateTime taskStartTime = LocalDateTime.now();
        try {
            List<String> importedAvailableHousingIds = fetchAvailableHousingIdsFromGraphQL();
            if (importedAvailableHousingIds.isEmpty()) {
                log.info("No available housing ids found to import");
                return;
            }

            log.info("Fetched {} available housing ids from API", importedAvailableHousingIds.size());
            processAvailableHousingIds(importedAvailableHousingIds);
        } catch (HousingImportException e) {
            log.error("Failed to import housing data: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during housing import", e);
            throw new HousingImportException("Unexpected error during housing import", e);
        } finally {
            long durationMs = Duration.between(taskStartTime, LocalDateTime.now()).toMillis();
            log.info("Housing import completed in {} ms", durationMs);
        }
    }

    private void processAvailableHousingIds(List<String> importedAvailableHousingIds) {
        int numberOfAvailableHousing = housingRepository.bulkUpdateAvailability(importedAvailableHousingIds, true);
        int numberOfHousingsNoLongerAvailable = housingRepository.markUnavailableNotInIds(importedAvailableHousingIds);

        log.info("Number of available housing ids: {}", numberOfAvailableHousing);
        log.info("Number of housing ids no longer available: {}", numberOfHousingsNoLongerAvailable);
    }

    @Retryable(
            retryFor = {HousingImportException.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    private List<String> fetchAvailableHousingIdsFromGraphQL() {
        String getHousingEntitiesQuery = """
            {
              "operationName": "GetHousingIds",
              "variables": {
                "input": {
                  "showUnavailable": false,
                  "offset": 0
                }
              },
              "query": "query GetHousingIds($input: GetHousingsInput!) { housings(filter: $input) { housingRentalObjects { rentalObjectId } } }"
            }
            """;
        try {
            String response = sitGraphQLClient.executeGraphQLQuery(getHousingEntitiesQuery);
            return graphQLHousingMapper.mapHousingIds(response);
        } catch (Exception e) {
            log.error("Failed to fetch housing ids from GraphQL API", e);
            throw new HousingImportException("Failed to fetch housing ids from GraphQL API", e);
        }
    }
}
