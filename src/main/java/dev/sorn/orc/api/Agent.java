package dev.sorn.orc.api;

import dev.sorn.orc.types.AgentData;
import dev.sorn.orc.types.AgentRole;
import dev.sorn.orc.types.Id;
import io.vavr.collection.List;

public interface Agent {

    Id id();

    AgentRole role();

    List<Tool<?, ?>> tools();

    List<AgentData> input();

    List<AgentData> output();

    List<String> instructions();

}
