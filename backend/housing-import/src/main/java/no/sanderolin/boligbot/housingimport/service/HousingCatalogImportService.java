package no.sanderolin.boligbot.housingimport.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.sanderolin.boligbot.dao.model.CityModel;
import no.sanderolin.boligbot.dao.model.DistrictModel;
import no.sanderolin.boligbot.dao.model.HousingModel;
import no.sanderolin.boligbot.dao.model.HousingTypeModel;
import no.sanderolin.boligbot.dao.repository.CityRepository;
import no.sanderolin.boligbot.dao.repository.DistrictRepository;
import no.sanderolin.boligbot.dao.repository.HousingRepository;
import no.sanderolin.boligbot.dao.repository.HousingTypeRepository;
import no.sanderolin.boligbot.housingimport.dto.HousingDTO;
import no.sanderolin.boligbot.housingimport.exception.HousingImportException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HousingCatalogImportService {

    private final HousingCatalogFetcher catalogFetcher;

    private final HousingRepository housingRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final HousingTypeRepository housingTypeRepository;

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
            List<HousingDTO> importedHousingDTOs = catalogFetcher.fetchHousingsFromGraphQL();

            if (importedHousingDTOs.isEmpty()) {
                long durationMs = Duration.between(taskStartTime, Instant.now()).toMillis();
                log.info("No housing items found to import [durationMs={}]", durationMs);
                return;
            }

            Map<String, CityModel> citiesByName = upsertCity(importedHousingDTOs, taskStartTime);
            Map<DistrictKey, DistrictModel> districtsByDistrictKey = upsertDistrict(importedHousingDTOs, citiesByName, taskStartTime);
            Map<String, HousingTypeModel> housingTypesByName = upsertHousingType(importedHousingDTOs, taskStartTime);

            CatalogImportResult result = upsertHousing(importedHousingDTOs, citiesByName, districtsByDistrictKey, housingTypesByName, taskStartTime);

            long durationMs = Duration.between(taskStartTime, Instant.now()).toMillis();
            log.info(
                    "Catalog import finished [durationMs={}, fetched={}, created={}, updated={}, unchanged={}]",
                    durationMs, result.fetched(), result.created(), result.updated(), result.unchanged()
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

    private Map<String, CityModel> upsertCity(List<HousingDTO> importedHousingDTOs, Instant taskStartTime) {
        Set<String> importedCityNames = importedHousingDTOs
                .stream()
                .map(HousingDTO::cityName)
                .collect(Collectors.toSet());

        Map<String, CityModel> citiesByName = cityRepository.findAllByNameIn(importedCityNames)
                .stream()
                .collect(Collectors.toMap(CityModel::getName, Function.identity()));

        List<CityModel> toCreate = new ArrayList<>();

        for (String cityName : importedCityNames) {
            CityModel existing = citiesByName.get(cityName);

            if (existing == null) {
                CityModel newCity = new CityModel();
                newCity.setName(cityName);
                newCity.setCreatedAt(taskStartTime);
                newCity.setLastModifiedAt(taskStartTime);
                newCity.setLastImportedAt(taskStartTime);
                toCreate.add(newCity);
            } else {
                existing.setLastImportedAt(taskStartTime);
            }
        }
        if (!toCreate.isEmpty()) {
            cityRepository.saveAll(toCreate);
            cityRepository.findAllByNameIn(importedCityNames)
                    .forEach(country -> citiesByName.put(country.getName(), country));
        }

        return citiesByName;
    }

    private Map<DistrictKey, DistrictModel> upsertDistrict(List<HousingDTO> importedHousingDTOs, Map<String, CityModel> citiesByName,
            Instant taskStartTime) {

        Set<DistrictKey> importedKeys = importedHousingDTOs.stream()
                .map(dto -> {
                    Long cityId = citiesByName.get(dto.cityName()).getId();
                    return new DistrictKey(cityId, dto.districtName());
                })
                .collect(Collectors.toSet());

        Set<Long> cityIds = importedKeys.stream()
                .map(DistrictKey::cityId)
                .collect(Collectors.toSet());

        Map<DistrictKey, DistrictModel> districtsByDistrictKey = districtRepository.findAllByCityIdIn(cityIds)
                .stream()
                .collect(Collectors.toMap(
                        d -> new DistrictKey(d.getCity().getId(), d.getName()),
                        Function.identity()
                ));

        List<DistrictModel> toCreate = new ArrayList<>();

        for (DistrictKey key : importedKeys) {
            DistrictModel d = districtsByDistrictKey.get(key);
            if (d == null) {
                DistrictModel created = new DistrictModel();
                created.setName(key.districtName());
                created.setCity(citiesByName.values().stream()
                        .filter(c -> c.getId().equals(key.cityId()))
                        .findFirst()
                        .orElseThrow());
                created.setCreatedAt(taskStartTime);
                created.setLastModifiedAt(taskStartTime);
                created.setLastImportedAt(taskStartTime);
                toCreate.add(created);
            } else {
                d.setLastImportedAt(taskStartTime);
            }
        }

        if (!toCreate.isEmpty()) {
            districtRepository.saveAll(toCreate);
            districtRepository.findAllByCityIdIn(cityIds)
                    .forEach(district -> districtsByDistrictKey.put(
                            new DistrictKey(district.getCity().getId(), district.getName()), district));
        }
        return districtsByDistrictKey;
    }

    private Map<String, HousingTypeModel> upsertHousingType(List<HousingDTO> importedHousingDTOs, Instant taskStartTime) {
        Set<String> importedHousingTypeNames = importedHousingDTOs
                .stream()
                .map(HousingDTO::housingTypeName)
                .collect(Collectors.toSet());

        Map<String, HousingTypeModel> housingTypesByName = housingTypeRepository.findAllByNameIn(importedHousingTypeNames)
                .stream()
                .collect(Collectors.toMap(HousingTypeModel::getName, Function.identity()));

        List<HousingTypeModel> toCreate = new ArrayList<>();

        for (String housingTypeName : importedHousingTypeNames) {
            HousingTypeModel existing = housingTypesByName.get(housingTypeName);

            if (existing == null) {
                HousingTypeModel housingType = new HousingTypeModel();
                housingType.setName(housingTypeName);
                housingType.setCreatedAt(taskStartTime);
                housingType.setLastModifiedAt(taskStartTime);
                housingType.setLastImportedAt(taskStartTime);
                toCreate.add(housingType);
            } else {
                existing.setLastImportedAt(taskStartTime);
            }
        }
        if (!toCreate.isEmpty()) {
            housingTypeRepository.saveAll(toCreate);
            housingTypeRepository.findAllByNameIn(importedHousingTypeNames)
                    .forEach(housingType -> housingTypesByName.put(housingType.getName(), housingType));
        }

        return housingTypesByName;
    }

    private CatalogImportResult upsertHousing(List<HousingDTO> importedHousingDTOs,
                                              Map<String, CityModel> citiesByName,
                                              Map<DistrictKey, DistrictModel> districtsByDistrictKey,
                                              Map<String, HousingTypeModel> housingTypesByName,
                                              Instant taskStartTime) {

        List<String> idsOfImportedHousingEntities = importedHousingDTOs.stream()
                .map(HousingDTO::rentalObjectId)
                .toList();

        Map<String, HousingModel> housingByRentalObjectId = housingRepository
                .findAllByRentalObjectIdIn(idsOfImportedHousingEntities)
                .stream()
                .collect(Collectors.toMap(HousingModel::getRentalObjectId, Function.identity()));

        List<HousingModel> toCreate = new ArrayList<>();
        int updated = 0;
        int unchanged = 0;

        for (HousingDTO housingDTO : importedHousingDTOs) {
            String rentalObjectId = housingDTO.rentalObjectId();
            HousingModel existing = housingByRentalObjectId.get(rentalObjectId);

            HousingTypeModel housingType = housingTypesByName.get(housingDTO.housingTypeName());
            CityModel city = citiesByName.get(housingDTO.cityName());
            DistrictModel district = districtsByDistrictKey.get(new DistrictKey(city.getId(), housingDTO.districtName()));

            if (existing == null) {
                HousingModel created = new HousingModel();
                created.setRentalObjectId(rentalObjectId);
                updateFields(created, housingDTO, housingType, district);
                created.setCreatedAt(taskStartTime);
                created.setLastModifiedAt(taskStartTime);
                created.setLastImportedAt(taskStartTime);
                toCreate.add(created);
            } else {
                existing.setLastImportedAt(taskStartTime);
                if (hasHousingModelChanged(existing, housingDTO)) {
                    updateFields(existing, housingDTO, housingType, district);
                    existing.setLastModifiedAt(taskStartTime);
                    updated++;
                } else {
                    unchanged++;
                }
            }
        }

        if (!toCreate.isEmpty()) housingRepository.saveAll(toCreate);
        return new CatalogImportResult(importedHousingDTOs.size(), toCreate.size(), updated, unchanged);
    }

    private void updateFields(HousingModel existing, HousingDTO housingDTO, HousingTypeModel housingTypeModel, DistrictModel districtModel) {
        existing.setName(housingDTO.name());
        existing.setAddress(housingDTO.address());
        existing.setHousingType(housingTypeModel);
        existing.setDistrict(districtModel);
        existing.setAreaSqm(housingDTO.areaSqm());
        existing.setPricePerMonth(housingDTO.pricePerMonth());
    }

    private boolean hasHousingModelChanged(HousingModel housingModel, HousingDTO housingDTO) {
        return housingModel.getPricePerMonth() != housingDTO.pricePerMonth()
                || !housingModel.getName().equals(housingDTO.name())
                || !housingModel.getAddress().equals(housingDTO.address())
                || !housingModel.getHousingType().getName().equals(housingDTO.housingTypeName())
                || !housingModel.getDistrict().getName().equals(housingDTO.districtName())
                || !housingModel.getCity().getName().equals(housingDTO.cityName())
                || housingModel.getAreaSqm().compareTo(housingDTO.areaSqm()) != 0;
    }

    private record DistrictKey(Long cityId, String districtName) { }
    private record CatalogImportResult(int fetched, int created, int updated, int unchanged) { }
}
