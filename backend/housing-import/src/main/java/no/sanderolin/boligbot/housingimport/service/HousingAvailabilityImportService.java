package no.sanderolin.boligbot.housingimport.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.sanderolin.boligbot.dao.repository.HousingRepository;
import no.sanderolin.boligbot.housingimport.dto.HousingAvailabilityDTO;
import no.sanderolin.boligbot.housingimport.exception.HousingImportException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class HousingAvailabilityImportService {

    private final HousingAvailabilityFetcher availabilityFetcher;
    private final HousingRepository housingRepository;
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Imports availability from SIT GraphQL and updates:
     *  - availability flag for returned IDs
     *  - availableFromDate for each returned ID
     *  - sets others to unavailable
     */
    @Transactional
    public void runImport() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Availability import skipped because a previous run is still in progress");
            return;
        }
        log.info("Starting available housing import process");
        Instant taskStartTime = Instant.now();
        try {
            long totalHousingCount = housingRepository.count();
            if (totalHousingCount == 0) {
                log.warn("No housing records found in database. Availability import requires existing housing data. " +
                        "Please run the housing catalog import first.");
                return;
            }

            List<HousingAvailabilityDTO> importedAvailableHousings = availabilityFetcher.fetchAvailabilityFromGraphQL();
            if (importedAvailableHousings.isEmpty()) {
                long durationMs = Duration.between(taskStartTime, Instant.now()).toMillis();
                log.info("No available housings found to import [durationMs={}]", durationMs);
                return;
            }

            log.info("Fetched {} availability entries from API", importedAvailableHousings.size());
            AvailabilityImportResult result = processAvailability(importedAvailableHousings);

            long durationMs = Duration.between(taskStartTime, Instant.now()).toMillis();
            log.info(
                    "Availability import finished [durationMs={}, fetched={}, madeAvailable={}, madeUnavailable={}, updatedDates={}]",
                    durationMs, result.fetched(), result.madeAvailable(), result.madeUnavailable(), result.updatedDates()
            );
        } catch (HousingImportException e) {
            long durationMs = Duration.between(taskStartTime, Instant.now()).toMillis();
            log.error("Availability import failed [durationMs={}]: {}", durationMs, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            long durationMs = Duration.between(taskStartTime, Instant.now()).toMillis();
            log.error("Availability import failed unexpectedly [durationMs={}]", durationMs, e);
            throw new HousingImportException("Unexpected error during availability import", e);
        } finally {
            running.set(false);
        }
    }

    private AvailabilityImportResult processAvailability(List<HousingAvailabilityDTO> imported) {
        List<String> ids = imported.stream().map(HousingAvailabilityDTO::rentalObjectId).toList();
        int madeAvailableSinceLastImport = housingRepository.markAvailableIfInIds(ids);
        int madeUnavailableSinceLastImport = housingRepository.markUnavailableIfNotInIds(ids);

        int updatedDates = 0;
        for (HousingAvailabilityDTO dto : imported) {
            updatedDates += housingRepository.updateAvailableFromDate(dto.rentalObjectId(), dto.availableFromDate());
        }

        return new AvailabilityImportResult(imported.size(), madeAvailableSinceLastImport, madeUnavailableSinceLastImport, updatedDates);
    }

    private record AvailabilityImportResult(int fetched, int madeAvailable, int madeUnavailable, int updatedDates) {}
}
