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
        var path = input.path();
        var range = input.lineNumberRange();
        var from = range.from().map(LineNumber::value).getOrElse(1);
        var to = range.to().map(LineNumber::value).getOrElse(MAX_VALUE);
        try (var reader = new BufferedReader(readerFactory.create(path))) {
            var result = reader.lines()
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
        var path = Path.of(node.get("path").asText());
        var rangeNode = node.get("lineNumberRange");
        LineNumberRange range;
        if (rangeNode == null || rangeNode.isNull()) {
            range = LineNumberRange.empty();
        } else if (rangeNode.isObject()) {
            var fromNode = rangeNode.get("from");
            var toNode = rangeNode.get("to");
            if (fromNode != null && !fromNode.isNull() && toNode != null && !toNode.isNull()) {
                range = LineNumberRange.of(fromNode.asInt(), toNode.asInt());
            } else if (fromNode != null && !fromNode.isNull()) {
                range = LineNumberRange.from(LineNumber.of(fromNode.asInt()));
            } else if (toNode != null && !toNode.isNull()) {
                range = LineNumberRange.to(LineNumber.of(toNode.asInt()));
            } else {
                range = LineNumberRange.empty();
            }
        } else {
            throw new OrcException("lineNumberRange must be an object");
        }
        return new Input(path, range);
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
