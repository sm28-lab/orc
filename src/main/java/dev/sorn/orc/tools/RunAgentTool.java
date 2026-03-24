package dev.sorn.orc.tools;

import dev.sorn.orc.agents.DefaultAgent;
import dev.sorn.orc.api.Result;
import dev.sorn.orc.api.LegacyTool;
import dev.sorn.orc.errors.OrcException;
import dev.sorn.orc.types.Id;
import tools.jackson.databind.JsonNode;
import java.util.Map;

public final class RunAgentTool implements LegacyTool<RunAgentTool.Input, JsonNode> {

    public static final Id RUN_AGENT_TOOL_ID = Id.of("run_agent_tool");
    private final Map<Id, DefaultAgent> agentMap;

    public RunAgentTool(Map<Id, DefaultAgent> agentMap) {
        this.agentMap = agentMap;
    }

    @Override
    public Id id() {
        return RUN_AGENT_TOOL_ID;
    }

    @Override
    public Result<JsonNode> execute(Input input) {
        final var agent = agentMap.get(input.agentId);
        if (agent == null) {
            return Result.Failure.of(new OrcException("Agent not found: " + input.agentId.value()));
        }
        return agent.execute(input.input);
    }

    @Override
    public Class<Input> inputType() {
        return Input.class;
    }

    @Override
    public Input parseArguments(JsonNode node) {
        if (!node.isObject()) {
            throw new OrcException("RunAgentTool expects an object with 'agentId' and 'input' fields");
        }
        final var agentIdNode = node.get("agentId");
        final var inputNode = node.get("input");
        if (agentIdNode == null || !agentIdNode.isTextual()) {
            throw new OrcException("'agentId' is required and must be a string");
        }
        if (inputNode == null) {
            throw new OrcException("'input' is required");
        }
        final var agentId = Id.of(agentIdNode.asText());
        return new Input(agentId, inputNode);
    }

    @Override
    public String inputDescription() {
        return """
            Executes another agent by ID with the given input.
            Input object:
            - "agentId": string (required) – ID of the agent to run
            - "input": object (required) – input to pass to the agent
            Example: {"agentId": "developer_agent", "input": {"task": "Write code"}}
            """;
    }

    public record Input(Id agentId, JsonNode input) {}
}