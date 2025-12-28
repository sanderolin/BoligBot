package no.sanderolin.boligbot.web.v1.city;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import no.sanderolin.boligbot.service.city.CityService;
import no.sanderolin.boligbot.web.v1.city.mapper.CityModelToDTOConverter;
import no.sanderolin.boligbot.web.v1.city.response.CityDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @Operation(
            summary = "Get all cities",
            description = """
                    Fetch all cities where sit have housings.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            useReturnTypeSchema = true
                    )
            }
    )
    @GetMapping
    public ResponseEntity<List<CityDTO>> getAllCities() {
        return ResponseEntity.ok(
                cityService.getCities()
                        .stream()
                        .map(CityModelToDTOConverter::toDTO)
                        .toList()
        );
    }
}
