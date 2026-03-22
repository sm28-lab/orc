package dev.sorn.orc.workflows;

import dev.sorn.orc.agents.DefaultAgent;
import dev.sorn.orc.api.OutputPrinter;
import dev.sorn.orc.api.Result;
import dev.sorn.orc.types.AgentTrigger;
import dev.sorn.orc.types.Id;
import dev.sorn.orc.types.WorkflowDefinition;
import io.vavr.collection.HashSet;
import tools.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import static dev.sorn.orc.json.Json.jsonObjectNode;

public final class WorkflowExecutor {

    private final Map<Id, DefaultAgent> agentMap;
    private final OutputPrinter printer;

    public WorkflowExecutor(Map<Id, DefaultAgent> agentMap, OutputPrinter printer) {
        this.agentMap = agentMap;
        this.printer = printer;
    }

    public void execute(WorkflowDefinition workflow, JsonNode initialInput) {
        var visited = HashSet.<Id>empty();
        final var queue = new LinkedList<Id>();
        final var agentOutputs = new HashMap<Id, JsonNode>();
        var currentInput = initialInput;

        workflow.entryPoints().forEach(queue::offer);

        while (!queue.isEmpty()) {
            final var agentId = queue.poll();

            if (visited.contains(agentId)) {
                continue;
            }

            final var agent = agentMap.get(agentId);
            if (agent == null) {
                printer.printError("Agent not found: " + agentId.value());
                continue;
            }

            visited = visited.add(agentId);
            printer.printInfo("\n--- Executing: " + agentId.value() + " ---");

            final var result = agent.execute(currentInput);
            final var output = result.fold(
                val -> {
                    printer.printResult(Result.Success.of(JsonNode.class.cast(val)));
                    return val;
                },
                err -> {
                    printer.printError("Agent failed: " + err.getMessage());
                    return null;
                }
            );

            if (output == null) {
                continue;
            }

            agentOutputs.put(agentId, output);

            final var finalVisited = visited;
            agent.triggers().forEach(trigger -> {
                if (shouldTrigger(trigger, output)) {
                    if (!finalVisited.contains(trigger.targetAgentId())) {
                        queue.offer(trigger.targetAgentId());
                        printer.printInfo("  -> Triggered: " + trigger.targetAgentId().value());
                    }
                }
            });

            currentInput = combineInputs(initialInput, agentOutputs);
        }
    }

    private static boolean shouldTrigger(AgentTrigger trigger, JsonNode output) {
        return switch (trigger.condition()) {
            case ALWAYS -> true;
            case ON_SUCCESS -> true;
            case ON_FAILURE -> false;
            case ON_OUTPUT -> {
                var field = trigger.outputField();
                if (field == null) yield true;
                var node = output.get(field);
                yield node != null && !node.isNull();
            }
        };
    }

    private static JsonNode combineInputs(JsonNode original, Map<Id, JsonNode> outputs) {
        var combined = jsonObjectNode();
        combined.set("original_input", original);
        var context = jsonObjectNode();
        outputs.forEach((id, out) -> context.set(id.value(), out));
        combined.set("previous_outputs", context);
        return combined;
    }

}
