package no.sanderolin.boligbot.housingimport.util;

import no.sanderolin.boligbot.housingimport.exception.HousingImportException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SitGraphQLClientTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    @InjectMocks private SitGraphQLClient sitGraphQLClient;

    private final String query = """
            {
              "operationName": "GetHousingItems",
              "variables": {
                "input": {
                  "category": {
                    "displayName": {
                      "no": {
                        "neq": "Parkering"
                      }
                    }
                  }
                },
                "sort": [
                  { "_id": "ASC" }
                ],
                "limit": 1,
                "offset": 0
              },
              "query": "query GetHousingItems($input: Sanity_EnhetFilter, $limit: Int, $offset: Int) { sanity_allEnhet(where: $input, limit: $limit, offset: $offset) { rentalObjectId name building { address } area price category { displayName { no en } } studentby { name studiested { name } } kollektiv { name } } }"
            }
            """;

    @Test
    void executeGraphQLQuery_WithNullInput_ShouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> sitGraphQLClient.executeGraphQLQuery(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GraphQL query cannot be null or empty");
    }

    @Test
    void executeGraphQLQuery_WithEmptyInput_ShouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> sitGraphQLClient.executeGraphQLQuery("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GraphQL query cannot be null or empty");
    }

    @Test
    void executeGraphQLQuery_WithNullResponse_ShouldThrowHousingImportException() {
        when(restClient.post().body(eq(query)).retrieve().body(eq(String.class))).thenReturn(null);

        assertThatThrownBy(() -> sitGraphQLClient.executeGraphQLQuery(query))
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Received empty response from SIT GraphQL API");
    }

    @Test
    void executeGraphQLQuery_WithErrorResponse_ShouldThrowHousingImportException() {
        String incorrectQuery = "{\"query\":\"{ sanity_allEnhet { name } }\"}";

        String errorResponse = """
                {
                    "errors": [
                        {
                            "message": "Expected a `Name`-token, but found a `EndOfFile`-token.",
                            "locations": [
                                {
                                    "line": 1,
                                    "column": 112
                                }
                            ],
                            "extensions": {
                                "code": "HC0011"
                            }
                        }
                    ]
                }
                """;
        when(restClient.post().body(eq(incorrectQuery)).retrieve().body(eq(String.class))).thenReturn(errorResponse);

        assertThatThrownBy(() -> sitGraphQLClient.executeGraphQLQuery(incorrectQuery))
                .isInstanceOf(HousingImportException.class)
                .hasMessage("GraphQL API returned errors: %s".formatted(errorResponse));
    }

    @Test
    void executeGraphQLQuery_RestClientError_ShouldThrowHousingImportException() {
        when(restClient.post().body(eq(query)).retrieve().body(eq(String.class)))
                .thenThrow(new RestClientException("Network error"));

        assertThatThrownBy(() -> sitGraphQLClient.executeGraphQLQuery(query))
                .isInstanceOf(HousingImportException.class)
                .hasMessage("Failed to communicate with SIT GraphQL API")
                .cause()
                .isInstanceOf(RestClientException.class);
    }

    @Test
    void executeGraphQLQuery_ValidQuery_ShouldReturnResponse() {
        String response = """
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
                                },
                                "kollektiv": {
                                    "name": "Kollektiv BER10-BER10-101-BER10-U01"
                                }
                            }
                        ]
                    }
                }
                """;
        when(restClient.post().body(eq(query)).retrieve().body(eq(String.class))).thenReturn(response);

        String result = sitGraphQLClient.executeGraphQLQuery(query);
        assertThat(result).isEqualTo(response);
    }

}