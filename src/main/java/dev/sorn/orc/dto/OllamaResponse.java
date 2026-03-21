package dev.sorn.orc.dto;

import io.vavr.collection.List;
import tools.jackson.databind.JsonNode;
import static io.vavr.collection.List.ofAll;

public record OllamaResponse(
    String id,
    String model,
    List<Choice> choices
) {

    public record Choice(int index, String text) {}

    public static OllamaResponse of(JsonNode json) {
        if (json == null) {
            throw new IllegalArgumentException("JsonNode cannot be null");
        }
        final var id = json.has("id") && !json.get("id").isNull() ? json.get("id").asText() : "";
        final var model = json.has("model") && !json.get("model").isNull() ? json.get("model").asText() : "";
        var choices = List.<Choice>empty();
        if (json.has("choices") && json.get("choices").isArray()) {
            final var iterator = json.get("choices").iterator();
            choices = ofAll(() -> iterator)
                .map(node -> {
                    final var index = node.has("index") ? node.get("index").asInt() : 0;
                    final var text = node.has("text") && !node.get("text").isNull() ? node.get("text").asText() : "";
                    return new Choice(index, text);
                });
        }
        return new OllamaResponse(id, model, choices);
    }

}
