package no.sanderolin.boligbot.web.v1.housingType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import no.sanderolin.boligbot.service.housingType.HousingTypeService;
import no.sanderolin.boligbot.web.v1.housingType.mapper.HousingTypeModelToDTOMapper;
import no.sanderolin.boligbot.web.v1.housingType.response.HousingTypeDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/housing-types")
@RequiredArgsConstructor
public class HousingTypeController {

    private final HousingTypeService housingTypeService;

    @Operation(
            summary = "Get all housing types",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            useReturnTypeSchema = true
                    )
            }
    )
    @GetMapping
    public ResponseEntity<List<HousingTypeDTO>> getAllHousingTypes() {
        return ResponseEntity.ok(
                housingTypeService.getAllHousingTypes()
                        .stream()
                        .map(HousingTypeModelToDTOMapper::toDTO)
                        .toList()
        );
    }
}
