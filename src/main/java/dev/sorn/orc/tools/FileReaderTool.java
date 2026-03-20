package dev.sorn.orc.tools;

import dev.sorn.orc.api.ReaderFactory;
import dev.sorn.orc.api.Tool;
import dev.sorn.orc.errors.ToolError;
import dev.sorn.orc.types.LineNumber;
import dev.sorn.orc.types.LineNumberRange;
import dev.sorn.orc.types.Result;
import dev.sorn.orc.types.ToolId;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;

import static dev.sorn.orc.types.Result.error;
import static dev.sorn.orc.types.Result.ok;
import static java.lang.Integer.MAX_VALUE;

public final class FileReaderTool implements Tool<FileReaderTool.Input, String> {

    public static final ToolId FILE_READER_TOOL_ID = ToolId.of("file_reader");

    private final ReaderFactory readerFactory;

    public FileReaderTool(ReaderFactory readerFactory) {
        this.readerFactory = readerFactory;
    }

    @Override
    public ToolId id() {
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
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
            return ok(result);
        } catch (IOException e) {
            return error(new ToolError(e));
        }
    }

    @Override
    public Class<Input> inputType() {
        return Input.class;
    }

    public record Input(
        Path path,
        LineNumberRange lineNumberRange
    ) {}

}
