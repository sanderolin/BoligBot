package no.sanderolin.boligbot.web.v1.district;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import no.sanderolin.boligbot.service.district.DistrictService;
import no.sanderolin.boligbot.web.v1.district.mapper.DistrictModelToDTOMapper;
import no.sanderolin.boligbot.web.v1.district.response.DistrictDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/districts")
@RequiredArgsConstructor
public class DistrictController {

    private final DistrictService districtService;

    @Operation(
            summary = "Get all districts",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            useReturnTypeSchema = true
                    )
            }
    )
    @GetMapping
    public ResponseEntity<List<DistrictDTO>> getAllDistricts() {
        return ResponseEntity.ok(
                districtService.getDistricts()
                        .stream()
                        .map(DistrictModelToDTOMapper::toDTO)
                        .toList()
        );
    }
}
