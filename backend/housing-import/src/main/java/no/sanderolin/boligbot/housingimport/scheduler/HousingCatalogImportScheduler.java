package no.sanderolin.boligbot.housingimport.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.sanderolin.boligbot.housingimport.service.HousingCatalogImportService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "housing.catalog.import.scheduled.enabled",
        havingValue = "true"
)
public class HousingCatalogImportScheduler {

    private final HousingCatalogImportService housingCatalogImportService;

    /**
     * Scheduled task to import all housing from the SIT GraphQL API.
     * Default: runs at 16:00 every day.
     */
    @Scheduled(cron = "${housing.catalog.import.cron}")
    public void scheduleHousingImport() {
        housingCatalogImportService.runImport();
    }
}
