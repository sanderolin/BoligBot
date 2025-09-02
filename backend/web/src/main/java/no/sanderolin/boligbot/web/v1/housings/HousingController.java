package no.sanderolin.boligbot.web.v1.housings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.sanderolin.boligbot.service.housings.HousingSearchCriteria;
import no.sanderolin.boligbot.service.housings.HousingService;
import no.sanderolin.boligbot.web.v1.common.response.PageResponse;
import no.sanderolin.boligbot.web.v1.housings.response.HousingDTO;
import org.hibernate.ObjectNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/housings")
@RequiredArgsConstructor
public class HousingController {

    private final HousingService housingService;

    @GetMapping
    public ResponseEntity<PageResponse<HousingDTO>> searchHousings(
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "district", required = false) String district,
            @RequestParam(name = "housingType", required = false) String housingType,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "sortDirection", required = false) String sortDirection
    ) {
        return ResponseEntity.ok(
                PageResponse.of(
                        housingService.searchHousings(
                                HousingSearchCriteria.builder()
                                        .setCity(city)
                                        .setDistrict(district)
                                        .setHousingType(housingType)
                                        .setPage(page)
                                        .setSize(size)
                                        .setSortBy(sortBy)
                                        .setSortDirection(sortDirection)
                                        .build()
                        ),
                        HousingDTO::createFromModel
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<HousingDTO> getHousingById(@PathVariable(name = "id") String id) {
        try {
            HousingDTO housingDTO = HousingDTO.createFromModel(housingService.getHousingById(id));
            return ResponseEntity.ok(housingDTO);
        } catch (ObjectNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}