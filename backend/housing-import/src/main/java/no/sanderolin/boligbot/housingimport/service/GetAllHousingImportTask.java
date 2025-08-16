package no.sanderolin.boligbot.housingimport.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.sanderolin.boligbot.dao.model.HousingModel;
import no.sanderolin.boligbot.dao.repository.HousingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Iterator;

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
     * It compares the fetched items with the existing items in the database and updates the database accordingly.
     * If an item exists in the database but not in the fetched items, it is deleted
     * and if an item exists in the fetched items but not in the database, it is saved.
     * If an item exists in both the fetched items and the database, it is updated.
     */
    @Scheduled(cron = "0 0 16 * * *")
    public void importAllHousing() {
        Iterator<HousingModel> scrapedBatch = fetchHousingItemsFromGraphQL();
        Iterator<HousingModel> dBBatch = housingRepository.findAllOrderByRentalObjectIdAsc().iterator();

        boolean scrapedItemProcessed = true;
        boolean dBItemProcessed = true;
        HousingModel scrapedHousingModel = null;
        HousingModel dBHousingModel = null;
        while (!scrapedBatch.hasNext() && !dBBatch.hasNext()) {

            if (scrapedItemProcessed) {
                scrapedHousingModel = scrapedBatch.next();
                scrapedItemProcessed = false;
            }
            if (dBItemProcessed) {
                dBHousingModel = dBBatch.next();
                dBItemProcessed = false;
            }

            int comparison = scrapedHousingModel.getRentalObjectId().compareTo(dBHousingModel.getRentalObjectId());
            if (comparison < 0) {
                housingRepository.save(scrapedHousingModel);
                scrapedItemProcessed = true;
            } else if (comparison > 0) {
                housingRepository.delete(dBHousingModel);
                dBItemProcessed = true;
            } else {
                housingRepository.save(scrapedHousingModel);
                scrapedItemProcessed = true;
                dBItemProcessed = true;
            }
        }
        if (scrapedHousingModel != null && !scrapedItemProcessed) {
            housingRepository.save(scrapedHousingModel);
        }

        if (dBHousingModel != null && !dBItemProcessed) {
            housingRepository.delete(dBHousingModel);
        }

        if (scrapedBatch.hasNext()) {
            while (scrapedBatch.hasNext()) {
                housingRepository.save(scrapedBatch.next());
            }
        } else if (dBBatch.hasNext()) {
            while (dBBatch.hasNext()) {
                housingRepository.delete(dBBatch.next());
            }
        }

        log.info("All housing items processed");
    }

    private Iterator<HousingModel> fetchHousingItemsFromGraphQL() {
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

    @PostConstruct
    public void runAtStartup() {
        importAllHousing();
    }
}
