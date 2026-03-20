package dev.sorn.orc.tools;

import dev.sorn.orc.api.Tool;
import dev.sorn.orc.types.Result;

import java.nio.file.Path;

import static dev.sorn.orc.types.Result.ok;
import static java.lang.System.getProperty;

public class PrintWorkingDirectoryTool implements Tool<Void, Path> {

    @Override
    public Result<Path> execute(Void input) {
        return ok(Path.of(getProperty("user.dir")));
    }

    @Override
    public Class<Void> inputType() {
        return Void.class;
    }

}
