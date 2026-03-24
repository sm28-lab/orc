package dev.sorn.orc.api;

import tools.jackson.databind.JsonNode;

@FunctionalInterface
public interface Tool {

    JsonNode execute(JsonNode request);

}
