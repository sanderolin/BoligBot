package no.sanderolin.boligbot.apitests.housings;

import no.sanderolin.boligbot.apitests.AbstractAPITest;
import no.sanderolin.boligbot.app.BackendApplication;
import no.sanderolin.boligbot.dao.model.HousingModel;
import no.sanderolin.boligbot.dao.repository.HousingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        classes = BackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class HousingAPITest extends AbstractAPITest {

    @Autowired private MockMvc mockMvc;
    @Autowired private HousingRepository housingRepository;
    private List<HousingModel> seededHousingModels;

    @BeforeEach
    void setUp() {
        housingRepository.deleteAll();
        seededHousingModels = List.of(
                createAndSaveTestHousingModel(
                        "1", "Address 1", "Name 1", "1-room apartment",
                        "Trondheim", "Moholt", BigDecimal.valueOf(19.9), 8200,
                        true, LocalDate.of(2025, 10, 1)
                ),
                createAndSaveTestHousingModel(
                        "2", "Address 2", "Name 2", "2-room apartment",
                        "Gjøvik", "Sentrum", BigDecimal.valueOf(36.6), 9358,
                        true, LocalDate.of(2025, 10, 8)
                ),
                createAndSaveTestHousingModel(
                        "3", "Address 3", "Name 3", "1-room apartment",
                        "Trondheim", "Moholt", BigDecimal.valueOf(16), 8000,
                        false, null
                ),
                createAndSaveTestHousingModel(
                        "4", "Address 4", "Name 4", "Dorm in collective",
                        "Ålesund", "Sørnesvågen", BigDecimal.valueOf(10.6), 5501,
                        true, LocalDate.of(2025, 11, 1)
                ),
                createAndSaveTestHousingModel(
                        "5", "Address 5", "Name 5", "Dorm in collective",
                        "Trondheim", "Singsaker", BigDecimal.valueOf(10.2), 5233,
                        false, null)
        );
    }

    @Test
    void testGetHousingById_Found() throws Exception {
        HousingModel expectedHousing = seededHousingModels.stream()
                .filter(h -> h.getRentalObjectId().equals("1")).findFirst().orElseThrow();

        mockMvc.perform(get("/api/v1/housings/{id}", expectedHousing.getRentalObjectId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentalObjectId").value(expectedHousing.getRentalObjectId()))
                .andExpect(jsonPath("$.address").value("Address 1"))
                .andExpect(jsonPath("$.name").value("Name 1"))
                .andExpect(jsonPath("$.housingType").value("1-room apartment"))
                .andExpect(jsonPath("$.city").value("Trondheim"))
                .andExpect(jsonPath("$.district").value("Moholt"))
                .andExpect(jsonPath("$.areaSqm").value(BigDecimal.valueOf(19.9)))
                .andExpect(jsonPath("$.pricePerMonth").value(8200))
                .andExpect(jsonPath("$.isAvailable").value(true))
                .andExpect(jsonPath("$.availableFromDate").value("2025-10-01"));
    }

    @Test
    void testGetHousingById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/housings/{id}", "nonexistent-id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchHousings_NoCriteria_ReturnsAllHousings() throws Exception {
        mockMvc.perform(get("/api/v1/housings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(5)))
                .andExpect(jsonPath("$.items[*].rentalObjectId",
                        containsInAnyOrder(
                                seededHousingModels.getFirst().getRentalObjectId(),
                                seededHousingModels.get(1).getRentalObjectId(),
                                seededHousingModels.get(2).getRentalObjectId(),
                                seededHousingModels.get(3).getRentalObjectId(),
                                seededHousingModels.get(4).getRentalObjectId())))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void testSearchHousings_ByRentalObjectId_ReturnsRelevantHousings() throws Exception {
        mockMvc.perform(get("/api/v1/housings")
                        .param("rentalObjectId", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[*].rentalObjectId",
                        contains(seededHousingModels.get(2).getRentalObjectId())))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void testSearchHousings_ByAddress_ReturnsRelevantHousings() throws Exception {
        mockMvc.perform(get("/api/v1/housings")
                        .param("address", "Address 5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[*].rentalObjectId",
                        contains(seededHousingModels.getLast().getRentalObjectId())))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void testSearchHousings_ByName_ReturnsRelevantHousings() throws Exception {
        mockMvc.perform(get("/api/v1/housings")
                        .param("name", "Name 2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[*].rentalObjectId",
                        contains(seededHousingModels.get(1).getRentalObjectId())))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void testSearchHousings_ByHousingType_ReturnsRelevantHousings() throws Exception {
        mockMvc.perform(get("/api/v1/housings")
                        .param("housingType", "Dorm in collective")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[*].rentalObjectId",
                        containsInAnyOrder(
                                seededHousingModels.get(3).getRentalObjectId(),
                                seededHousingModels.get(4).getRentalObjectId())))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void testSearchHousings_ByCity_ReturnsRelevantHousings() throws Exception {
        mockMvc.perform(get("/api/v1/housings")
                        .param("city", "Trondheim")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andExpect(jsonPath("$.items[*].rentalObjectId",
                        containsInAnyOrder(
                                seededHousingModels.getFirst().getRentalObjectId(),
                                seededHousingModels.get(2).getRentalObjectId(),
                                seededHousingModels.get(4).getRentalObjectId())))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void testSearchHousings_ByCityAndDistrict_ReturnsRelevantHousings() throws Exception {
        mockMvc.perform(get("/api/v1/housings")
                        .param("city", "Trondheim")
                        .param("district", "Moholt")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[*].rentalObjectId",
                        containsInAnyOrder(
                                seededHousingModels.getFirst().getRentalObjectId(),
                                seededHousingModels.get(2).getRentalObjectId())))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void testSearchHousings_ByMinPrice_ReturnsRelevantHousings() throws Exception {
        mockMvc.perform(get("/api/v1/housings")
                        .param("minPricePerMonth", "8000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andExpect(jsonPath("$.items[*].rentalObjectId",
                        containsInAnyOrder(
                                seededHousingModels.getFirst().getRentalObjectId(),
                                seededHousingModels.get(1).getRentalObjectId(),
                                seededHousingModels.get(2).getRentalObjectId())))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void testSearchHousings_ByMaxPrice_ReturnsRelevantHousings() throws Exception {
        mockMvc.perform(get("/api/v1/housings")
                        .param("maxPricePerMonth", "7000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[*].rentalObjectId",
                        containsInAnyOrder(
                                seededHousingModels.get(3).getRentalObjectId(),
                                seededHousingModels.get(4).getRentalObjectId())))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void testSearchHousings_ByMinAndMaxPrice_ReturnsRelevantHousings() throws Exception {
        mockMvc.perform(get("/api/v1/housings")
                        .param("minPricePerMonth", "8001")
                        .param("maxPricePerMonth", "9000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[*].rentalObjectId",
                        contains(seededHousingModels.getFirst().getRentalObjectId())))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void testSearchHousings_ByMinArea_ReturnsRelevantHousings() throws Exception {
        mockMvc.perform(get("/api/v1/housings")
                        .param("minAreaSqm", "17")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[*].rentalObjectId",
                        containsInAnyOrder(
                                seededHousingModels.getFirst().getRentalObjectId(),
                                seededHousingModels.get(1).getRentalObjectId())))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void testSearchHousings_ByMaxArea_ReturnsRelevantHousings() throws Exception {
        mockMvc.perform(get("/api/v1/housings")
                        .param("maxAreaSqm", "16")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andExpect(jsonPath("$.items[*].rentalObjectId",
                        containsInAnyOrder(
                                seededHousingModels.get(2).getRentalObjectId(),
                                seededHousingModels.get(3).getRentalObjectId(),
                                seededHousingModels.get(4).getRentalObjectId())))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void testSearchHousings_ByMinAndMaxArea_ReturnsRelevantHousings() throws Exception {
        mockMvc.perform(get("/api/v1/housings")
                        .param("minAreaSqm", "14")
                        .param("maxAreaSqm", "17")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[*].rentalObjectId",
                        contains(seededHousingModels.get(2).getRentalObjectId())))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void testSearchHousings_WithInvalidMinAndMaxPrice_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/housings")
                        .param("minPricePerMonth", "20")
                        .param("maxPricePerMonth", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSearchHousings_WithInvalidMinAndMaxArea_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/housings")
                        .param("minAreaSqm", "20")
                        .param("maxAreaSqm", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSearchHousings_WithSize_ReturnsRelevantHousings() throws Exception {
        mockMvc.perform(get("/api/v1/housings")
                        .param("size", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    void testSearchHousings_WithSortByAndDirection_ReturnsRelevantHousings() throws Exception {
        mockMvc.perform(get("/api/v1/housings")
                        .param("sortBy", "pricePerMonth")
                        .param("sortDirection", "desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(5)))
                .andExpect(jsonPath("$.items[*].rentalObjectId",
                        containsInRelativeOrder(
                                seededHousingModels.get(1).getRentalObjectId(),
                                seededHousingModels.get(0).getRentalObjectId(),
                                seededHousingModels.get(2).getRentalObjectId(),
                                seededHousingModels.get(3).getRentalObjectId(),
                                seededHousingModels.get(4).getRentalObjectId())))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    private HousingModel createAndSaveTestHousingModel(
            String rentalObjectId,
            String address,
            String name,
            String housingType,
            String city,
            String district,
            BigDecimal areaSqm,
            int pricePerMonth,
            boolean isAvailable,
            LocalDate availableFromDate) {
        HousingModel model = new HousingModel();
        model.setRentalObjectId(rentalObjectId);
        model.setAddress(address);
        model.setName(name);
        model.setHousingType(housingType);
        model.setCity(city);
        model.setDistrict(district);
        model.setAreaSqm(areaSqm);
        model.setPricePerMonth(pricePerMonth);
        model.setAvailable(isAvailable);
        model.setAvailableFromDate(availableFromDate);

        Instant now = Instant.now();
        model.setCreatedAt(now);
        model.setLastModifiedAt(now);
        model.setLastImportedAt(now);
        return housingRepository.save(model);
    }
}
