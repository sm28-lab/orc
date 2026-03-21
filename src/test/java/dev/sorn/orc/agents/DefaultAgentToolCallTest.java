package dev.sorn.orc.agents;

import dev.sorn.orc.DefaultAgentTestData;
import dev.sorn.orc.api.LlmClient;
import dev.sorn.orc.api.Result;
import dev.sorn.orc.api.ToolRegistry;
import dev.sorn.orc.types.AgentDefinition;
import org.junit.jupiter.api.Test;
import static dev.sorn.orc.agents.DefaultAgent.Builder.defaultAgent;
import static dev.sorn.orc.api.Result.Success;
import static dev.sorn.orc.json.Json.jsonObjectNode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class DefaultAgentToolCallTest implements DefaultAgentTestData {

    private final AgentDefinition definition = anAgentDefinition().build();
    private final ToolRegistry toolRegistry = new StubToolRegistry();
    private final LlmClient llmClient = mock(LlmClient.class);
    private final DefaultAgent agent = defaultAgent()
        .agentDefinition(definition)
        .toolRegistry(toolRegistry)
        .llmClient(llmClient)
        .build();

    @Test
    void calls_tool_and_returns_final_answer() {
        // GIVEN
        var toolCallJson = jsonObjectNode()
            .put("tool", "print_working_directory_tool")
            .set("arguments", jsonObjectNode());
        var toolCallResponse = "<tool_call>\n" + toolCallJson.toString() + "\n</tool_call>\nThe current directory is " + System.getProperty("user.dir");
        var finalAnswer = "Final answer: " + System.getProperty("user.dir");

        given(llmClient.complete(anyString()))
            .willReturn(Success.of(toolCallResponse))
            .willReturn(Success.of(finalAnswer));

        // WHEN
        var result = agent.complete("What is the working directory?");

        // THEN
        assertThat(result).isInstanceOf(Result.Success.class);
        assertThat(((Result.Success<String>) result).value()).isEqualTo(finalAnswer);
    }

}
