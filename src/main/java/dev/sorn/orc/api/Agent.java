package dev.sorn.orc.api;

import dev.sorn.orc.types.AgentData;
import dev.sorn.orc.types.AgentRole;
import dev.sorn.orc.types.AgentTrigger;
import dev.sorn.orc.types.BddInstructionGroup;
import dev.sorn.orc.types.Id;
import io.vavr.collection.List;
import tools.jackson.databind.JsonNode;

public interface Agent {

    Result<JsonNode> execute(JsonNode input);

    Id id();

    AgentRole role();

    List<AgentTrigger> triggers();

    List<Tool<?, ?>> tools();

    List<AgentData> inputs();

    List<AgentData> outputs();

    List<BddInstructionGroup> instructions();

}
