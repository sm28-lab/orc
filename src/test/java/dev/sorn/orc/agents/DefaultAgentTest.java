package dev.sorn.orc.agents;

import dev.sorn.orc.DefaultAgentTestData;
import dev.sorn.orc.api.LlmClient;
import dev.sorn.orc.api.ToolRegistry;
import dev.sorn.orc.types.AgentDefinition;
import dev.sorn.orc.types.BddInstruction;
import dev.sorn.orc.types.Id;
import io.vavr.collection.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static dev.sorn.orc.agents.DefaultAgent.Builder.defaultAgent;
import static dev.sorn.orc.api.Result.Success;
import static dev.sorn.orc.types.AgentRole.WORKER;
import static dev.sorn.orc.types.Id.of;
import static io.vavr.collection.List.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

class DefaultAgentTest implements DefaultAgentTestData {

    private final AgentDefinition definition = mock(AgentDefinition.class);
    private final ToolRegistry toolRegistry = mock(ToolRegistry.class);
    private final LlmClient llmClient = mock(LlmClient.class);
    private final DefaultAgent agent = defaultAgent()
        .agentDefinition(definition)
        .toolRegistry(toolRegistry)
        .llmClient(llmClient)
        .build();

    @Test
    void completes_prompt_with_instructions() {
        // GIVEN
        var prompt = "My prompt";
        given(definition.id())
            .willReturn(of("agent1"));
        given(definition.role())
            .willReturn(WORKER);
        given(definition.toolIds())
            .willReturn(empty());
        given(definition.inputs())
            .willReturn(empty());
        given(definition.outputs())
            .willReturn(empty());
        given(definition.instructions())
            .willReturn(List.of(BddInstruction.given("Instruction1"), BddInstruction.when("Instruction2")));
        given(llmClient.complete(anyString()))
            .willReturn(Success.of("Some LLM response"));

        // WHEN
        var result = agent.complete(prompt);

        // THEN
        then(llmClient)
            .should(times(1))
            .complete("""
                ## Instructions
                GIVEN: Instruction1
                WHEN: Instruction2

                ## Tool Usage Rules
                You can use tools by outputting a tool call in the following strict format:
                <tool_call>
                {
                  "tool": "tool_id",
                  "arguments": {
                    ... arguments as JSON ...
                  }
                }
                </tool_call>
                You may output multiple tool calls. After each tool call you will receive the result. Then you can output more tool calls or the final answer.

                ## User Input
                My prompt
                """);
        result.fold(
            val -> assertThat(val).isEqualTo("Some LLM response"),
            err -> {
                throw new AssertionError("Expected success but got failure", err);
            });
    }

    @Test
    void equals_and_hash_code_depend_on_id() {
        // GIVEN
        var def1 = anAgentDefinition()
            .id(Id.of("some_agent_1"))
            .build();
        var def2 = anAgentDefinition()
            .id(Id.of("some_agent_2"))
            .build();

        // WHEN / THEN
        EqualsVerifier.forClass(DefaultAgent.class)
            .withPrefabValues(AgentDefinition.class, def1, def2)
            .withNonnullFields("agentDefinition", "toolRegistry", "llmClient")
            .withIgnoredFields("toolRegistry", "llmClient")
            .verify();
    }

}
