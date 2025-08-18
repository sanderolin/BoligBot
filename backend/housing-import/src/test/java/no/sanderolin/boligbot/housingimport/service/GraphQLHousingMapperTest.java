package no.sanderolin.boligbot.housingimport.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.sanderolin.boligbot.dao.model.HousingModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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
    void mapHousingIds_WithValidResponse_ShouldReturnIds() {
        String validHousingIdsResponse = """
            {
                "data": {
                    "housings": {
                        "housingRentalObjects": [
                            {
                                "rentalObjectId": "KAL11-911"
                            },
                            {
                                "rentalObjectId": "LO9-12"
                            }
                        ]
                    }
                }
            }
            """;
        List<String> result = graphQLHousingMapper.mapHousingIds(validHousingIdsResponse);
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("KAL11-911", "LO9-12");
    }

    @Test
    void mapHousingIds_WithNullInput_ShouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> graphQLHousingMapper.mapHousingIds(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JSON response cannot be null or empty");
    }

    @Test
    void mapHousingIds_WithEmptyInput_ShouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> graphQLHousingMapper.mapHousingEntities("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JSON response cannot be null or empty");
    }

    @Test
    void mapHousingIds_WithInvalidJson_ShouldThrowHousingImportException() {
        assertThatThrownBy(() -> graphQLHousingMapper.mapHousingEntities("invalid json"))
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Failed to parse JSON response from GraphQL API");
    }

    @Test
    void mapHousingIds_WithMissingHousingsField_ShouldThrowHousingImportException() {
        String responseWithoutHousings = """
            {
                "data": {
                    "other_field": []
                }
            }
            """;
        assertThatThrownBy(() -> graphQLHousingMapper.mapHousingIds(responseWithoutHousings))
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Invalid GraphQL response: missing 'housings.housingRentalObjects' field");
    }

    @Test
    void mapHousingIds_WithItemsWithoutRentalObjectId_ShouldSkipItems() {
        String responseWithMissingIds = """
            {
                "data": {
                    "housings": {
                        "housingRentalObjects": [
                            {
                                "rentalObjectId": "VALID-001"
                            },
                            {
                                "other_field": "no_id"
                            },
                            {
                                "rentalObjectId": ""
                            },
                            {
                                "rentalObjectId": "VALID-002"
                            }
                        ]
                    }
                }
            }
            """;
        List<String> result = graphQLHousingMapper.mapHousingIds(responseWithMissingIds);
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("VALID-001", "VALID-002");
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