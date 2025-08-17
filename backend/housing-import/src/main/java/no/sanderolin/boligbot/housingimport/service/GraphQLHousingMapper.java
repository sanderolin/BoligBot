package no.sanderolin.boligbot.housingimport.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.sanderolin.boligbot.dao.model.HousingModel;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GraphQLHousingMapper {

    private final ObjectMapper objectMapper;

    /**
     * Maps the JSON response from the GraphQL query to a list of HousingModel objects.
     * @param jsonResponse The JSON response string from the GraphQL query.
     * @return A list of HousingModel objects populated with data from the JSON response.
     */
    public List<HousingModel> map(String jsonResponse) {
        validateInput(jsonResponse);

        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            validateJsonStructure(root);

            JsonNode items = root.path("data").path("sanity_allEnhet");
            List<HousingModel> result = new ArrayList<>();
            int processedCount = 0;
            int skippedCount = 0;

            for (JsonNode item : items) {
                try {
                    HousingModel model = mapSingleItem(item);
                    if (model == null) {
                        skippedCount++;
                        continue;
                    }
                    result.add(model);
                    processedCount++;
                } catch (Exception e) {
                    log.warn("Failed to map housing item: {}", item.toString(), e);
                    skippedCount++;
                }
            }
            log.info("Mapped {} housing items successfully, skipped {} items", processedCount, skippedCount);
            return result;
        } catch (JsonProcessingException e) {
            throw new HousingImportException("Failed to parse JSON response from GraphQL API", e);
        } catch (Exception e) {
            throw new HousingImportException("Failed to map GraphQL response to housing models", e);
        }
    }

    private void validateInput(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON response cannot be null or empty");
        }
    }

    private void validateJsonStructure(JsonNode root) {
        if (root == null) {
            throw new HousingImportException("Invalid JSON: root node is null");
        }

        if (!root.has("data")) {
            throw new HousingImportException("Invalid GraphQL response: missing 'data' field");
        }

        JsonNode data = root.path("data");
        if (!data.has("sanity_allEnhet")) {
            throw new HousingImportException("Invalid GraphQL response: missing 'sanity_allEnhet' field");
        }

        if (root.has("errors")) {
            JsonNode errors = root.path("errors");
            throw new HousingImportException("GraphQL response contains errors: " + errors.toString());
        }
    }

    private HousingModel mapSingleItem(JsonNode item) {
        String rentalObjectId = getStringValue(item, "rentalObjectId");
        if (!StringUtils.hasText(rentalObjectId)) {
            log.warn("Skipping item with missing or empty rentalObjectId: {}", item);
            return null;
        }

        HousingModel model = new HousingModel();
        model.setRentalObjectId(rentalObjectId);
        model.setName(getStringValue(item, "name"));
        model.setAddress(getNestedStringValue(item, "building", "address"));
        model.setHousingType(getNestedStringValue(item, "category", "displayName", "en"));
        model.setCity(getNestedStringValue(item, "studentby", "studiested", "name"));
        model.setDistrict(getNestedStringValue(item, "studentby", "name"));

        model.setAreaSqm(getBigDecimalValue(item, "area"));
        model.setPricePerMonth(getIntValue(item, "price"));

        return model;
    }

    private String getStringValue(JsonNode parent, String fieldName) {
        JsonNode field = parent.path(fieldName);
        return field.isMissingNode() || field.isNull() ? null : field.asText();
    }

    private String getNestedStringValue(JsonNode parent, String... fieldPath) {
        JsonNode current = parent;
        for (String field : fieldPath) {
            current = current.path(field);
            if (current.isMissingNode() || current.isNull()) {
                return null;
            }
        }
        return current.asText();
    }

    private BigDecimal getBigDecimalValue(JsonNode parent, String fieldName) {
        JsonNode field = parent.path(fieldName);
        if (field.isMissingNode() || field.isNull()) {
            return null;
        }

        try {
            double value = field.asDouble();
            if (value < 0) {
                log.warn("Negative area value found: {}", value);
                return null;
            }
            return BigDecimal.valueOf(value);
        } catch (Exception e) {
            log.warn("Invalid area value: {}", field.asText());
            return null;
        }
    }

    private int getIntValue(JsonNode parent, String fieldName) {
        JsonNode field = parent.path(fieldName);
        if (field.isMissingNode() || field.isNull()) {
            return 0;
        }

        try {
            int value = field.asInt();
            if (value < 0) {
                log.warn("Negative price value found: {}", value);
                return 0;
            }
            return value;
        } catch (Exception e) {
            log.warn("Invalid price value: {}", field.asText());
            return 0;
        }
    }
}
