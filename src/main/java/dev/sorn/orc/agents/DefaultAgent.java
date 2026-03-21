package dev.sorn.orc.agents;

import dev.sorn.orc.api.LlmClient;
import dev.sorn.orc.api.Result;
import dev.sorn.orc.api.ToolRegistry;
import dev.sorn.orc.errors.OrcException;
import dev.sorn.orc.errors.ToolCallException;
import dev.sorn.orc.json.Json;
import dev.sorn.orc.parsers.ToolCallParser;
import dev.sorn.orc.types.AgentDefinition;
import dev.sorn.orc.types.ToolCall;
import io.vavr.collection.List;
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
            progressConsumer.accept(msg);
        } else {
            System.err.println("[DEBUG] " + msg);
        }
    }

    @Override
    public Result<String> complete(String prompt) {
        var conversation = new StringBuilder();
        conversation.append("## Instructions\n");
        agentDefinition.instructions().forEach(instruction -> {
            conversation.append(instruction.type()).append(": ").append(instruction.text()).append("\n");
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
        conversation.append("\n## User Input\n").append(prompt).append("\n");

        var iteration = 0;
        var lastToolCall = new ToolCall(null, null);
        var repeatCount = 0;

        while (iteration < MAX_ITERATIONS) {
            log("Iteration " + (iteration + 1) + " - calling LLM");
            var llmResult = llmClient.complete(conversation.toString());
            if (llmResult instanceof Result.Failure<String> failure) {
                return failure;
            }
            var response = ((Result.Success<String>) llmResult).value();
            log("LLM response received, length: " + response.length());

            if (response == null || response.isBlank()) {
                return Result.Failure.of(new OrcException("LLM returned empty response"));
            }

            log("Response preview: " + response.substring(0, Math.min(200, response.length())));

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
                return Result.Success.of(response);
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
            for (var toolCall : toolCalls) {
                log("Executing tool: " + toolCall.toolId().value());
                log("Arguments: " + Json.toJson(toolCall.arguments()));
                try {
                    var tool = toolRegistry.get(toolCall.toolId());
                    var input = tool.parseArguments(toolCall.arguments());
                    var result = tool.execute(input);
                    var resultStr = result.fold(
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

    public static final class Builder {
        private AgentDefinition agentDefinition;
        private ToolRegistry toolRegistry;
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

        public Builder toolRegistry(ToolRegistry toolRegistry) {
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
