package dev.sorn.orc;

import dev.sorn.orc.agents.DefaultAgent;
import dev.sorn.orc.module.AgentFactory;
import dev.sorn.orc.module.AppToolRegistry;
import dev.sorn.orc.tools.FileReaderTool;
import dev.sorn.orc.tools.ListDirectoryContentsTool;
import dev.sorn.orc.tools.PrintWorkingDirectoryTool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.readString;

public class OrcApplication {

    public static void main(String[] args) throws IOException {
        final var jsonPath = Path.of("src/main/resources/agents.def.json");
        final var json = readString(jsonPath);

        final var registry = new AppToolRegistry();
        registry.register(new FileReaderTool(Files::newBufferedReader));
        registry.register(new ListDirectoryContentsTool());
        registry.register(new PrintWorkingDirectoryTool());

        final var agentFactory = new AgentFactory();
        final var agents = agentFactory.loadFromJson(json)
            .map(def -> new DefaultAgent(def, registry));

        agents.forEach(agent -> {
            System.out.println("Agent: " + agent.id().value());
            System.out.println("Role: " + agent.role());
            System.out.println("Tools: " + agent.tools().map(t -> t.id().value()));
            System.out.println("Inputs: " + agent.input());
            System.out.println("Outputs: " + agent.output());
            System.out.println("Instructions: " + agent.instructions());
        });



    }

}
