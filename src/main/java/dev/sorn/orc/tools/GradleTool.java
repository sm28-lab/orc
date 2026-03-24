package dev.sorn.orc.tools;

import dev.sorn.orc.api.Result;
import dev.sorn.orc.api.LegacyTool;
import dev.sorn.orc.errors.OrcException;
import dev.sorn.orc.types.Id;
import io.vavr.control.Option;
import tools.jackson.databind.JsonNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public final class GradleTool implements LegacyTool<GradleTool.Input, String> {

    public static final Id GRADLE_TOOL_ID = Id.of("gradle_tool");

    @Override
    public Id id() {
        return GRADLE_TOOL_ID;
    }

    @Override
    public Result<String> execute(Input input) {
        try {
            final var dir = input.projectDir()
                .map(Path::of)
                .getOrElse(Path.of("").toAbsolutePath().normalize());

            final var gradlew = dir.resolve("gradlew");
            if (!Files.exists(gradlew)) {
                return Result.Failure.of(new OrcException("gradlew not found at: " + gradlew));
            }
            if (!gradlew.toFile().canExecute()) {
                return Result.Failure.of(new OrcException("gradlew is not executable: " + gradlew));
            }

            final var args = input.command().split("\\s+");
            final var cmd = new ArrayList<String>();
            cmd.add(gradlew.toString());
            cmd.addAll(Arrays.asList(args));

            final var pb = new ProcessBuilder(cmd);
            pb.directory(dir.toFile());
            pb.redirectErrorStream(true);

            final var process = pb.start();
            final var output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            final var exitCode = process.waitFor();

            if (exitCode != 0) {
                return Result.Failure.of(new OrcException("Gradle command failed with exit code " + exitCode + "\nOutput:\n" + output));
            }

            return Result.Success.of(output);
        } catch (IOException | InterruptedException e) {
            return Result.Failure.of(new OrcException(e));
        }
    }

    @Override
    public Class<Input> inputType() {
        return Input.class;
    }

    @Override
    public Input parseArguments(JsonNode node) {
        if (!node.isObject()) {
            throw new OrcException("GradleTool expects an object with 'command' field");
        }

        final var commandNode = node.get("command");
        if (commandNode == null || !commandNode.isTextual()) {
            throw new OrcException("'command' field is required and must be a string");
        }

        final var command = commandNode.asText();
        final var projectDir = node.has("projectDir") && !node.get("projectDir").isNull()
            ? Option.some(node.get("projectDir").asText())
            : Option.<String>none();

        return new Input(command, projectDir);
    }

    @Override
    public String inputDescription() {
        return """
            Executes Gradle tasks using the project's gradlew script.
            Input object:
            - "command": string (required) – the Gradle tasks and options to run, e.g., "clean", "build", "test", "test --tests SomeTest"
            - "projectDir": string (optional) – directory containing gradlew. If not provided, uses current working directory.
            Example: {"command": "test --tests MyTest", "projectDir": "/path/to/project"}
            """;
    }

    public record Input(String command, Option<String> projectDir) {}
}