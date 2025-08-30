package no.sanderolin.boligbot.housingimport.dto;

import java.time.LocalDate;

public record HousingAvailabilityDTO (
        String rentalObjectId,
        LocalDate availableFromDate
) {}
