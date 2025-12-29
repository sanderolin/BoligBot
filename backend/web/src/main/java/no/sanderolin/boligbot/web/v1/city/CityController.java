package no.sanderolin.boligbot.web.v1.city;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import no.sanderolin.boligbot.service.city.CityService;
import no.sanderolin.boligbot.service.district.DistrictService;
import no.sanderolin.boligbot.web.v1.city.mapper.CityModelToDTOMapper;
import no.sanderolin.boligbot.web.v1.city.response.CityDTO;
import no.sanderolin.boligbot.web.v1.common.exception.NotFoundException;
import no.sanderolin.boligbot.web.v1.district.mapper.DistrictModelToDTOMapper;
import no.sanderolin.boligbot.web.v1.district.response.DistrictDTO;
import org.hibernate.ObjectNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;
    private final DistrictService districtService;

    @Operation(
            summary = "Get all cities",
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
                        .map(CityModelToDTOMapper::toDTO)
                        .toList()
        );
    }

    @Operation(
            summary = "Get districts by city id",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            useReturnTypeSchema = true
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "City with the given ID was not found."
                    )
            }
    )
    @GetMapping("/{cityId}/districts")
    public ResponseEntity<List<DistrictDTO>> getDistrictsByCityId(@PathVariable long cityId) {
        try {
            return ResponseEntity.ok(
                    districtService.getDistrictsByCityId(cityId)
                            .stream()
                            .map(DistrictModelToDTOMapper::toDTO)
                            .toList()
            );
        } catch (ObjectNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        }
    }
}
