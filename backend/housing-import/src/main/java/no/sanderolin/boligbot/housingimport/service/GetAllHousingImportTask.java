package no.sanderolin.boligbot.housingimport.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.sanderolin.boligbot.dao.model.HousingModel;
import no.sanderolin.boligbot.dao.repository.HousingRepository;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetAllHousingImportTask {

    private final SitGraphQLClient sitGraphQLClient;
    private final GraphQLHousingMapper graphQLHousingMapper;
    private final HousingRepository housingRepository;

    /**
     * Scheduled task to import all housing items from the SIT GraphQL API.
     * Default: every day at 16:00.
     */
    @Scheduled(cron = "0 0 16 * * *")
    @Transactional
    @PostConstruct
    public void importAllHousing() {
        log.info("Starting housing import process");
        LocalDateTime taskStartTime = LocalDateTime.now();
        try {
            List<HousingModel> importedHousingEntities = fetchHousingEntitiesFromGraphQL();

            if (importedHousingEntities.isEmpty()) {
                log.info("No housing items found to import");
                return;
            }

            log.info("Fetched {} housing items from API", importedHousingEntities.size());
            processHousingEntities(importedHousingEntities, taskStartTime);

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

    private void processHousingEntities(List<HousingModel> importedHousingEntities, LocalDateTime taskStartTime) {
        List<String> idsOfImportedHousingEntities = importedHousingEntities.stream()
                .map(HousingModel::getRentalObjectId)
                .toList();

        Map<String, HousingModel> existingHousingEntities = housingRepository
                .findAllByRentalObjectIdIn(idsOfImportedHousingEntities)
                .stream()
                .collect(Collectors.toMap(HousingModel::getRentalObjectId, Function.identity()));

        List<HousingModel> toCreate = new ArrayList<>();
        List<HousingModel> toUpdate = new ArrayList<>();

        for (HousingModel incoming : importedHousingEntities) {
            HousingModel existing = existingHousingEntities.get(incoming.getRentalObjectId());
            if (existing == null) {
                incoming.setCreatedAt(taskStartTime);
                incoming.setLastModifiedAt(taskStartTime);
                incoming.setLastImportedAt(taskStartTime);
                toCreate.add(incoming);
            } else {
                existing.setLastImportedAt(taskStartTime);
                if (!existing.dataEquals(incoming)) {
                    updateFields(existing, incoming);
                    existing.setLastModifiedAt(taskStartTime);
                    toUpdate.add(existing);
                } else {
                    toUpdate.add(existing);
                }
            }
        }
        if (!toCreate.isEmpty()) housingRepository.saveAll(toCreate);
        if (!toUpdate.isEmpty()) housingRepository.saveAll(toUpdate);
        log.info("Saved {} new and {} updated housing items", toCreate.size(), toUpdate.size());
        log.info("Successfully saved {} total housing items", importedHousingEntities.size());
    }

    private void updateFields(HousingModel existing, HousingModel incoming) {
        existing.setName(incoming.getName());
        existing.setAddress(incoming.getAddress());
        existing.setHousingType(incoming.getHousingType());
        existing.setCity(incoming.getCity());
        existing.setDistrict(incoming.getDistrict());
        existing.setAreaSqm(incoming.getAreaSqm());
        existing.setPricePerMonth(incoming.getPricePerMonth());
    }


    @Retryable(
            retryFor = {HousingImportException.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    private List<HousingModel> fetchHousingEntitiesFromGraphQL() {
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
            String response = sitGraphQLClient.fetchHousingEntitiesResponse(getHousingEntitiesQuery);
            return graphQLHousingMapper.mapHousingEntities(response);
        } catch (Exception e) {
            log.error("Failed to fetch housing entities from GraphQL API", e);
            throw new HousingImportException("Failed to fetch housing data from external API", e);
        }
    }
}
