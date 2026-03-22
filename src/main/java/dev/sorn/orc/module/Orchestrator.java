package dev.sorn.orc.module;

import dev.sorn.orc.agents.DefaultAgent;
import dev.sorn.orc.api.OutputPrinter;
import dev.sorn.orc.types.Id;
import dev.sorn.orc.types.WorkflowDefinition;
import dev.sorn.orc.workflows.WorkflowExecutor;
import io.vavr.collection.List;
import java.util.Map;
import static dev.sorn.orc.json.Json.jsonObjectNode;

public final class Orchestrator {

    private final Map<Id, DefaultAgent> agentMap;
    private final List<WorkflowDefinition> workflows;
    private final OutputPrinter printer;

    public Orchestrator(Map<Id, DefaultAgent> agentMap, List<WorkflowDefinition> workflows, OutputPrinter printer) {
        this.agentMap = agentMap;
        this.workflows = workflows;
        this.printer = printer;
    }

    public void run(String prompt) {
        final var input = jsonObjectNode().put("task", prompt);

        if (workflows.isEmpty()) {
            agentMap.values().forEach(agent -> {
                printer.printInfo("\nAgent: " + agent.id().value());
                printer.printInfo("Role: " + agent.role());
                printer.printInfo("--- Processing ---");
                var result = agent.execute(input);
                printer.printResult(result);
            });
        } else {
            workflows.forEach(workflow -> {
                printer.printInfo("\n=== Workflow: " + workflow.id().value() + " ===");
                printer.printInfo("Description: " + workflow.description());
                var executor = new WorkflowExecutor(agentMap, printer);
                executor.execute(workflow, input);
            });
        }
    }

}
