package dev.sorn.orc;

import dev.sorn.orc.agents.DefaultAgent;
import dev.sorn.orc.types.AgentDefinition;
import dev.sorn.orc.types.BddInstructionGroup;
import dev.sorn.orc.types.Id;
import io.vavr.collection.List;
import static dev.sorn.orc.agents.DefaultAgent.Builder.defaultAgent;
import static dev.sorn.orc.types.AgentData.Builder.agentData;
import static dev.sorn.orc.types.AgentData.Type.COLLECTION;
import static dev.sorn.orc.types.AgentData.Type.STRING;
import static dev.sorn.orc.types.AgentDefinition.Builder.agentDefinition;
import static dev.sorn.orc.types.AgentRole.WORKER;

public interface DefaultAgentTestData {

    default DefaultAgent.Builder aDefaultAgent() {
        return defaultAgent()
            .agentDefinition(anAgentDefinition().build())
            .toolRegistry(new StubToolRegistry())
            .llmClient(new StubLlmClient());
    }

    default AgentDefinition.Builder anAgentDefinition() {
        return agentDefinition()
            .id(Id.of("some_agent"))
            .role(WORKER)
            .toolIds(List.of(
                Id.of("file_reader_tool"),
                Id.of("list_directory_contents_tool"),
                Id.of("print_working_directory_tool")))
            .inputs(List.of(agentData().name("code").type(STRING).build()))
            .outputs(List.of(agentData().name("suggestions").type(COLLECTION).build()))
            .instructionGroups(List.of(
                BddInstructionGroup.of("some instruction 1", "some instruction 2", "some instruction 3")))
            .modelId("test-model")
            .baseUrl("http://test")
            .maxTokens(100);
    }

}
