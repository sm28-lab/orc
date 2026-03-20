package dev.sorn.orc.module;

import dev.sorn.orc.api.Tool;
import dev.sorn.orc.api.ToolRegistry;
import dev.sorn.orc.errors.Error;
import dev.sorn.orc.types.Id;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AppToolRegistry implements ToolRegistry {

    private final Map<Id, Tool<?, ?>> tools = new ConcurrentHashMap<>();

    @Override
    public Tool<?, ?> get(Id id) {
        final var tool = tools.get(id);
        if (tool == null) {
            throw new Error("'%s' tool is not registered", id.value());
        }
        return tool;
    }

    @Override
    public <I, O> void register(Tool<I, O> tool) {
        tools.put(tool.id(), tool);
    }

    public int size() {
        return tools.size();
    }

}
