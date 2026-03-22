package dev.sorn.orc;

import dev.sorn.orc.cli.CommandLinePrinter;
import dev.sorn.orc.clients.DefaultJsonHttpClient;
import dev.sorn.orc.module.AgentCreator;
import dev.sorn.orc.module.AppToolRegistry;
import dev.sorn.orc.module.Orchestrator;
import dev.sorn.orc.module.PromptResolver;
import dev.sorn.orc.module.ToolRegistrar;
import java.io.IOException;

public final class OrcApplication {

    public static void main(String[] args) throws IOException {
        var prompt = PromptResolver.resolve(args);
        var toolRegistry = new AppToolRegistry();
        ToolRegistrar.registerAll(toolRegistry);
        var jsonHttpClient = new DefaultJsonHttpClient();
        var agentCreator = new AgentCreator(toolRegistry, jsonHttpClient);
        var agents = agentCreator.createAgents();
        var workflows = agentCreator.loadWorkflows();
        var printer = new CommandLinePrinter();
        var orchestrator = new Orchestrator(agents, workflows, printer);
        orchestrator.run(prompt);
    }

}