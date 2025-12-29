package no.sanderolin.boligbot.apitests.housingType;

import no.sanderolin.boligbot.apitests.AbstractAPITest;
import no.sanderolin.boligbot.app.BackendApplication;
import no.sanderolin.boligbot.dao.model.HousingTypeModel;
import no.sanderolin.boligbot.dao.repository.HousingTypeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = BackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class HousingTypeAPITest extends AbstractAPITest {

    @Autowired private MockMvc mockMvc;
    @Autowired private HousingTypeRepository housingTypeRepository;
    private final Instant now = Instant.now();
    private HousingTypeModel oneRoomApartment;
    private HousingTypeModel twoRoomApartment;
    private HousingTypeModel dormInCollective;

    @BeforeEach
    void setup() {
        oneRoomApartment = createHousingType("1-room apartment");
        twoRoomApartment = createHousingType("2-room apartment");
        dormInCollective = createHousingType("Dorm in collective");
    }

    @AfterEach
    void teardown() {
        housingTypeRepository.deleteAll();
    }

    @Test
    void getAllHousingTypes_shouldReturnAllHousingTypes() throws Exception {
        mockMvc.perform(get("/api/v1/housing-types")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].name",
                        containsInAnyOrder(
                                oneRoomApartment.getName(),
                                twoRoomApartment.getName(),
                                dormInCollective.getName()
                        )
                ));
    }

    private HousingTypeModel createHousingType(String name) {
        HousingTypeModel housingTypeModel = new HousingTypeModel();
        housingTypeModel.setName(name);
        housingTypeModel.setCreatedAt(now);
        housingTypeModel.setLastModifiedAt(now);
        housingTypeModel.setLastImportedAt(now);
        return housingTypeRepository.save(housingTypeModel);
    }
}
