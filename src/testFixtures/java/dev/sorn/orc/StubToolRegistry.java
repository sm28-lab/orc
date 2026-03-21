package dev.sorn.orc;

import dev.sorn.orc.api.Tool;
import dev.sorn.orc.api.ToolRegistry;
import dev.sorn.orc.errors.OrcException;
import dev.sorn.orc.tools.FileReaderTool;
import dev.sorn.orc.tools.FileWriterTool;
import dev.sorn.orc.tools.GrepTool;
import dev.sorn.orc.tools.ListDirectoryContentsTool;
import dev.sorn.orc.tools.PrintWorkingDirectoryTool;
import dev.sorn.orc.types.Id;

import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StubToolRegistry implements ToolRegistry {

    private final Map<Id, Tool<?, ?>> tools = new ConcurrentHashMap<>();

    public StubToolRegistry() {
        register(new FileReaderTool(Files::newBufferedReader));
        register(new ListDirectoryContentsTool());
        register(new PrintWorkingDirectoryTool());
        register(new GrepTool());
        register(new FileWriterTool());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <I, O> Tool<I, O> get(Id id) {
        var tool = tools.get(id);
        if (tool == null) {
            throw new OrcException("'%s' tool is not registered", id.value());
        }
        return (Tool<I, O>) tool;
    }

    @Override
    public <I, O> void register(Tool<I, O> tool) {
        var id = tool.id();
        if (tools.containsKey(id)) {
            throw new OrcException("'%s' tool is already registered", id.value());
        }
        tools.put(id, tool);
    }

    public int size() {
        return tools.size();
    }
}