package dev.sorn.orc.agents;

import dev.sorn.orc.api.Agent;
import dev.sorn.orc.api.AgentDefinition;
import dev.sorn.orc.api.Tool;
import dev.sorn.orc.api.ToolRegistry;
import dev.sorn.orc.types.AgentData;
import dev.sorn.orc.types.AgentRole;
import dev.sorn.orc.types.Id;
import io.vavr.collection.List;

public final class DefaultAgent implements Agent {

    private final Id id;
    private final AgentRole role;
    private final List<Tool<?, ?>> tools;
    private final List<AgentData> input;
    private final List<AgentData> output;
    private final List<String> instructions;

    public DefaultAgent(AgentDefinition def, ToolRegistry registry) {
        this.id = def.id();
        this.role = def.role();
        this.tools = def.toolIds().map(registry::get);
        this.input = def.input();
        this.output = def.output();
        this.instructions = def.instructions();
    }

    @Override
    public Id id() {
        return id;
    }

    @Override
    public AgentRole role() {
        return role;
    }

    @Override
    public List<Tool<?, ?>> tools() {
        return tools;
    }

    @Override
    public List<AgentData> input() {
        return input;
    }

    @Override
    public List<AgentData> output() {
        return output;
    }

    @Override
    public List<String> instructions() {
        return instructions;
    }

}
