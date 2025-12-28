package no.sanderolin.boligbot.web.v1.housing.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import no.sanderolin.boligbot.service.housing.SortDirection;
import no.sanderolin.boligbot.service.housing.HousingSortBy;

import java.math.BigDecimal;

public record HousingSearchRequest(

        @Schema(description = "Filter by exact rental object ID (unique identifier of a housing unit).",
                example = "BER10-101")
        String rentalObjectId,

        @Schema(description = "Filter by address.",
                example = "Berghusvegen 1")
        String address,

        @Schema(description = "Filter by housing name.",
                example = "Hybel 101 H0101")
        String name,

        @Schema(description = "Filter by type of housing.",
                example = "Dorm in collective")
        String housingType,

        @Schema(description = "Filter by city name.",
                example = "Gjøvik")
        String city,

        @Schema(description = "Filter by district within a city.",
                example = "Kallerud")
        String district,

        @Schema(description = "Minimum monthly rent (inclusive).",
                example = "5000")
        @Positive Integer minPricePerMonth,

        @Schema(description = "Maximum monthly rent (inclusive).",
                example = "12000")
        @Positive Integer maxPricePerMonth,

        @Schema(description = "Minimum living area in square meters (inclusive).",
                example = "15.0")
        @DecimalMin("0.0") BigDecimal minAreaSqm,

        @Schema(description = "Maximum living area in square meters (inclusive).",
                example = "50.0")
        @DecimalMin("0.0") BigDecimal maxAreaSqm,

        @Schema(description = "Zero-based page index (0 = first page).",
                example = "0", defaultValue = "0")
        @Min(0) Integer page,

        @Schema(description = "Number of results per page.",
                example = "20", defaultValue = "20", minimum = "1", maximum = "100")
        @Min(1) @Max(100) Integer size,

        @Schema(description = "Field to sort by.",
                example = "availableFromDate",
                defaultValue = "availableFromDate",
                exampleClasses = HousingSortBy.class)
        HousingSortBy sortBy,

        @Schema(description = "Sort direction.",
                example = "asc",
                defaultValue = "asc",
                exampleClasses = SortDirection.class)
        SortDirection sortDirection
) { }
