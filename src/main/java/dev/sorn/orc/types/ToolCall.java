package dev.sorn.orc.types;

import tools.jackson.databind.JsonNode;

public record ToolCall(Id toolId, JsonNode arguments) {

    public static ToolCall of(Id toolId, JsonNode arguments) {
        return new ToolCall(toolId, arguments);
    }

}