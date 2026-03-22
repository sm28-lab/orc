package dev.sorn.orc;

import dev.sorn.orc.api.LlmClient;
import dev.sorn.orc.api.Result;
import dev.sorn.orc.api.Result.Success;
import dev.sorn.orc.types.Id;
import java.util.HashMap;
import java.util.Map;

public class StubLlmClient implements LlmClient {

    private final Map<String, String> responses = new HashMap<>();
    private String defaultResponse = "{}";
    private String currentPrompt;

    @Override
    public Id modelId() {
        return Id.of("stub_llm_client");
    }

    @Override
    public Result<String> complete(String prompt) {
        String response = responses.getOrDefault(prompt, defaultResponse);
        return Success.of(response);
    }

    public StubLlmClient given(String prompt) {
        this.currentPrompt = prompt;
        return this;
    }

    public void willReturn(String response) {
        if (currentPrompt == null) {
            throw new IllegalStateException("Call given() first");
        }
        responses.put(currentPrompt, response);
        currentPrompt = null;
    }

    public void setDefaultResponse(String defaultResponse) {
        this.defaultResponse = defaultResponse;
    }

}
