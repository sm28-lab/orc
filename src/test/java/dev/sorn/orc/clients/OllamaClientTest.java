package dev.sorn.orc.clients;

import dev.sorn.orc.api.Result.Success;
import dev.sorn.orc.errors.OrcException;
import dev.sorn.orc.types.Id;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;
import java.net.URI;
import static dev.sorn.orc.api.Result.Failure;
import static dev.sorn.orc.api.Result.Success.of;
import static dev.sorn.orc.json.Json.jsonObjectNode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class OllamaClientTest {

    private final DefaultJsonHttpClient mockHttpClient = mock(DefaultJsonHttpClient.class);
    private final Id modelId = Id.of("codellama");
    private final URI baseUri = URI.create("http://localhost:11434");
    private final OllamaClient llmClient = new OllamaClient(modelId, mockHttpClient, baseUri, 512);

    @Test
    void returns_parsed_response() {
        // GIVEN
        var fakeResponse = jsonObjectNode()
            .put("id", "cmpl-645")
            .put("model", "codellama")
            .set("choices", jsonObjectNode().putArray("choices")
                .add(jsonObjectNode()
                    .put("index", 0)
                    .put("text", "The SOLID principles are a set of design principles...")));

        given(mockHttpClient.post(any(URI.class), any(ObjectNode.class)))
            .willReturn(of(fakeResponse));

        // WHEN
        var result = llmClient.complete("Explain SOLID principles");

        // THEN
        assertThat(result).isInstanceOf(Success.class);
        var text = ((Success<String>) result).value();
        assertThat(text).contains("The SOLID principles are a set of design principles...");
    }

    @Test
    void propagates_http_failure() {
        // GIVEN
        given(mockHttpClient.post(any(URI.class), any(ObjectNode.class)))
            .willReturn(Failure.of(new OrcException("http fail")));

        // WHEN
        var result = llmClient.complete("prompt");

        // THEN
        assertThat(result).isInstanceOf(Failure.class);
    }

}
