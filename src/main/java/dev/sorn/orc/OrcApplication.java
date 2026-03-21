package dev.sorn.orc;

import dev.sorn.orc.clients.DefaultJsonHttpClient;
import dev.sorn.orc.clients.OllamaClient;
import dev.sorn.orc.module.AgentFactory;
import dev.sorn.orc.module.AppToolRegistry;
import dev.sorn.orc.tools.FileReaderTool;
import dev.sorn.orc.tools.FileWriterTool;
import dev.sorn.orc.tools.GrepTool;
import dev.sorn.orc.tools.ListDirectoryContentsTool;
import dev.sorn.orc.tools.PrintWorkingDirectoryTool;
import dev.sorn.orc.types.Id;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import static dev.sorn.orc.agents.DefaultAgent.Builder.defaultAgent;
import static java.nio.file.Files.readString;

public class OrcApplication {

    public static void main(String[] args) throws IOException {
        String prompt;
        if (args.length > 0) {
            prompt = String.join(" ", args);
        } else {
            System.out.print("Enter your prompt: ");
            try (Scanner scanner = new Scanner(System.in)) {
                prompt = scanner.nextLine();
            }
        }

        final var jsonPath = Path.of("src/main/resources/agents.def.json");
        final var json = readString(jsonPath);

        final var jsonHttpClient = new DefaultJsonHttpClient();

        final var registry = new AppToolRegistry();
        registry.register(new FileReaderTool(Files::newBufferedReader));
        registry.register(new ListDirectoryContentsTool());
        registry.register(new PrintWorkingDirectoryTool());
        registry.register(new GrepTool());
        registry.register(new FileWriterTool());

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
                    .progressConsumer(createProgressConsumer())
                    .build();
            });

        agents.forEach(agent -> {
            System.out.println("\nAgent: " + agent.id().value());
            System.out.println("Role: " + agent.role());
            System.out.println("Tools: " + agent.tools().map(t -> t.id().value()));
            System.out.println("Inputs: " + agent.inputs());
            System.out.println("Outputs: " + agent.outputs());
            System.out.println("Instructions: " + agent.instructions());
            System.out.println("User prompt: " + prompt);
            System.out.println("--- Processing ---");

            var result = agent.complete(prompt);

            System.out.print("\r\033[2K");
            result.fold(
                val -> {
                    if (val == null || val.isBlank()) {
                        System.out.println("[Empty response]");
                    } else {
                        System.out.println("\n--- Result ---\n" + val);
                    }
                    return val;
                },
                err -> {
                    System.err.println("\nError: " + err.getMessage());
                    return err;
                });
        });
    }

    private static Consumer<String> createProgressConsumer() {
        final var spinner = new char[]{'⠋', '⠙', '⠹', '⠸', '⠼', '⠴', '⠦', '⠧'};
        final var idx = new AtomicInteger(0);
        return message -> {
            System.out.print("\r\033[2K");
            var frame = spinner[idx.getAndIncrement() % spinner.length];
            System.out.print("[" + frame + "] " + message);
            System.out.flush();
        };
    }

}
