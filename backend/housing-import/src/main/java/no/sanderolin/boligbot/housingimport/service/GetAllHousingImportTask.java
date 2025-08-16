package no.sanderolin.boligbot.housingimport.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.sanderolin.boligbot.dao.model.HousingModel;
import no.sanderolin.boligbot.dao.repository.HousingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        List<HousingModel> housingItems = fetchHousingItemsFromGraphQL();
        if (housingItems.isEmpty()) {
            log.info("No housing items found to import");
            return;
        }
        log.info("Fetched {} housing items from API", housingItems.size());
        housingRepository.saveAllAndFlush(housingItems);
        log.info("Successfully saved {} housing items", housingItems.size());
    }

    private List<HousingModel> fetchHousingItemsFromGraphQL() {
        String getHousingItemsQuery = """
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
            return sitScraper.scrapeHousingItems(getHousingItemsQuery);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
