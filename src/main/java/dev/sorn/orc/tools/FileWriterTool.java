package dev.sorn.orc.tools;

import dev.sorn.orc.api.Result;
import dev.sorn.orc.api.Tool;
import dev.sorn.orc.errors.OrcException;
import dev.sorn.orc.types.Id;
import tools.jackson.databind.JsonNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class FileWriterTool implements Tool<FileWriterTool.Input, String> {

    public static final Id FILE_WRITER_TOOL_ID = Id.of("file_writer_tool");

    @Override
    public Id id() {
        return FILE_WRITER_TOOL_ID;
    }

    @Override
    public Result<String> execute(Input input) {
        try {
            final var path = input.path();
            final var content = input.content();

            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            final var existedBefore = Files.exists(path);
            final var options = existedBefore
                ? new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND}
                : new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};

            Files.write(path, content.getBytes(), options);

            final var message = String.format("File written to: %s (%d bytes)%s",
                path.toAbsolutePath(),
                content.length(),
                existedBefore ? " (appended)" : "");

            return Result.Success.of(message);
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
            throw new OrcException("FileWriterTool expects an object with 'path' and 'content' fields");
        }

        final var pathNode = node.get("path");
        final var contentNode = node.get("content");

        if (pathNode == null || pathNode.isNull()) {
            throw new OrcException("'path' is required");
        }
        if (contentNode == null || contentNode.isNull()) {
            throw new OrcException("'content' is required");
        }

        final var path = Path.of(pathNode.asText());
        final var content = contentNode.asText();

        return new Input(path, content);
    }

    @Override
    public String inputDescription() {
        return """
            Writes content to a file. Creates parent directories if needed.
            If the file already exists, the content is appended; otherwise a new file is created.
            Input object:
            - "path": string (required) – file to write
            - "content": string (required) – content to write
            Example: {"path": "/project/README.md", "content": "# Project Title"}
            """;
    }

    public record Input(Path path, String content) {}

}
