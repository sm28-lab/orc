package dev.sorn.orc.workflows;

import dev.sorn.orc.agents.DefaultAgent;
import dev.sorn.orc.api.OutputPrinter;
import dev.sorn.orc.api.Result;
import dev.sorn.orc.types.AgentTrigger;
import dev.sorn.orc.types.Id;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.JsonNode;
import java.util.Map;
import static dev.sorn.orc.json.Json.jsonArrayNode;
import static dev.sorn.orc.json.Json.jsonObjectNode;
import static dev.sorn.orc.types.AgentTrigger.TriggerCondition.ON_OUTPUT;
import static dev.sorn.orc.types.WorkflowDefinition.Builder.workflowDefinition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.captor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class WorkflowExecutorTest {

    private final DefaultAgent developerAgent = mock(DefaultAgent.class);
    private final DefaultAgent reviewerAgent = mock(DefaultAgent.class);
    private final OutputPrinter printer = mock(OutputPrinter.class);
    private final ArgumentCaptor<JsonNode> promptCaptor = captor();
    private final Id developerId = Id.of("developer_agent");
    private final Id reviewerId = Id.of("code_reviewer_agent");

    @Test
    void uses_initial_prompt_and_previous_context_for_triggered_agent() {
        // GIVEN
        var trigger = AgentTrigger.Builder.agentTrigger()
            .targetAgentId(reviewerId)
            .condition(ON_OUTPUT)
            .outputField("files_modified")
            .build();

        var initialInput = jsonObjectNode().put("task", "Write code for feature X");
        var developerOutput = jsonObjectNode()
            .set("files_modified", jsonArrayNode().add("Y"));

        given(developerAgent.id()).willReturn(developerId);
        given(developerAgent.triggers()).willReturn(List.of(trigger));
        given(developerAgent.execute(initialInput)).willReturn(Result.Success.of(developerOutput));

        given(reviewerAgent.id()).willReturn(reviewerId);
        given(reviewerAgent.triggers()).willReturn(List.empty());
        doReturn(Result.Success.of(jsonObjectNode())).when(reviewerAgent).execute(any());

        var workflow = workflowDefinition()
            .id(Id.of("test_workflow"))
            .entryPoints(List.of(developerId))
            .build();

        var agentMap = Map.of(developerId, developerAgent, reviewerId, reviewerAgent);
        var executor = new WorkflowExecutor(agentMap, printer);

        // WHEN
        executor.execute(workflow, initialInput);

        // THEN
        then(reviewerAgent).should().execute(promptCaptor.capture());
        var capturedInput = promptCaptor.getValue();
        assertThat(capturedInput.get("original_input")).isEqualTo(initialInput);
        assertThat(capturedInput.get("previous_outputs").get(developerId.value())).isEqualTo(developerOutput);
    }

}
