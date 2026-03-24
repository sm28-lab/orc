package dev.sorn.orc.agents;

import dev.sorn.orc.api.LegacyToolRegistry;
import dev.sorn.orc.api.LlmClient;
import dev.sorn.orc.api.Result;
import dev.sorn.orc.errors.OrcException;
import dev.sorn.orc.errors.ToolCallException;
import dev.sorn.orc.json.Json;
import dev.sorn.orc.parsers.ToolCallParser;
import dev.sorn.orc.types.AgentData;
import dev.sorn.orc.types.AgentDefinition;
import dev.sorn.orc.types.ToolCall;
import io.vavr.collection.List;
import tools.jackson.databind.JsonNode;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class DefaultAgent extends BaseAgent {

    private static final int MAX_ITERATIONS = 20;

    private final Consumer<String> progressConsumer;

    private DefaultAgent(Builder builder) {
        super(builder.agentDefinition, builder.toolRegistry, builder.llmClient);
        this.progressConsumer = builder.progressConsumer;
    }

    private void log(String msg) {
        if (progressConsumer != null) {
            progressConsumer.accept("[" + agentDefinition.id().value() + "] " + msg);
        } else {
            System.err.println("[DEBUG] [" + agentDefinition.id().value() + "] " + msg);
        }
    }

    @Override
    public Result<JsonNode> execute(JsonNode input) {
        var validation = validateInput(input);
        if (validation instanceof Result.Failure<?>(OrcException ex)) {
            return Result.Failure.of(new OrcException((ex.getMessage())));
        }

        final var conversation = new StringBuilder();
        conversation.append("## Instructions\n");
        agentDefinition.instructions().forEach(group -> {
            group.given().forEach(text -> conversation.append("GIVEN: ").append(text).append("\n"));
            group.when().forEach(text -> conversation.append("WHEN: ").append(text).append("\n"));
            group.then().forEach(text -> conversation.append("THEN: ").append(text).append("\n"));
            conversation.append("\n");
        });
        conversation.append("\n## Current Working Directory\n")
            .append(Path.of("").toAbsolutePath().normalize().toString())
            .append("\n\n");
        conversation.append("## Available Tools\n");
        tools().forEach(tool -> {
            conversation.append("- ").append(tool.id().value()).append(": ").append(tool.inputDescription()).append("\n");
        });
        conversation.append("\n## Efficiency Guidelines\n");
        conversation.append("""
            - Use **grep_tool** to search for files by name or content (it can search recursively).
            - Use **list_directory_contents_tool** only for listing a single directory’s immediate children (non‑recursive).
            - Avoid calling list_directory_contents_tool repeatedly for deep searches; use grep_tool instead.
            """);
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

        conversation.append("\n## Input\n");
        conversation.append(Json.toJson(input)).append("\n");

        conversation.append("\n## Output Schema\n");
        conversation.append("The final answer must be a JSON object with the following fields:\n");
        agentDefinition.outputs().forEach(output -> {
            conversation.append("- ").append(output.name()).append(": ").append(output.type().name()).append("\n");
        });
        conversation.append("\nYour final answer must be a valid JSON object matching this schema.\n");

        var iteration = 0;
        var lastToolCall = new ToolCall(null, null);
        var repeatCount = 0;
        var consecutiveInvalid = 0;
        String lastInvalidResponse = null;

        while (iteration < MAX_ITERATIONS) {
            log("Iteration " + (iteration + 1) + " - calling LLM");
            final var llmResult = llmClient.complete(conversation.toString());
            if (llmResult instanceof Result.Failure<String> failure) {
                return Result.Failure.of(new OrcException(failure.value()));
            }
            final var response = ((Result.Success<String>) llmResult).value();
            log("LLM response received, length: " + response.length());

            if (response == null || response.isBlank()) {
                return Result.Failure.of(new OrcException("LLM returned empty response"));
            }

            log("Response preview: " + response.substring(0, Math.min(420, response.length())));

            List<ToolCall> toolCalls;
            try {
                toolCalls = ToolCallParser.parse(response);
                log("Parsed " + toolCalls.size() + " tool calls");
            } catch (ToolCallException e) {
                conversation.append("## Assistant\n").append(response).append("\n");
                conversation.append("## Tool Call Error\n").append(e.getMessage()).append("\n");
                iteration++;
                continue;
            }

            if (toolCalls.isEmpty()) {
                try {
                    final var finalNode = Json.fromJson(response);
                    var validationResult = validateOutput(finalNode);
                    if (validationResult instanceof Result.Failure<?>(OrcException ex)) {
                        // Check for repeated invalid output
                        if (lastInvalidResponse != null && lastInvalidResponse.equals(response)) {
                            consecutiveInvalid++;
                            if (consecutiveInvalid >= 3) {
                                log("Repeated invalid output 3 times, giving up");
                                return Result.Failure.of(new OrcException("Agent failed to produce valid output after multiple attempts"));
                            }
                        } else {
                            consecutiveInvalid = 1;
                            lastInvalidResponse = response;
                        }
                        conversation.append("## Assistant\n").append(response).append("\n");
                        conversation.append("## Output Validation Error\n").append(ex.getMessage()).append("\n");
                        // Append expected schema for clarity
                        conversation.append("Expected output schema: ").append(describeOutputSchema()).append("\n");
                        iteration++;
                        continue;
                    }
                    return Result.Success.of(finalNode);
                } catch (Exception e) {
                    conversation.append("## Assistant\n").append(response).append("\n");
                    conversation.append("## Invalid JSON Output\n").append(e.getMessage()).append("\n");
                    iteration++;
                    continue;
                }
            }

            if (!toolCalls.isEmpty() && toolCalls.get(0).equals(lastToolCall)) {
                repeatCount++;
                if (repeatCount >= 2) {
                    log("Repeated tool call detected, forcing final answer");
                    return Result.Failure.of(new OrcException("Agent stuck in tool call loop"));
                }
            } else {
                repeatCount = 0;
                lastToolCall = toolCalls.get(0);
            }

            conversation.append("## Assistant\n").append(response).append("\n");
            conversation.append("## Tool Results\n");
            for (final var toolCall : toolCalls) {
                log("Executing tool: " + toolCall.toolId().value());
                log("Arguments: " + Json.toJson(toolCall.arguments()));
                try {
                    final var tool = toolRegistry.get(toolCall.toolId());
                    final var inputObj = tool.parseArguments(toolCall.arguments());
                    final var result = tool.execute(inputObj);
                    final var resultStr = result.fold(
                        val -> {
                            log("Tool succeeded, result length: " + (val != null ? val.toString().length() : 0));
                            return Json.toJson(val);
                        },
                        err -> {
                            log("Tool failed: " + err.getMessage());
                            return "Error: " + err.getMessage();
                        }
                    );
                    conversation.append("Tool: ").append(toolCall.toolId().value()).append("\n");
                    conversation.append("Result: ").append(resultStr).append("\n");
                } catch (OrcException e) {
                    log("Tool execution exception: " + e.getMessage());
                    conversation.append("Tool: ").append(toolCall.toolId().value()).append("\n");
                    conversation.append("Result: Error: ").append(e.getMessage()).append("\n");
                }
            }
            iteration++;
        }
        return Result.Failure.of(new OrcException("Maximum tool call iterations reached"));
    }

    private Result<Void> validateInput(JsonNode input) {
        return validateAgainstSchema(input, agentDefinition.inputs());
    }

    private Result<Void> validateOutput(JsonNode output) {
        return validateAgainstSchema(output, agentDefinition.outputs());
    }

    private Result<Void> validateAgainstSchema(JsonNode node, List<AgentData> schema) {
        if (!node.isObject()) {
            return Result.Failure.of(new OrcException("Input must be a JSON object"));
        }
        for (var field : schema) {
            if (!node.has(field.name())) {
                return Result.Failure.of(new OrcException("Missing required field: " + field.name()));
            }
            var value = node.get(field.name());
            var type = field.type();
            switch (type) {
                case STRING:
                    if (!value.isTextual()) {
                        return Result.Failure.of(new OrcException("Field '" + field.name() + "' must be a string"));
                    }
                    break;
                case BOOLEAN:
                    if (!value.isBoolean()) {
                        return Result.Failure.of(new OrcException("Field '" + field.name() + "' must be a boolean"));
                    }
                    break;
                case COLLECTION:
                    if (!value.isArray()) {
                        return Result.Failure.of(new OrcException("Field '" + field.name() + "' must be an array"));
                    }
                    break;
            }
        }
        return Result.Success.of(null);
    }

    private String describeOutputSchema() {
        var sb = new StringBuilder("{ ");
        var first = true;
        for (var output : agentDefinition.outputs()) {
            if (!first) sb.append(", ");
            sb.append(output.name()).append(": ").append(output.type().name().toLowerCase());
            first = false;
        }
        sb.append(" }");
        return sb.toString();
    }

    public static final class Builder {
        private AgentDefinition agentDefinition;
        private LegacyToolRegistry toolRegistry;
        private LlmClient llmClient;
        private Consumer<String> progressConsumer;

        private Builder() {}

        public static Builder defaultAgent() {
            return new Builder();
        }

        public Builder agentDefinition(AgentDefinition agentDefinition) {
            this.agentDefinition = agentDefinition;
            return this;
        }

        public Builder toolRegistry(LegacyToolRegistry toolRegistry) {
            this.toolRegistry = toolRegistry;
            return this;
        }

        public Builder llmClient(LlmClient llmClient) {
            this.llmClient = llmClient;
            return this;
        }

        public Builder progressConsumer(Consumer<String> progressConsumer) {
            this.progressConsumer = progressConsumer;
            return this;
        }

        public DefaultAgent build() {
            return new DefaultAgent(this);
        }
    }

}
