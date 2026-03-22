package dev.sorn.orc.agents;

import dev.sorn.orc.api.Agent;
import dev.sorn.orc.api.LlmClient;
import dev.sorn.orc.api.Tool;
import dev.sorn.orc.api.ToolRegistry;
import dev.sorn.orc.types.AgentData;
import dev.sorn.orc.types.AgentDefinition;
import dev.sorn.orc.types.AgentRole;
import dev.sorn.orc.types.AgentTrigger;
import dev.sorn.orc.types.BddInstructionGroup;
import dev.sorn.orc.types.Id;
import io.vavr.collection.List;
import java.util.Objects;
import static java.util.Objects.hash;

public abstract class BaseAgent implements Agent {

    protected final AgentDefinition agentDefinition;
    protected final ToolRegistry toolRegistry;
    protected final LlmClient llmClient;

    protected BaseAgent(
        AgentDefinition agentDefinition,
        ToolRegistry toolRegistry,
        LlmClient llmClient
    ) {
        this.agentDefinition = agentDefinition;
        this.toolRegistry = toolRegistry;
        this.llmClient = llmClient;
    }

    @Override
    public final Id id() {
        return agentDefinition.id();
    }

    @Override
    public final AgentRole role() {
        return agentDefinition.role();
    }

    @Override
    public final List<AgentTrigger> triggers() {
        return agentDefinition.triggers();
    }

    @Override
    public final List<Tool<?, ?>> tools() {
        return agentDefinition.toolIds().map(toolRegistry::get);
    }

    @Override
    public final List<AgentData> inputs() {
        return agentDefinition.inputs();
    }

    @Override
    public final List<AgentData> outputs() {
        return agentDefinition.outputs();
    }

    @Override
    public final List<BddInstructionGroup> instructions() {
        return agentDefinition.instructions();
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof Agent that)) return false;
        return Objects.equals(this.id(), that.id());
    }

    @Override
    public int hashCode() {
        return hash(id());
    }

}