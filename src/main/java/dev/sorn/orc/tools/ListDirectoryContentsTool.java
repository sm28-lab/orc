package dev.sorn.orc.tools;

import dev.sorn.orc.api.Tool;
import dev.sorn.orc.errors.Error;
import dev.sorn.orc.types.Result;
import dev.sorn.orc.types.Id;

import java.nio.file.Path;
import java.util.List;

import static dev.sorn.orc.types.Result.empty;
import static dev.sorn.orc.types.Result.error;
import static dev.sorn.orc.types.Result.ok;
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
                throw new Error("'%s' directory not found", directory);
            }
            if (!isDirectory(directory)) {
                throw new Error("'%s' is not a directory", directory);
            }
            try (final var stream = list(directory)) {
                final var list = stream
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .toList();
                return list.isEmpty() ? empty() : ok(list);
            }
        } catch (Exception e) {
            return error(new Error(e));
        }
    }

    @Override
    public Class<Path> inputType() {
        return Path.class;
    }

}
