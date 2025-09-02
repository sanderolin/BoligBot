package no.sanderolin.boligbot.web.v1.housings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.sanderolin.boligbot.service.housings.HousingSearchCriteria;
import no.sanderolin.boligbot.service.housings.HousingService;
import no.sanderolin.boligbot.web.v1.common.response.PageResponse;
import no.sanderolin.boligbot.web.v1.housings.response.HousingDTO;
import org.hibernate.ObjectNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/v1/housings")
@RequiredArgsConstructor
public class HousingController {

    private final HousingService housingService;

    @GetMapping
    public ResponseEntity<?> searchHousings(
            @RequestParam(name = "rentalObjectId", required = false) String rentalObjectId,
            @RequestParam(name = "address", required = false) String address,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "housingType", required = false) String housingType,
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "district", required = false) String district,
            @RequestParam(name = "minPricePerMonth", required = false) Integer minPricePerMonth,
            @RequestParam(name = "maxPricePerMonth", required = false) Integer maxPricePerMonth,
            @RequestParam(name = "minAreaSqm", required = false) BigDecimal minAreaSqm,
            @RequestParam(name = "maxAreaSqm", required = false) BigDecimal maxAreaSqm,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "sortDirection", required = false) String sortDirection
    ) {
        HousingSearchCriteria criteria = HousingSearchCriteria.builder()
                .setRentalObjectId(rentalObjectId)
                .setAddress(address)
                .setName(name)
                .setHousingType(housingType)
                .setCity(city)
                .setDistrict(district)
                .setMinPricePerMonth(minPricePerMonth)
                .setMaxPricePerMonth(maxPricePerMonth)
                .setMinAreaSqm(minAreaSqm)
                .setMaxAreaSqm(maxAreaSqm)
                .setPage(page)
                .setSize(size)
                .setSortBy(sortBy)
                .setSortDirection(sortDirection)
                .build();

        Integer normMinPrice = criteria.minPricePerMonthOrNull();
        Integer normMaxPrice = criteria.maxPricePerMonthOrNull();
        BigDecimal normMinArea = criteria.minAreaOrNull();
        BigDecimal normMaxArea = criteria.maxAreaOrNull();

        if (normMinPrice != null && normMaxPrice != null && normMinPrice > normMaxPrice) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST,
                    "minPricePerMonth cannot be greater than maxPricePerMonth"
            );
            pd.setProperty("minPricePerMonth", normMinPrice);
            pd.setProperty("maxPricePerMonth", normMaxPrice);
            return ResponseEntity.badRequest().body(pd);
        }
        if (normMinArea != null && normMaxArea != null && normMinArea.compareTo(normMaxArea) > 0) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST,
                    "minAreaSqm cannot be greater than maxAreaSqm"
            );
            pd.setProperty("minAreaSqm", normMinArea);
            pd.setProperty("maxAreaSqm", normMaxArea);
            return ResponseEntity.badRequest().body(pd);
        }
        return ResponseEntity.ok(
                PageResponse.of(
                        housingService.searchHousings(criteria),
                        HousingDTO::createFromModel
                )
        );
    }

    @GetMapping("/{rentalObjectId}")
    public ResponseEntity<HousingDTO> getHousingById(@PathVariable(name = "rentalObjectId") String rentalObjectId) {
        try {
            HousingDTO housingDTO = HousingDTO.createFromModel(housingService.getHousingByRentalObjectId(rentalObjectId));
            return ResponseEntity.ok(housingDTO);
        } catch (ObjectNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}