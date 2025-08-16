package no.sanderolin.boligbot.housingimport.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.sanderolin.boligbot.dao.model.HousingModel;
import no.sanderolin.boligbot.dao.repository.HousingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetAllHousingImportTask {

    private final SitScraper sitScraper;
    private final HousingRepository housingRepository;

    /**
     * Scheduled task to import all housing items from the SIT GraphQL API.
     * This task runs every day at 16:00.
     * It uses a GraphQL query to fetch all housing items, excluding parking spaces.
     * The fetched items are then saved to the database.
     */
    @Scheduled(cron = "0 0 16 * * *")
    @Transactional
    @PostConstruct
    public void importAllHousing() {
        log.info("Starting housing import process");

        LocalDateTime taskStartTime = LocalDateTime.now();
        List<HousingModel> importedHousingEntities = fetchHousingEntitiesFromGraphQL();
        if (importedHousingEntities.isEmpty()) {
            log.info("No housing items found to import");
            return;
        }
        log.info("Fetched {} housing items from API", importedHousingEntities.size());

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
                if (hasHousingEntityChanged(existing, incoming)) {
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
        long durationMs = Duration.between(taskStartTime, LocalDateTime.now()).toMillis();
        log.info("Housing import completed in {} ms", durationMs);
    }

    private boolean hasHousingEntityChanged(HousingModel existing, HousingModel incoming) {
        return !Objects.equals(existing.getName(), incoming.getName())
               || !Objects.equals(existing.getAddress(), incoming.getAddress())
               || !Objects.equals(existing.getHousingType(), incoming.getHousingType())
               || !Objects.equals(existing.getCity(), incoming.getCity())
               || !Objects.equals(existing.getDistrict(), incoming.getDistrict())
               || !areaEquals(existing.getAreaSqm(), incoming.getAreaSqm())
               || existing.getPricePerMonth() != incoming.getPricePerMonth();
    }

    private boolean areaEquals(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) return Objects.equals(a, b);
        return a.compareTo(b) == 0;
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
            return sitScraper.scrapeHousingEntities(getHousingEntitiesQuery);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
