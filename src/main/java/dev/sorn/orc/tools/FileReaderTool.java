package dev.sorn.orc.tools;

import dev.sorn.orc.api.ReaderFactory;
import dev.sorn.orc.api.Result;
import dev.sorn.orc.api.Tool;
import dev.sorn.orc.errors.OrcException;
import dev.sorn.orc.types.Id;
import dev.sorn.orc.types.LineNumber;
import dev.sorn.orc.types.LineNumberRange;
import tools.jackson.databind.JsonNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import static java.lang.Integer.MAX_VALUE;
import static java.util.stream.Collectors.joining;

public final class FileReaderTool implements Tool<FileReaderTool.Input, String> {

    public static final Id FILE_READER_TOOL_ID = Id.of("file_reader_tool");

    private final ReaderFactory readerFactory;

    public FileReaderTool(ReaderFactory readerFactory) {
        this.readerFactory = readerFactory;
    }

    @Override
    public Id id() {
        return FILE_READER_TOOL_ID;
    }

    @Override
    public Result<String> execute(Input input) {
        final var path = input.path();
        final var range = input.lineNumberRange();
        final var from = range.from().map(LineNumber::value).getOrElse(1);
        final var to = range.to().map(LineNumber::value).getOrElse(MAX_VALUE);
        try (final var reader = new BufferedReader(readerFactory.create(path))) {
            final var result = reader.lines()
                .skip(from - 1)
                .limit(to - from)
                .collect(joining("\n"));
            return Result.Success.of(result);
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
            throw new OrcException("FileReaderTool expects an object with 'path' and optional 'lineNumberRange'");
        }
        final var path = Path.of(node.get("path").asText());
        final var rangeNode = node.get("lineNumberRange");
        final var range = parseRange(rangeNode);
        return new Input(path, range);
    }

    private static LineNumberRange parseRange(JsonNode rangeNode) {
        if (rangeNode == null || rangeNode.isNull()) {
            return LineNumberRange.empty();
        }
        if (!rangeNode.isObject()) {
            throw new OrcException("lineNumberRange must be an object");
        }
        final var fromNode = rangeNode.get("from");
        final var toNode = rangeNode.get("to");
        if (fromNode != null && !fromNode.isNull() && toNode != null && !toNode.isNull()) {
            return LineNumberRange.of(fromNode.asInt(), toNode.asInt());
        }
        if (fromNode != null && !fromNode.isNull()) {
            return LineNumberRange.from(LineNumber.of(fromNode.asInt()));
        }
        if (toNode != null && !toNode.isNull()) {
            return LineNumberRange.to(LineNumber.of(toNode.asInt()));
        }
        return LineNumberRange.empty();
    }

    @Override
    public String inputDescription() {
        return """
            An object with:
            - "path": string (required)
            - "lineNumberRange": optional object with "from" and/or "to" (integers, 1-based inclusive/exclusive)
            Example: {"path": "/tmp/file.txt", "lineNumberRange": {"from": 1, "to": 10}}
            """;
    }

    public record Input(Path path, LineNumberRange lineNumberRange) {}

}
