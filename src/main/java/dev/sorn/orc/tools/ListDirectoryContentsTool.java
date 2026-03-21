package dev.sorn.orc.tools;

import dev.sorn.orc.api.Tool;
import dev.sorn.orc.errors.OrcException;
import dev.sorn.orc.api.Result;
import dev.sorn.orc.types.Id;
import dev.sorn.orc.api.Result.Empty;
import dev.sorn.orc.api.Result.Failure;
import dev.sorn.orc.api.Result.Success;

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
            try (final var stream = list(directory)) {
                final var list = stream
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .toList();
                return list.isEmpty() ? Empty.of() : Success.of(list);
            }
        } catch (Exception e) {
            return Failure.of(new OrcException(e));
        }
    }

}
