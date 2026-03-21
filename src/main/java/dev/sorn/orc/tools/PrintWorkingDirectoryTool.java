package dev.sorn.orc.tools;

import dev.sorn.orc.api.Result;
import dev.sorn.orc.api.Tool;
import dev.sorn.orc.types.Id;
import tools.jackson.databind.JsonNode;
import java.nio.file.Path;
import static java.lang.System.getProperty;

public class PrintWorkingDirectoryTool implements Tool<Void, Path> {

    public static final Id PRINT_WORKING_DIRECTORY_TOOL_ID = Id.of("print_working_directory_tool");

    @Override
    public Id id() {
        return PRINT_WORKING_DIRECTORY_TOOL_ID;
    }

    @Override
    public Result<Path> execute(Void input) {
        return Result.Success.of(Path.of(getProperty("user.dir")));
    }

    @Override
    public Class<Void> inputType() {
        return Void.class;
    }

    @Override
    public Void parseArguments(JsonNode node) {
        return null;
    }

    @Override
    public String inputDescription() {
        return "No arguments needed (can be null or empty object).";
    }

}
