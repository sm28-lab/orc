package dev.sorn.orc.api;

import dev.sorn.orc.types.Id;
import tools.jackson.databind.JsonNode;
import static dev.sorn.orc.json.Json.fromJsonNode;

public interface LegacyTool<I, O> {

    Id id();

    Result<O> execute(I input);

    Class<I> inputType();

    default I parseArguments(JsonNode node) {
        return fromJsonNode(node, inputType());
    }

    default String inputDescription() {
        return "JSON object with fields: " + inputType().getSimpleName();
    }

}
