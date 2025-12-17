package no.sanderolin.boligbot.housingimport.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.sanderolin.boligbot.dao.model.HousingModel;
import no.sanderolin.boligbot.dao.repository.HousingRepository;
import no.sanderolin.boligbot.housingimport.exception.HousingImportException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HousingCatalogImportService {

    private final HousingCatalogFetcher catalogFetcher;
    private final HousingRepository housingRepository;
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Task to import all housing items from the SIT GraphQL API.
     */
    @Transactional
    public void runImport() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Catalog import skipped because a previous run is still in progress");
            return;
        }
        log.info("Starting housing import process");
        Instant taskStartTime = Instant.now();
        try {
            List<HousingModel> importedHousingEntities = catalogFetcher.fetchHousingEntitiesFromGraphQL();

            if (importedHousingEntities.isEmpty()) {
                long durationMs = Duration.between(taskStartTime, Instant.now()).toMillis();
                log.info("No housing items found to import [durationMs={}]", durationMs);
                return;
            }

            log.info("Fetched {} housing items from API", importedHousingEntities.size());
            CatalogImportResult result = processHousingEntities(importedHousingEntities, taskStartTime);

            long durationMs = Duration.between(taskStartTime, Instant.now()).toMillis();
            log.info(
                    "Catalog import finished [durationMs={}, fetched={}, created={}, updated={}]",
                    durationMs, result.fetched(), result.created(), result.updated()
            );
        } catch (HousingImportException e) {
            long durationMs = Duration.between(taskStartTime, Instant.now()).toMillis();
            log.error("Catalog import failed [durationMs={}]: {}", durationMs, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            long durationMs = Duration.between(taskStartTime, Instant.now()).toMillis();
            log.error("Catalog import failed unexpectedly [durationMs={}]", durationMs, e);
            throw new HousingImportException("Unexpected error during housing import", e);
        } finally {
            running.set(false);
        }
    }

    private CatalogImportResult processHousingEntities(List<HousingModel> importedHousingEntities, Instant taskStartTime) {
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
                continue;
            }
            existing.setLastImportedAt(taskStartTime);
            if (!existing.catalogEquals(incoming)) {
                updateFields(existing, incoming);
                existing.setLastModifiedAt(taskStartTime);
            }
            toUpdate.add(existing);
        }
        if (!toCreate.isEmpty()) housingRepository.saveAll(toCreate);
        if (!toUpdate.isEmpty()) housingRepository.saveAll(toUpdate);
        return new CatalogImportResult(importedHousingEntities.size(), toCreate.size(), toUpdate.size());
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

    private record CatalogImportResult(int fetched, int created, int updated) { }
}
