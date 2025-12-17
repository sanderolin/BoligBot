package no.sanderolin.boligbot.housingimport.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.sanderolin.boligbot.housingimport.exception.HousingImportException;
import no.sanderolin.boligbot.housingimport.service.HousingAvailabilityImportService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "housing.availability.import.scheduled.enabled",
        havingValue = "true"
)
public class HousingAvailabilityImportScheduler {

    private final HousingAvailabilityImportService housingAvailabilityImportService;

    /**
     * Scheduled task to import available housing ids from the SIT GraphQL API.
     * Default: every 10 seconds
     */
    @Scheduled(cron = "${housing.availability.import.cron}")
    public void importAvailableHousing() {
        try {
            housingAvailabilityImportService.runImport();
        } catch (HousingImportException exception) {
            log.warn("Scheduled availability import failed");
        }
    }
}
