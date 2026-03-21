package dev.sorn.orc.clients;

import dev.sorn.orc.api.JsonHttpClient;
import dev.sorn.orc.api.LlmClient;
import dev.sorn.orc.api.Result;
import dev.sorn.orc.api.Result.Failure;
import dev.sorn.orc.api.Result.Success;
import dev.sorn.orc.dto.OllamaResponse;
import dev.sorn.orc.errors.OrcException;
import dev.sorn.orc.types.Id;
import java.net.URI;
import static dev.sorn.orc.json.Json.jsonObjectNode;

public final class OllamaClient implements LlmClient {

    private final Id modelId;
    private final JsonHttpClient httpClient;
    private final URI baseUri;
    private final int maxTokens;

    public OllamaClient(
        Id modelId,
        JsonHttpClient httpClient,
        URI baseUri,
        int maxTokens
    ) {
        this.modelId = modelId;
        this.httpClient = httpClient;
        this.baseUri = baseUri;
        this.maxTokens = maxTokens;
    }

    @Override
    public Id modelId() {
        return modelId;
    }

    @Override
    public Result<String> complete(String prompt) {
        try {
            final var body = jsonObjectNode()
                .put("model", modelId.value())
                .put("prompt", prompt)
                .put("stream", false)
                .put("max_tokens", maxTokens);
            final var uri = baseUri.resolve("/v1/completions");
            final var response = httpClient.post(uri, body);
            return response.fold(
                json -> Success.of(OllamaResponse.of(json).toString()),
                Failure::of);
        } catch (Exception e) {
            return Failure.of(new OrcException(e));
        }
    }

}
