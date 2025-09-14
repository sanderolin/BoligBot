package no.sanderolin.boligbot.web.v1.housings.converters;

import no.sanderolin.boligbot.service.housings.HousingSearchCriteria;
import no.sanderolin.boligbot.web.v1.common.exception.BadRequestException;
import no.sanderolin.boligbot.web.v1.housings.request.HousingSearchRequest;

import java.math.BigDecimal;

/**
 * Converts a HousingSearchRequest to a HousingSearchCriteria.
 * Also performs validation of the request parameters.
 */
public class HousingSearchRequestToCriteriaConverter {

    public static HousingSearchCriteria toCriteria(HousingSearchRequest request) throws BadRequestException {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setRentalObjectId(trimToNull(request.rentalObjectId()))
                .setAddress(trimToNull(request.address()))
                .setName(trimToNull(request.name()))
                .setHousingType(trimToNull(request.housingType()))
                .setCity(trimToNull(request.city()))
                .setDistrict(trimToNull(request.district()))
                .setMinPricePerMonth(request.minPricePerMonth())
                .setMaxPricePerMonth(request.maxPricePerMonth())
                .setMinAreaSqm(request.minAreaSqm())
                .setMaxAreaSqm(request.maxAreaSqm())
                .setPage(request.page())
                .setSize(request.size())
                .setSortBy(request.sortBy())
                .setSortDirection(request.sortDirection())
                .build();

        Integer minPrice = criteria.minPricePerMonthOrNull();
        Integer maxPrice = criteria.maxPricePerMonthOrNull();
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new BadRequestException("minPricePerMonth cannot be greater than maxPricePerMonth");
        }

        BigDecimal minArea = criteria.minAreaOrNull();
        BigDecimal maxArea = criteria.maxAreaOrNull();
        if (minArea != null && maxArea != null && minArea.compareTo(maxArea) > 0) {
            throw new BadRequestException("minAreaSqm cannot be greater than maxAreaSqm");
        }

        return criteria;
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
