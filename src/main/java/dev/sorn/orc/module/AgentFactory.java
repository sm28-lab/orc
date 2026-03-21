package dev.sorn.orc.module;

import dev.sorn.orc.api.AgentDefinition;
import dev.sorn.orc.types.AgentData;
import dev.sorn.orc.types.AgentRole;
import dev.sorn.orc.types.Id;
import io.vavr.collection.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

import static dev.sorn.orc.json.Json.fromJson;
import static io.vavr.collection.List.ofAll;

public class AgentFactory {

    public List<AgentDefinition> loadFromJson(String json) {
        final var root = fromJson(json);
        final var agentsNode = (ArrayNode) root.get("agents");
        return ofAll(agentsNode).map(this::toAgentDefinition);
    }

    private AgentDefinition toAgentDefinition(JsonNode node) {
        final var id = Id.of(node.get("id").asText());
        final var role = AgentRole.valueOf(node.get("role").asText().toUpperCase());
        final var toolIds = ofAll(node.get("toolIds")).map(t -> Id.of(t.asText()));
        final var inputs = parseAgentData(node.get("input"));
        final var outputs = parseAgentData(node.get("output"));
        final var instructions = parseInstructions(node.get("instructions"));

        return new AgentDefinition() {
            @Override
            public Id id() { return id; }

            @Override
            public AgentRole role() { return role; }

            @Override
            public List<Id> toolIds() { return toolIds; }

            @Override
            public List<AgentData> input() { return inputs; }

            @Override
            public List<AgentData> output() { return outputs; }

            @Override
            public List<String> instructions() { return instructions; }
        };
    }

    private List<AgentData> parseAgentData(JsonNode node) {
        if (node == null || !node.isArray()) return List.empty();
        return ofAll(node).map(n -> new AgentData(
            n.has("name") ? n.get("name").asText() : "",
            n.has("type") ? AgentData.Type.valueOf(n.get("type").asText().toUpperCase()) : null));
    }

    private List<String> parseInstructions(JsonNode node) {
        if (node == null || !node.isArray()) return List.empty();
        return ofAll(node).map(JsonNode::asText);
    }

}
