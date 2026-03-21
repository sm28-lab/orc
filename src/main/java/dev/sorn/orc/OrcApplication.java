package dev.sorn.orc;

import dev.sorn.orc.clients.DefaultJsonHttpClient;
import dev.sorn.orc.clients.OllamaClient;
import dev.sorn.orc.module.AgentFactory;
import dev.sorn.orc.module.AppToolRegistry;
import dev.sorn.orc.tools.FileReaderTool;
import dev.sorn.orc.tools.ListDirectoryContentsTool;
import dev.sorn.orc.tools.PrintWorkingDirectoryTool;
import dev.sorn.orc.types.Id;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.sorn.orc.agents.DefaultAgent.Builder.defaultAgent;
import static java.nio.file.Files.readString;

public class OrcApplication {

    public static void main(String[] args) throws IOException {
        final var jsonPath = Path.of("src/main/resources/agents.def.json");
        final var json = readString(jsonPath);

        final var jsonHttpClient = new DefaultJsonHttpClient();

        final var registry = new AppToolRegistry();
        registry.register(new FileReaderTool(Files::newBufferedReader));
        registry.register(new ListDirectoryContentsTool());
        registry.register(new PrintWorkingDirectoryTool());

        final var agentFactory = new AgentFactory();
        final var agents = agentFactory.loadFromJson(json)
            .map(def -> {
                var llmClient = new OllamaClient(
                    Id.of(def.modelId()),
                    jsonHttpClient,
                    URI.create(def.baseUrl()),
                    def.maxTokens()
                );
                return defaultAgent()
                    .agentDefinition(def)
                    .toolRegistry(registry)
                    .llmClient(llmClient)
                    .build();
            });

        agents.forEach(agent -> {
            System.out.println("Agent: " + agent.id().value());
            System.out.println("Role: " + agent.role());
            System.out.println("Tools: " + agent.tools().map(t -> t.id().value()));
            System.out.println("Inputs: " + agent.inputs());
            System.out.println("Outputs: " + agent.outputs());
            System.out.println("Instructions: " + agent.instructions());

            var result = agent.complete("Find an review \"AgentData.java\"");

            result.fold(
                val -> {
                    if (val == null || val.isBlank()) {
                        System.out.println("[Empty response]");
                    } else {
                        System.out.println(val);
                    }
                    return val;
                },
                err -> {
                    System.err.println("Error: " + err.getMessage());
                    return err;
                });
        });
    }
}