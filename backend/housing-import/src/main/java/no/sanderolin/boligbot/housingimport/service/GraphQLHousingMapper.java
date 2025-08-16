package no.sanderolin.boligbot.housingimport.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import no.sanderolin.boligbot.dao.model.HousingModel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GraphQLHousingMapper {

    private final ObjectMapper objectMapper;

    /**
     * Maps the JSON response from the GraphQL query to a list of HousingModel objects.
     * @param jsonResponse The JSON response string from the GraphQL query.
     * @return A list of HousingModel objects populated with data from the JSON response.
     * @throws Exception If there is an error parsing the JSON response.
     */
    public List<HousingModel> map(String jsonResponse) throws Exception {
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode items = root.path("data").path("sanity_allEnhet");

        List<HousingModel> result = new ArrayList<>();

        for (JsonNode item : items) {
            HousingModel model = new HousingModel();
            model.setRentalObjectId(item.path("rentalObjectId").asText());
            model.setName(item.path("name").asText());
            model.setAddress(item.path("building").path("address").asText());
            model.setHousingType(item.path("category").path("displayName").path("en").asText());
            model.setCity(item.path("studentby").path("studiested").path("name").asText());
            model.setDistrict(item.path("studentby").path("name").asText());
            model.setAreaSqm(BigDecimal.valueOf(item.path("area").asDouble()));
            model.setPricePerMonth(item.path("price").asInt());
            result.add(model);
        }
        return result;
    }
}
