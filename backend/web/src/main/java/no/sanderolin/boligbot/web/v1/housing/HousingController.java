package no.sanderolin.boligbot.web.v1.housing;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.sanderolin.boligbot.service.housing.HousingSearchCriteria;
import no.sanderolin.boligbot.service.housing.HousingService;
import no.sanderolin.boligbot.web.v1.common.exception.NotFoundException;
import no.sanderolin.boligbot.web.v1.common.response.PagedResponse;
import no.sanderolin.boligbot.web.v1.housing.mapper.HousingModelToDTOMapper;
import no.sanderolin.boligbot.web.v1.housing.mapper.HousingSearchRequestToCriteriaMapper;
import no.sanderolin.boligbot.web.v1.housing.request.HousingSearchRequest;
import no.sanderolin.boligbot.web.v1.housing.response.HousingDTO;
import org.hibernate.ObjectNotFoundException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        HousingSearchCriteria criteria = HousingSearchRequestToCriteriaMapper.toCriteria(request);

        return ResponseEntity.ok(
                PagedResponse.of(
                        housingService.searchHousings(criteria),
                        HousingModelToDTOMapper::toDTO
                )
        );
    }

    @GetMapping("/{rentalObjectId}")
    public ResponseEntity<HousingDTO> getHousingById(@PathVariable(name = "rentalObjectId") String rentalObjectId) {
        try {
            HousingDTO housingDTO = HousingModelToDTOMapper.toDTO(housingService.getHousingByRentalObjectId(rentalObjectId));
            return ResponseEntity.ok(housingDTO);
        } catch (ObjectNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        }
    }
}