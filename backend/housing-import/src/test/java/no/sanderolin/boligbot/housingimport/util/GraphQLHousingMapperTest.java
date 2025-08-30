package no.sanderolin.boligbot.housingimport.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.sanderolin.boligbot.dao.model.HousingModel;
import no.sanderolin.boligbot.housingimport.dto.HousingAvailabilityDTO;
import no.sanderolin.boligbot.housingimport.exception.HousingImportException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GraphQLHousingMapperTest {

    private GraphQLHousingMapper graphQLHousingMapper;

    @BeforeEach
    void setUp() {
        graphQLHousingMapper = new GraphQLHousingMapper(new ObjectMapper());
    }

    @Test
    void mapHousingEntities_ValidResponse_ReturnsMappedEntities() {
        String validHousingEntitiesResponse = """
            {
                "data": {
                    "sanity_allEnhet": [
                        {
                            "rentalObjectId": "BER10-101",
                            "name": "Hybel 101 H0101",
                            "building": {
                                "address": "Berghusvegen 10"
                            },
                            "area": 9.5,
                            "price": 5258,
                            "category": {
                                "displayName": {
                                    "no": "Hybel i kollektiv",
                                    "en": "Dorm in collective"
                                }
                            },
                            "studentby": {
                                "name": "Kallerud",
                                "studiested": {
                                    "name": "Gjøvik"
                                }
                            }
                        },
                        {
                            "rentalObjectId": "BER10-102",
                            "name": "Hybel 102 H0102",
                            "building": {
                                "address": "Moholt Allé 10"
                            },
                            "area": 9.1,
                            "price": 6000,
                            "category": {
                                "displayName": {
                                    "no": "2-roms leilighet",
                                    "en": "1-bedroom apartment"
                                }
                            },
                            "studentby": {
                                "name": "Moholt",
                                "studiested": {
                                    "name": "Trondheim"
                                }
                            }
                        }
                    ]
                }
            }
            """;
        List<HousingModel> result = graphQLHousingMapper.mapHousingEntities(validHousingEntitiesResponse);
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertHousingModel(
                result.getFirst(),
                "BER10-101",
                "Hybel 101 H0101",
                "Berghusvegen 10",
                "Dorm in collective",
                "Gjøvik",
                "Kallerud",
                BigDecimal.valueOf(9.5),
                5258
        );
        assertHousingModel(
                result.getLast(),
                "BER10-102",
                "Hybel 102 H0102",
                "Moholt Allé 10",
                "1-bedroom apartment",
                "Trondheim",
                "Moholt",
                BigDecimal.valueOf(9.1),
                6000
        );
    }

    @Test
    void mapHousingEntities_WithNullInput_ShouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> graphQLHousingMapper.mapHousingEntities(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JSON response cannot be null or empty");
    }

    @Test
    void mapHousingEntities_WithEmptyInput_ShouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> graphQLHousingMapper.mapHousingEntities("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JSON response cannot be null or empty");
    }

    @Test
    void mapHousingEntities_WithInvalidJson_ShouldThrowHousingImportException() {
        assertThatThrownBy(() -> graphQLHousingMapper.mapHousingEntities("invalid json"))
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Failed to parse JSON response from GraphQL API");
    }

    @Test
    void mapHousingEntities_WithMissingDataField_ShouldThrowHousingImportException() {
        String responseWithoutData = """
            {
                "sanity_allEnhet": []
            }
            """;
        assertThatThrownBy(() -> graphQLHousingMapper.mapHousingEntities(responseWithoutData))
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Invalid GraphQL response: missing 'data' field");
    }

    @Test
    void mapHousingEntities_WithGraphQLErrors_ShouldThrowHousingImportException() {
        String responseWithErrors = """
            {
                "data": null,
                "errors": [
                    {
                        "message": "Field not found"
                    }
                ]
            }
            """;
        assertThatThrownBy(() -> graphQLHousingMapper.mapHousingEntities(responseWithErrors))
                .isInstanceOf(HousingImportException.class)
                .hasMessageContaining("GraphQL response contains errors:");
    }

    @Test
    void mapHousingEntities_WithItemsMissingFields_ShouldSkipItems() {
        String responseWithMissingIds = """
            {
                "data": {
                    "sanity_allEnhet": [
                        {
                            "rentalObjectId": "VALID-001",
                            "name": "Valid Item",
                            "building": {
                                "address": "Valid Address"
                            },
                            "area": 20.0,
                            "price": 15000
                        },
                        {
                            "rentalObjectId": "VALID-002",
                            "name": "Invalid Item - No ID",
                            "area": 20.0,
                            "price": 15000
                        }
                    ]
                }
            }
            """;
        List<HousingModel> result = graphQLHousingMapper.mapHousingEntities(responseWithMissingIds);
        assertThat(result).hasSize(0);
    }

    @Test
    void mapHousingEntities_WithNegativeValues_ShouldHandleGracefully() {
        String responseWithNegativeValues = """
            {
                "data": {
                    "sanity_allEnhet": [
                        {
                            "rentalObjectId": "BER10-101",
                            "name": "Hybel 101 H0101",
                            "building": {
                                "address": "Berghusvegen 10"
                            },
                            "area": -5.5,
                            "price": -1000,
                            "category": {
                                "displayName": {
                                    "no": "Hybel i kollektiv",
                                    "en": "Dorm in collective"
                                }
                            },
                            "studentby": {
                                "name": "Kallerud",
                                "studiested": {
                                    "name": "Gjøvik"
                                }
                            },
                            "kollektiv": {
                                "name": "Kollektiv BER10-BER10-101-BER10-U01"
                            }
                        }
                    ]
                }
            }
            """;
        List<HousingModel> result = graphQLHousingMapper.mapHousingEntities(responseWithNegativeValues);
        assertThat(result).hasSize(0);
    }

    @Test
    void mapHousingAvailability_WithValidResponse_ShouldReturnAvailableHousings() {
        String validHousingIdsResponse = """
            {
                "data": {
                    "housings": {
                        "housingRentalObjects": [
                            {
                                "rentalObjectId": "KAL11-911",
                                "availableFrom": "2025-09-01T00:00:00.000+02:00"
                            },
                            {
                                "rentalObjectId": "LO9-12",
                                "availableFrom": "2025-09-02T00:00:00.000+02:00"
                            }
                        ]
                    }
                }
            }
            """;
        LocalDate date1 = OffsetDateTime.parse("2025-09-01T00:00:00.000+02:00")
                .toInstant()
                .atZone(ZoneId.of("Europe/Oslo"))
                .toLocalDate();
        LocalDate date2 = OffsetDateTime.parse("2025-09-02T00:00:00.000+02:00")
                .toInstant()
                .atZone(ZoneId.of("Europe/Oslo"))
                .toLocalDate();
        List<HousingAvailabilityDTO> result = graphQLHousingMapper.mapHousingAvailability(validHousingIdsResponse);
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(
                new HousingAvailabilityDTO("KAL11-911", date1),
                new HousingAvailabilityDTO("LO9-12", date2)
        );
    }

    @Test
    void mapHousingAvailability_WithNullInput_ShouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> graphQLHousingMapper.mapHousingAvailability(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JSON response cannot be null or empty");
    }

    @Test
    void mapHousingAvailability_WithEmptyInput_ShouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> graphQLHousingMapper.mapHousingEntities("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JSON response cannot be null or empty");
    }

    @Test
    void mapHousingAvailability_WithInvalidJson_ShouldThrowHousingImportException() {
        assertThatThrownBy(() -> graphQLHousingMapper.mapHousingEntities("invalid json"))
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Failed to parse JSON response from GraphQL API");
    }

    @Test
    void mapHousingAvailability_WithMissingHousingsField_ShouldThrowHousingImportException() {
        String responseWithoutHousings = """
            {
                "data": {
                    "other_field": []
                }
            }
            """;
        assertThatThrownBy(() -> graphQLHousingMapper.mapHousingAvailability(responseWithoutHousings))
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Invalid GraphQL response: missing 'housings.housingRentalObjects' field");
    }

    @Test
    void mapHousingAvailability_WithItemsWithoutRentalObjectId_ShouldSkipItems() {
        String responseWithMissingIds = """
            {
                "data": {
                    "housings": {
                        "housingRentalObjects": [
                            {
                                "rentalObjectId": "VALID-001",
                                "availableFrom": "2025-09-01T00:00:00.000+02:00"
                            },
                            {
                                "other_field": "no_id"
                            },
                            {
                                "rentalObjectId": ""
                            },
                            {
                                "rentalObjectId": "VALID-002",
                                "availableFrom": "2025-09-02T00:00:00.000+02:00"
                            }
                        ]
                    }
                }
            }
            """;
        LocalDate date1 = OffsetDateTime.parse("2025-09-01T00:00:00.000+02:00")
                .toInstant()
                .atZone(ZoneId.of("Europe/Oslo"))
                .toLocalDate();
        LocalDate date2 = OffsetDateTime.parse("2025-09-02T00:00:00.000+02:00")
                .toInstant()
                .atZone(ZoneId.of("Europe/Oslo"))
                .toLocalDate();
        List<HousingAvailabilityDTO> result = graphQLHousingMapper.mapHousingAvailability(responseWithMissingIds);
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(
                new HousingAvailabilityDTO("VALID-001", date1),
                new HousingAvailabilityDTO("VALID-002", date2)
        );
    }

    private void assertHousingModel(HousingModel model, String expectedRentalObjectId, String expectedName,
                                    String expectedAddress, String expectedHousingType, String expectedCity,
                                    String expectedDistrict, BigDecimal expectedAreaSqm, int expectedPricePerMonth) {
        assertThat(model.getRentalObjectId()).isEqualTo(expectedRentalObjectId);
        assertThat(model.getName()).isEqualTo(expectedName);
        assertThat(model.getAddress()).isEqualTo(expectedAddress);
        assertThat(model.getHousingType()).isEqualTo(expectedHousingType);
        assertThat(model.getCity()).isEqualTo(expectedCity);
        assertThat(model.getDistrict()).isEqualTo(expectedDistrict);
        assertThat(model.getAreaSqm()).isEqualTo(expectedAreaSqm);
        assertThat(model.getPricePerMonth()).isEqualTo(expectedPricePerMonth);
    }
}