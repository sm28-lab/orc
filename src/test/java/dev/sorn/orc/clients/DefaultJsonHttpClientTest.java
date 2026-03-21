package dev.sorn.orc.clients;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static dev.sorn.orc.api.Result.Success;
import static dev.sorn.orc.json.Json.jsonObjectNode;
import static java.net.URI.create;
import static java.net.http.HttpResponse.BodyHandler;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class DefaultJsonHttpClientTest {

    private final HttpClient clientMock = mock(HttpClient.class);
    private final DefaultJsonHttpClient httpClient = new DefaultJsonHttpClient(clientMock);
    private final HttpResponse<String> responseMock = mock(HttpResponse.class);

    @Test
    @DisplayName("GET returns parsed JSON")
    void get_returns_parsed_json() throws Exception {
        // GIVEN
        given(responseMock.body())
            .willReturn("{\"key\":\"value\"}");
        given(clientMock.send(any(HttpRequest.class), any(BodyHandler.class)))
            .willReturn(responseMock);

        // WHEN
        var result = httpClient.get(create("https://example.com"));

        // THEN
        assertThat(result instanceof Success).isTrue();
        assertThat(((Success<JsonNode>) result).value().get("key").asText()).isEqualTo("value");
    }

    @Test
    @DisplayName("POST returns parsed JSON")
    void postReturnsParsedJson() throws Exception {
        // GIVEN
        given(responseMock.body())
            .willReturn("{\"response\":\"ok\"}");
        given(clientMock.send(any(HttpRequest.class), any(BodyHandler.class)))
            .willReturn(responseMock);

        // WHEN
        var result = httpClient.post(create("https://example.com"), jsonObjectNode().put("foo", "bar"));

        // THEN
        assertThat(result).isInstanceOf(Success.class);
        assertThat(((Success<JsonNode>) result).value().get("response").asText()).isEqualTo("ok");
    }

}
