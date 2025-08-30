package no.sanderolin.boligbot.housingimport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.sanderolin.boligbot.housingimport.service.HousingAvailabilityImportService;
import no.sanderolin.boligbot.housingimport.service.HousingCatalogImportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ImportOnStartupRunner {

    private final HousingCatalogImportService catalogService;
    private final HousingAvailabilityImportService availabilityService;

    @Value("${housing.catalog.import.run-on-startup:true}")
    private boolean runCatalogOnStartup;

    @Value("${housing.availability.import.run-on-startup:true}")
    private boolean runAvailabilityOnStartup;

    @Bean
    public ApplicationRunner housingImportStartupRunner() {
        return args -> {
            if (!runCatalogOnStartup) {
                log.info("Startup: catalog import disabled → availability import skipped.");
                return;
            }

            log.info("Startup: running catalog import…");
            catalogService.runImport();
            log.info("Startup: catalog import done.");

            if (runAvailabilityOnStartup) {
                log.info("Startup: running availability import (after catalog)…");
                availabilityService.runImport();
                log.info("Startup: availability import done.");
            } else {
                log.info("Startup: availability import disabled.");
            }
        };
    }
}
