package dev.sorn.orc.tools;

import dev.sorn.orc.agents.DefaultAgent;
import dev.sorn.orc.api.Result;
import dev.sorn.orc.errors.OrcException;
import dev.sorn.orc.types.Id;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import java.util.Map;
import static dev.sorn.orc.json.Json.jsonObjectNode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RunAgentToolTest {

    @Test
    void delegates_to_agent_and_returns_success() {
        // GIVEN
        final var agentId = Id.of("test_agent");
        final var input = jsonObjectNode().put("task", "do something");
        final var expectedOutput = jsonObjectNode().put("result", "done");
        final var agent = mock(DefaultAgent.class);
        given(agent.execute(input)).willReturn(Result.Success.of(expectedOutput));
        final var agentMap = Map.of(agentId, agent);
        final var tool = new RunAgentTool(agentMap);

        // WHEN
        final var result = tool.execute(new RunAgentTool.Input(agentId, input));

        // THEN
        assertThat(result).isInstanceOf(Result.Success.class);
        final var success = (Result.Success<JsonNode>) result;
        assertThat(success.value()).isEqualTo(expectedOutput);
        verify(agent).execute(input);
    }

    @Test
    void propagates_failure_from_agent() {
        // GIVEN
        final var agentId = Id.of("test_agent");
        final var input = jsonObjectNode().put("task", "do something");
        final var agent = mock(DefaultAgent.class);
        given(agent.execute(input)).willReturn(Result.Failure.of(new OrcException("agent failed")));
        final var agentMap = Map.of(agentId, agent);
        final var tool = new RunAgentTool(agentMap);

        // WHEN
        final var result = tool.execute(new RunAgentTool.Input(agentId, input));

        // THEN
        assertThat(result).isInstanceOf(Result.Failure.class);
        final var failure = (Result.Failure<JsonNode>) result;
        assertThat(failure.value().getMessage()).isEqualTo("agent failed");
    }

    @Test
    void returns_failure_when_agent_not_found() {
        // GIVEN
        final var agentId = Id.of("unknown_agent");
        final var input = jsonObjectNode().put("task", "do something");
        final var agentMap = Map.<Id, DefaultAgent>of();
        final var tool = new RunAgentTool(agentMap);

        // WHEN
        final var result = tool.execute(new RunAgentTool.Input(agentId, input));

        // THEN
        assertThat(result).isInstanceOf(Result.Failure.class);
        final var failure = (Result.Failure<JsonNode>) result;
        assertThat(failure.value().getMessage()).isEqualTo("Agent not found: unknown_agent");
    }

    @Nested
    class ParseArguments {

        @Test
        void extracts_agent_id_and_input() {
            // GIVEN
            final var agentMap = Map.<Id, DefaultAgent>of();
            final var tool = new RunAgentTool(agentMap);
            final var node = jsonObjectNode()
                .put("agentId", "developer_agent")
                .set("input", jsonObjectNode().put("task", "write code"));

            // WHEN
            final var input = tool.parseArguments(node);

            // THEN
            assertThat(input.agentId()).isEqualTo(Id.of("developer_agent"));
            assertThat(input.input()).isEqualTo(node.get("input"));
        }

        @Test
        void throws_when_agentId_missing() {
            // GIVEN
            final var tool = new RunAgentTool(Map.of());
            final var node = jsonObjectNode().set("input", jsonObjectNode());

            // WHEN / THEN
            assertThatThrownBy(() -> tool.parseArguments(node))
                .isInstanceOf(OrcException.class)
                .hasMessage("'agentId' is required and must be a string");
        }

        @Test
        void throws_when_input_missing() {
            // GIVEN
            final var tool = new RunAgentTool(Map.of());
            final var node = jsonObjectNode().put("agentId", "some_agent");

            // WHEN / THEN
            assertThatThrownBy(() -> tool.parseArguments(node))
                .isInstanceOf(OrcException.class)
                .hasMessage("'input' is required");
        }

        @Test
        void throws_when_node_not_object() {
            // GIVEN
            final var tool = new RunAgentTool(Map.of());
            final var node = jsonObjectNode().put("not_an_object", "value");

            // WHEN / THEN
            assertThatThrownBy(() -> tool.parseArguments(node))
                .isInstanceOf(OrcException.class)
                .hasMessage("'agentId' is required and must be a string");
        }

    }

}