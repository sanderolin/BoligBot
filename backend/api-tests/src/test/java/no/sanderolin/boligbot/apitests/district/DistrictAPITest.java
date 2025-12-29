package no.sanderolin.boligbot.apitests.district;

import no.sanderolin.boligbot.apitests.AbstractAPITest;
import no.sanderolin.boligbot.app.BackendApplication;
import no.sanderolin.boligbot.dao.model.CityModel;
import no.sanderolin.boligbot.dao.model.DistrictModel;
import no.sanderolin.boligbot.dao.repository.CityRepository;
import no.sanderolin.boligbot.dao.repository.DistrictRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        classes = BackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DistrictAPITest extends AbstractAPITest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CityRepository cityRepository;
    @Autowired private DistrictRepository districtRepository;
    private DistrictModel moholt;
    private DistrictModel singsaker;
    private DistrictModel sentrum;
    private DistrictModel kallerud;
    private static final Instant now = Instant.now();

    @BeforeEach
    void setup() {
        CityModel trondheim = createCityModel("Trondheim");
        CityModel gjoevik = createCityModel("Gjøvik");
        moholt = createDistrictModel("Moholt", trondheim);
        singsaker = createDistrictModel("Singsaker", trondheim);
        sentrum = createDistrictModel("Sentrum", gjoevik);
        kallerud = createDistrictModel("Kallerud", gjoevik);
    }

    @AfterEach
    void teardown() {
        districtRepository.deleteAll();
        cityRepository.deleteAll();
    }

    @Test
    void getAllDistricts_shouldReturnAllDistricts() throws Exception {
        mockMvc.perform(get("/api/v1/districts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[*].name",
                        containsInAnyOrder(
                                moholt.getName(),
                                singsaker.getName(),
                                sentrum.getName(),
                                kallerud.getName()
                        )
                ));
    }

    private CityModel createCityModel(String city) {
        CityModel cityModel = new CityModel();
        cityModel.setName(city);
        cityModel.setCreatedAt(now);
        cityModel.setLastModifiedAt(now);
        cityModel.setLastImportedAt(now);
        return cityRepository.save(cityModel);
    }

    private DistrictModel createDistrictModel(String district, CityModel cityModel) {
        DistrictModel districtModel = new DistrictModel();
        districtModel.setName(district);
        districtModel.setCity(cityModel);
        districtModel.setCreatedAt(now);
        districtModel.setLastModifiedAt(now);
        districtModel.setLastImportedAt(now);
        return districtRepository.save(districtModel);
    }
}
