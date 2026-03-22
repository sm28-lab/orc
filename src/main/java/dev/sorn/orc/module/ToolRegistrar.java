package dev.sorn.orc.module;

import dev.sorn.orc.tools.FileReaderTool;
import dev.sorn.orc.tools.FileWriterTool;
import dev.sorn.orc.tools.GradleTool;
import dev.sorn.orc.tools.GrepTool;
import dev.sorn.orc.tools.ListDirectoryContentsTool;
import dev.sorn.orc.tools.PrintWorkingDirectoryTool;
import java.nio.file.Files;

public final class ToolRegistrar {

    private ToolRegistrar() {}

    public static void registerAll(AppToolRegistry registry) {
        registry.register(new FileReaderTool(Files::newBufferedReader));
        registry.register(new FileWriterTool());
        registry.register(new GradleTool());
        registry.register(new GrepTool());
        registry.register(new ListDirectoryContentsTool());
        registry.register(new PrintWorkingDirectoryTool());
    }

}