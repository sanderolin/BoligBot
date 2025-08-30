package no.sanderolin.boligbot.housingimport.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.sanderolin.boligbot.dao.repository.HousingRepository;
import no.sanderolin.boligbot.housingimport.dto.HousingAvailabilityDTO;
import no.sanderolin.boligbot.housingimport.exception.HousingImportException;
import no.sanderolin.boligbot.housingimport.util.GraphQLHousingMapper;
import no.sanderolin.boligbot.housingimport.util.SitGraphQLClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HousingAvailabilityImportService {

    private final SitGraphQLClient sitGraphQLClient;
    private final GraphQLHousingMapper graphQLHousingMapper;
    private final HousingRepository housingRepository;

    /**
     * Imports availability from SIT GraphQL and updates:
     *  - availability flag for returned IDs
     *  - availableFromDate for each returned ID
     *  - sets others to unavailable
     */
    @Transactional
    public void runImport() {
        log.info("Starting available housing import process");
        Instant taskStartTime = Instant.now();
        try {
            long totalHousingCount = housingRepository.count();
            if (totalHousingCount == 0) {
                log.warn("No housing records found in database. Availability import requires existing housing data. " +
                        "Please run the housing catalog import first.");
                return;
            }

            List<HousingAvailabilityDTO> importedAvailableHousings = fetchAvailabilityFromGraphQL();
            if (importedAvailableHousings.isEmpty()) {
                log.info("No available housings found to import");
                return;
            }

            log.info("Fetched {} availability entries from API", importedAvailableHousings.size());
            processAvailability(importedAvailableHousings);
        } catch (HousingImportException e) {
            log.error("Failed to import availability data: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during availability import", e);
            throw new HousingImportException("Unexpected error during availability import", e);
        } finally {
            long durationMs = Duration.between(taskStartTime, Instant.now()).toMillis();
            log.info("Availability import completed in {} ms", durationMs);
        }
    }

    private void processAvailability(List<HousingAvailabilityDTO> imported) {
        List<String> ids = imported.stream().map(HousingAvailabilityDTO::rentalObjectId).toList();
        int madeAvailable = housingRepository.bulkUpdateAvailability(ids, true);
        int madeUnavailable = housingRepository.markUnavailableNotInIds(ids);

        int updatedDates = 0;
        for (HousingAvailabilityDTO dto : imported) {
            updatedDates += housingRepository.updateAvailableFromDate(dto.rentalObjectId(), dto.availableFromDate());
        }

        log.info("Availability updated. Made available: {}, made unavailable: {}, updated availableFromDate for: {}",
                madeAvailable, madeUnavailable, updatedDates);
    }

    @Retryable(
            retryFor = {HousingImportException.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    private List<HousingAvailabilityDTO> fetchAvailabilityFromGraphQL() {
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
            log.error("Failed to fetch availability from GraphQL API", e);
            throw new HousingImportException("Failed to fetch availability from GraphQL API", e);
        }
    }
}
