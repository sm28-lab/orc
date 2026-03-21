package dev.sorn.orc.tools;

import dev.sorn.orc.api.ReaderFactory;
import dev.sorn.orc.api.Tool;
import dev.sorn.orc.errors.OrcException;
import dev.sorn.orc.types.LineNumber;
import dev.sorn.orc.types.LineNumberRange;
import dev.sorn.orc.api.Result;
import dev.sorn.orc.types.Id;
import dev.sorn.orc.api.Result.Failure;
import dev.sorn.orc.api.Result.Success;

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
            return Success.of(result);
        } catch (IOException e) {
            return Failure.of(new OrcException(e));
        }
    }

    public record Input(
        Path path,
        LineNumberRange lineNumberRange
    ) {}

}
