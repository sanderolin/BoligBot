package no.sanderolin.boligbot.web.v1.housings;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.sanderolin.boligbot.service.housings.HousingSearchCriteria;
import no.sanderolin.boligbot.service.housings.HousingService;
import no.sanderolin.boligbot.web.v1.common.exception.NotFoundException;
import no.sanderolin.boligbot.web.v1.common.response.PagedResponse;
import no.sanderolin.boligbot.web.v1.housings.converters.HousingModelToDTOConverter;
import no.sanderolin.boligbot.web.v1.housings.converters.HousingSearchRequestToCriteriaConverter;
import no.sanderolin.boligbot.web.v1.housings.request.HousingSearchRequest;
import no.sanderolin.boligbot.web.v1.housings.response.HousingDTO;
import org.hibernate.ObjectNotFoundException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/housings")
@RequiredArgsConstructor
public class HousingController {

    private final HousingService housingService;

    @Operation(
            summary = "Search housings",
            description = """
                    Search for housings based on various criteria. All parameters are optional.
                    If no parameters are provided, all housings will be returned (paginated).
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            useReturnTypeSchema = true
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request parameters",
                            content = @Content(
                                    mediaType = "application/problem+json",
                                    schema = @Schema(implementation = ProblemDetail.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "Invalid sortBy",
                                                    value = """
                                                    {
                                                      "type": "urn:boligbot:problem:bad-request",
                                                      "title": "Bad Request",
                                                      "status": 400,
                                                      "detail": "Invalid sortBy. Allowed: [availableFromDate, pricePerMonth, areaSqm, city, district]",
                                                      "instance": "/api/v1/housings"
                                                    }"""
                                            ),
                                            @ExampleObject(
                                                    name = "Reversed price range",
                                                    value = """
                                                    {
                                                      "type": "urn:boligbot:problem:bad-request",
                                                      "title": "Bad Request",
                                                      "status": 400,
                                                      "detail": "minPricePerMonth cannot be greater than maxPricePerMonth",
                                                      "instance": "/api/v1/housings"
                                                    }"""
                                            )
                                    }
                            )
                    )
            }
    )
    @GetMapping
    public ResponseEntity<PagedResponse<HousingDTO>> searchHousings(
            @Valid @ParameterObject HousingSearchRequest request) {
        HousingSearchCriteria criteria = HousingSearchRequestToCriteriaConverter.toCriteria(request);

        return ResponseEntity.ok(
                PagedResponse.of(
                        housingService.searchHousings(criteria),
                        HousingModelToDTOConverter::toDTO
                )
        );
    }

    @GetMapping("/{rentalObjectId}")
    public ResponseEntity<HousingDTO> getHousingById(@PathVariable(name = "rentalObjectId") String rentalObjectId) {
        try {
            HousingDTO housingDTO = HousingModelToDTOConverter.toDTO(housingService.getHousingByRentalObjectId(rentalObjectId));
            return ResponseEntity.ok(housingDTO);
        } catch (ObjectNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        }
    }
}