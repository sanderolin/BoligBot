package no.sanderolin.boligbot.service.housing;

import lombok.Builder;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;

@Builder(setterPrefix = "set")
public record HousingSearchCriteria(
        String rentalObjectId,
        String address,
        String name,
        String housingType,
        String city,
        String district,
        Integer minPricePerMonth,
        Integer maxPricePerMonth,
        BigDecimal minAreaSqm,
        BigDecimal maxAreaSqm,
        Integer page,
        Integer size,
        HousingSortBy sortBy,
        SortDirection sortDirection
) {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private static final HousingSortBy DEFAULT_SORT_BY = HousingSortBy.AVAILABLE_FROM_DATE;
    private static final SortDirection DEFAULT_SORT_DIRECTION = SortDirection.ASC;

    public int pageOrDefault() {
        return page == null ? DEFAULT_PAGE : Math.max(0, page);
    }

    public int sizeOrDefault() {
        int s = (size == null ? DEFAULT_SIZE : size);
        return Math.min(MAX_SIZE, Math.max(1, s));
    }

    public HousingSortBy sortByOrDefault() {
        return (sortBy == null) ? DEFAULT_SORT_BY : sortBy;
    }

    public SortDirection sortDirectionOrDefault() {
        return (sortDirection == null) ? DEFAULT_SORT_DIRECTION : sortDirection;
    }

    public Sort toSpringSort() {
        Sort.Direction dir = sortDirectionOrDefault().toSpring();
        String prop = sortByOrDefault().property();

        Sort.Order primary = new Sort.Order(dir, prop);
        Sort.Order tieBreaker = Sort.Order.asc("rentalObjectId");

        return Sort.by(primary).and(Sort.by(tieBreaker));
    }

    public Integer minPricePerMonthOrNull() {
        return (minPricePerMonth == null || minPricePerMonth <= 0) ? null : minPricePerMonth;
    }
    public Integer maxPricePerMonthOrNull() {
        return (maxPricePerMonth == null || maxPricePerMonth <= 0) ? null : maxPricePerMonth;
    }
    public BigDecimal minAreaOrNull() {
        return (minAreaSqm == null || minAreaSqm.compareTo(BigDecimal.ZERO) <= 0) ? null : minAreaSqm;
    }
    public BigDecimal maxAreaOrNull() {
        return (maxAreaSqm == null || maxAreaSqm.compareTo(BigDecimal.ZERO) <= 0) ? null : maxAreaSqm;
    }
}