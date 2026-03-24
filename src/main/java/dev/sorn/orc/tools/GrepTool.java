package dev.sorn.orc.tools;

import dev.sorn.orc.api.Result;
import dev.sorn.orc.api.LegacyTool;
import dev.sorn.orc.errors.OrcException;
import dev.sorn.orc.types.Id;
import dev.sorn.orc.types.LineNumber;
import io.vavr.collection.List;
import tools.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import static java.nio.file.Files.lines;

public final class GrepTool implements LegacyTool<GrepTool.Input, List<GrepTool.Match>> {

    public static final Id GREP_TOOL_ID = Id.of("grep_tool");

    @Override
    public Id id() {
        return GREP_TOOL_ID;
    }

    @Override
    public Result<List<Match>> execute(Input input) {
        try {
            final var pattern = input.caseSensitive()
                ? Pattern.compile(input.pattern())
                : Pattern.compile(input.pattern(), Pattern.CASE_INSENSITIVE);

            var matches = List.<Match>empty();
            final var walk = Files.walk(input.path());

            try (walk) {
                final var paths = walk.filter(Files::isRegularFile).iterator();
                while (paths.hasNext()) {
                    final var file = paths.next();
                    try (final var lines = lines(file)) {
                        final var iterator = lines.iterator();
                        var lineNumber = 0;
                        while (iterator.hasNext()) {
                            lineNumber++;
                            final var line = iterator.next();
                            if (pattern.matcher(line).find()) {
                                matches = matches.append(new Match(file, LineNumber.of(lineNumber)));
                            }
                        }
                    } catch (UncheckedIOException e) {
                        continue;
                    }
                }
            }
            return Result.Success.of(matches);
        } catch (IOException e) {
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
            throw new OrcException("GrepTool expects an object with 'pattern' and 'path' fields");
        }
        final var pattern = node.get("pattern").asText();
        final var path = Path.of(node.get("path").asText());
        final var recursive = node.has("recursive") && node.get("recursive").asBoolean();
        final var caseSensitive = !node.has("caseSensitive") || node.get("caseSensitive").asBoolean();
        return new Input(pattern, path, recursive, caseSensitive);
    }

    @Override
    public String inputDescription() {
        return """
            Searches for a pattern (regular expression) in files. Can search recursively.
            This is the recommended tool for locating files by name or content, especially for large directories.
            Input object:
            - "pattern": string (required)
            - "path": string (required) – file or directory to search
            - "recursive": boolean (optional, default true) – search subdirectories
            - "caseSensitive": boolean (optional, default true)
            Example: {"pattern": "AgentData", "path": "/src", "recursive": true}
            """;
    }

    public record Input(String pattern, Path path, boolean recursive, boolean caseSensitive) {}

    public record Match(Path file, LineNumber lineNumber) {}

}
