package dev.sorn.orc.agents;

import dev.sorn.orc.api.LlmClient;
import dev.sorn.orc.api.Result;
import dev.sorn.orc.api.ToolRegistry;
import dev.sorn.orc.errors.OrcException;
import dev.sorn.orc.errors.ToolCallException;
import dev.sorn.orc.json.Json;
import dev.sorn.orc.parsers.ToolCallParser;
import dev.sorn.orc.types.AgentDefinition;
import dev.sorn.orc.types.BddInstruction;
import dev.sorn.orc.types.ToolCall;
import io.vavr.collection.List;

import java.util.StringJoiner;

import static dev.sorn.orc.json.Json.toJson;

public final class DefaultAgent extends BaseAgent {

    private static final int MAX_ITERATIONS = 5;

    private DefaultAgent(Builder builder) {
        super(builder.agentDefinition, builder.toolRegistry, builder.llmClient);
    }

    @Override
    public Result<String> complete(String prompt) {
        var conversation = new StringBuilder();
        conversation.append("## Instructions\n");
        agentDefinition.instructions().forEach(instruction -> {
            conversation.append(instruction.type()).append(": ").append(instruction.text()).append("\n");
        });
        conversation.append("\n## Available Tools\n");
        tools().forEach(tool -> {
            conversation.append("- ").append(tool.id().value()).append(": ").append(tool.inputDescription()).append("\n");
        });
        conversation.append("\n## Tool Usage Format\n");
        conversation.append("""
            You can use tools by outputting a tool call in the following strict format:
            <tool_call>
            {
              "tool": "tool_id",
              "arguments": { ... }
            }
            </tool_call>
            You may output multiple tool calls. After each tool call you will receive the result. Then you can output more tool calls or the final answer.
            """);
        conversation.append("\n## User Input\n").append(prompt).append("\n");

        var iteration = 0;
        while (iteration < MAX_ITERATIONS) {
            System.err.println("[DEBUG] Iteration " + (iteration + 1) + " - calling LLM");
            var llmResult = llmClient.complete(conversation.toString());
            if (llmResult instanceof Result.Failure<String> failure) {
                return failure;
            }
            var response = ((Result.Success<String>) llmResult).value();
            System.err.println("[DEBUG] LLM response length: " + (response == null ? "null" : response.length()));

            if (response == null || response.isBlank()) {
                return Result.Failure.of(new OrcException("LLM returned empty response"));
            }

            List<ToolCall> toolCalls;
            try {
                toolCalls = ToolCallParser.parse(response);
                System.err.println("[DEBUG] Parsed " + toolCalls.size() + " tool calls");
            } catch (ToolCallException e) {
                conversation.append("## Assistant\n").append(response).append("\n");
                conversation.append("## Tool Call Error\n").append(e.getMessage()).append("\n");
                iteration++;
                continue;
            }

            if (toolCalls.isEmpty()) {
                return Result.Success.of(response);
            }

            conversation.append("## Assistant\n").append(response).append("\n");
            conversation.append("## Tool Results\n");
            for (var toolCall : toolCalls) {
                try {
                    var tool = toolRegistry.get(toolCall.toolId());
                    var input = tool.parseArguments(toolCall.arguments());
                    var result = tool.execute(input);
                    var resultStr = result.fold(
                        Json::toJson,
                        err -> "Error: " + err.getMessage()
                    );
                    conversation.append("Tool: ").append(toolCall.toolId().value()).append("\n");
                    conversation.append("Result: ").append(resultStr).append("\n");
                } catch (OrcException e) {
                    conversation.append("Tool: ").append(toolCall.toolId().value()).append("\n");
                    conversation.append("Result: Error: ").append(e.getMessage()).append("\n");
                }
            }
            iteration++;
        }
        return Result.Failure.of(new OrcException("Maximum tool call iterations reached"));
    }

    public static final class Builder {
        private AgentDefinition agentDefinition;
        private ToolRegistry toolRegistry;
        private LlmClient llmClient;

        private Builder() {}

        public static Builder defaultAgent() {
            return new Builder();
        }

        public Builder agentDefinition(AgentDefinition agentDefinition) {
            this.agentDefinition = agentDefinition;
            return this;
        }

        public Builder toolRegistry(ToolRegistry toolRegistry) {
            this.toolRegistry = toolRegistry;
            return this;
        }

        public Builder llmClient(LlmClient llmClient) {
            this.llmClient = llmClient;
            return this;
        }

        public DefaultAgent build() {
            return new DefaultAgent(this);
        }
    }
}