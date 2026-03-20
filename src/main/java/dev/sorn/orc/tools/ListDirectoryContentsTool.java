package dev.sorn.orc.tools;

import dev.sorn.orc.api.Tool;
import dev.sorn.orc.errors.ToolError;
import dev.sorn.orc.types.Result;

import java.nio.file.Path;
import java.util.List;

import static dev.sorn.orc.types.Result.empty;
import static dev.sorn.orc.types.Result.error;
import static dev.sorn.orc.types.Result.ok;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.list;

public final class ListDirectoryContentsTool implements Tool<Path, List<String>> {

    @Override
    public Result<List<String>> execute(Path directory) {
        try {
            if (!exists(directory)) {
                throw new ToolError("'%s' directory not found", directory);
            }
            if (!isDirectory(directory)) {
                throw new ToolError("'%s' is not a directory", directory);
            }
            try (final var stream = list(directory)) {
                final var list = stream
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .toList();
                return list.isEmpty() ? empty() : ok(list);
            }
        } catch (Exception e) {
            return error(new ToolError(e));
        }
    }

    @Override
    public Class<Path> inputType() {
        return Path.class;
    }

}
