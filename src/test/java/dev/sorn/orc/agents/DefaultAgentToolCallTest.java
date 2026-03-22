package dev.sorn.orc.agents;

import dev.sorn.orc.StubToolRegistry;
import dev.sorn.orc.api.LlmClient;
import dev.sorn.orc.api.Result;
import dev.sorn.orc.api.ToolRegistry;
import dev.sorn.orc.types.Id;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import static dev.sorn.orc.agents.DefaultAgent.Builder.defaultAgent;
import static dev.sorn.orc.api.Result.Success;
import static dev.sorn.orc.json.Json.jsonObjectNode;
import static dev.sorn.orc.types.AgentDefinition.Builder.agentDefinition;
import static dev.sorn.orc.types.AgentRole.WORKER;
import static dev.sorn.orc.types.BddInstructionGroup.Builder.bddInstructionGroup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class DefaultAgentToolCallTest {

    private final ToolRegistry toolRegistry = new StubToolRegistry();
    private final LlmClient llmClient = mock(LlmClient.class);

    @Test
    void calls_tool_and_returns_final_answer() {
        // GIVEN
        var agentDefinition = agentDefinition()
            .id(Id.of("test_agent"))
            .role(WORKER)
            .toolIds(List.of(Id.of("print_working_directory_tool")))
            .inputs(List.empty())
            .outputs(List.empty())
            .instructionGroups(List.of(
                bddInstructionGroup()
                    .given(List.of("You are a test agent."))
                    .when(List.of("When asked for the working directory."))
                    .then(List.of("Use print_working_directory_tool and then output the result."))
                    .build()
            ))
            .build();

        var agent = defaultAgent()
            .agentDefinition(agentDefinition)
            .toolRegistry(toolRegistry)
            .llmClient(llmClient)
            .build();

        var toolCallJson = jsonObjectNode()
            .put("tool", "print_working_directory_tool")
            .set("arguments", jsonObjectNode());
        var toolCallResponse = "<tool_call>\n" + toolCallJson + "\n</tool_call>\n{\"cwd\": \"" + System.getProperty("user.dir") + "\"}";
        var finalAnswer = "{\"cwd\": \"" + System.getProperty("user.dir") + "\"}";

        given(llmClient.complete(anyString()))
            .willReturn(Success.of(toolCallResponse))
            .willReturn(Success.of(finalAnswer));

        // WHEN
        var input = jsonObjectNode().put("task", "What is the working directory?");
        var result = agent.execute(input);

        // THEN
        assertThat(result).isInstanceOf(Result.Success.class);
        var value = ((Result.Success<JsonNode>) result).value();
        assertThat(value.get("cwd").asText()).isEqualTo(System.getProperty("user.dir"));
    }

}
