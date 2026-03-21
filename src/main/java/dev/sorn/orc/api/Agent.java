package dev.sorn.orc.api;

import dev.sorn.orc.types.AgentData;
import dev.sorn.orc.types.AgentRole;
import dev.sorn.orc.types.BddInstruction;
import dev.sorn.orc.types.Id;
import io.vavr.collection.List;

public interface Agent {

    Result<String> complete(String prompt);

    Id id();

    AgentRole role();

    List<Tool<?, ?>> tools();

    List<AgentData> inputs();

    List<AgentData> outputs();

    List<BddInstruction> instructions();

}