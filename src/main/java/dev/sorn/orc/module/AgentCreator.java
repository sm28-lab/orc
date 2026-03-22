package dev.sorn.orc.module;

import dev.sorn.orc.agents.DefaultAgent;
import dev.sorn.orc.api.JsonHttpClient;
import dev.sorn.orc.clients.OllamaClient;
import dev.sorn.orc.tools.RunAgentTool;
import dev.sorn.orc.types.Id;
import dev.sorn.orc.types.WorkflowDefinition;
import io.vavr.collection.List;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static dev.sorn.orc.agents.DefaultAgent.Builder.defaultAgent;
import static java.nio.file.Files.readString;

public final class AgentCreator {

    private static final Path DEFINITION_PATH = Path.of("src/main/resources/agents.def.json");

    private final AppToolRegistry toolRegistry;
    private final JsonHttpClient jsonHttpClient;

    public AgentCreator(AppToolRegistry toolRegistry, JsonHttpClient jsonHttpClient) {
        this.toolRegistry = toolRegistry;
        this.jsonHttpClient = jsonHttpClient;
    }

    public Map<Id, DefaultAgent> createAgents() throws IOException {
        final var json = readString(DEFINITION_PATH);
        final var factory = new AgentFactory();
        final var definitions = factory.loadFromJson(json);
        final var agentMap = new ConcurrentHashMap<Id, DefaultAgent>();

        definitions.forEach(def -> {
            final var llmClient = new OllamaClient(
                Id.of(def.modelId()),
                jsonHttpClient,
                URI.create(def.baseUrl()),
                def.maxTokens()
            );
            final var agent = defaultAgent()
                .agentDefinition(def)
                .toolRegistry(toolRegistry)
                .llmClient(llmClient)
                .build();
            agentMap.put(def.id(), agent);
        });

        toolRegistry.register(new RunAgentTool(agentMap));

        return agentMap;
    }

    public List<WorkflowDefinition> loadWorkflows() throws IOException {
        final var json = readString(DEFINITION_PATH);
        final var factory = new AgentFactory();
        return factory.loadWorkflowsFromJson(json);
    }

}
