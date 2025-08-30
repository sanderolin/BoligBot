package no.sanderolin.boligbot.service.housings;

import lombok.Builder;
import org.springframework.data.domain.Sort;

import java.util.Set;

@Builder(setterPrefix = "set")
public record HousingSearchCriteria(
        String city,
        String district,
        String housingType,
        Integer page,
        Integer size,
        String sortBy,
        String sortDirection
) {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private static final String DEFAULT_SORT_BY = "availableFromDate";
    private static final Sort.Direction DEFAULT_SORT_DIRECTION = Sort.Direction.ASC;
    private static final Set<String> SORT_WHITELIST = Set.of(DEFAULT_SORT_BY, "pricePerMonth", "name", "areaSqm");

    public int pageOrDefault() {
        return page == null ? DEFAULT_PAGE : Math.max(0, page);
    }

    public int sizeOrDefault() {
        int s = (size == null ? DEFAULT_SIZE : size);
        return Math.min(MAX_SIZE, Math.max(1, s));
    }

    public String sortByOrDefault() {
        if (sortBy == null || sortBy.isBlank()) {
            return DEFAULT_SORT_BY;
        }
        String prop = sortBy.trim();
        if (!SORT_WHITELIST.contains(prop)) {
            return DEFAULT_SORT_BY;
        }
        return prop;
    }

    public Sort.Direction sortDirectionOrDefault() {
        if (sortDirection == null || sortDirection.isBlank()) {
            return DEFAULT_SORT_DIRECTION;
        }
        String direction = sortDirection.trim().toLowerCase();
        if ("desc".equals(direction)) {
            return Sort.Direction.DESC;
        }
        return Sort.Direction.ASC;
    }

    public Sort toSpringSort() {
        Sort.Direction dir = sortDirectionOrDefault();
        Sort.Order primary = new Sort.Order(dir, sortByOrDefault());
        Sort.Order tieBreaker = Sort.Order.asc("rentalObjectId");
        return Sort.by(primary).and(Sort.by(tieBreaker));
    }
}