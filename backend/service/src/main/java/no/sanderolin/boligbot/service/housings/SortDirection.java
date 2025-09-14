package no.sanderolin.boligbot.service.housings;

import org.springframework.data.domain.Sort;

public enum SortDirection {
    ASC, DESC;

    public Sort.Direction toSpring() {
        return this == ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
    }
}
