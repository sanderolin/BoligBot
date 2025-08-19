package no.sanderolin.boligbot.housingimport.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.sanderolin.boligbot.housingimport.service.HousingCatalogImportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "housing.catalog.import.enabled",
        havingValue = "true"
)
public class HousingCatalogImportScheduler {

    private final HousingCatalogImportService housingCatalogImportService;

    @Value("${housing.catalog.import.run-on-startup}")
    private boolean shouldRunOnStartup;

    /**
     * Run housing import when application starts up (if enabled).
     * Configurable via housing.catalog.import.run-on-startup property.
     */
    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void runImportOnStartup() {
        if (!shouldRunOnStartup) {
            log.info("Startup import disabled via configuration");
            return;
        }
        housingCatalogImportService.runImport();
    }


    /**
     * Scheduled task to import all housing from the SIT GraphQL API.
     * Default: runs at 16:00 every day.
     */
    @Scheduled(cron = "${housing.catalog.import.cron}")
    public void scheduleHousingImport() {
        housingCatalogImportService.runImport();
    }
}
