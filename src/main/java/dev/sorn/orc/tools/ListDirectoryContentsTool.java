package dev.sorn.orc.tools;

import dev.sorn.orc.api.Result;
import dev.sorn.orc.api.Tool;
import dev.sorn.orc.errors.OrcException;
import dev.sorn.orc.types.Id;
import tools.jackson.databind.JsonNode;
import java.nio.file.Path;
import java.util.List;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.list;

public final class ListDirectoryContentsTool implements Tool<Path, List<String>> {

    public static final Id LIST_DIRECTORY_CONTENTS_TOOL_ID = Id.of("list_directory_contents_tool");

    @Override
    public Id id() {
        return LIST_DIRECTORY_CONTENTS_TOOL_ID;
    }

    @Override
    public Result<List<String>> execute(Path directory) {
        try {
            if (!exists(directory)) {
                throw new OrcException("'%s' directory not found", directory);
            }
            if (!isDirectory(directory)) {
                throw new OrcException("'%s' is not a directory", directory);
            }
            try (var stream = list(directory)) {
                var list = stream
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .toList();
                return Result.Success.of(list);
            }
        } catch (Exception e) {
            return Result.Failure.of(new OrcException(e));
        }
    }

    @Override
    public Class<Path> inputType() {
        return Path.class;
    }

    @Override
    public Path parseArguments(JsonNode node) {
        if (node.isTextual()) {
            return Path.of(node.asText());
        }
        if (node.isObject() && node.has("path")) {
            return Path.of(node.get("path").asText());
        }
        throw new OrcException("Expected string path or object with 'path' field");
    }

    @Override
    public String inputDescription() {
        return """
            Lists the immediate contents of a directory (non‑recursive). 
            For recursive file searches (by name or content), use the grep_tool instead.
            Input: the path to a directory as a string, or an object with a 'path' field.
            """;
    }

}
