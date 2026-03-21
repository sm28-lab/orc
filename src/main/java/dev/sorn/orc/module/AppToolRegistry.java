package dev.sorn.orc.module;

import dev.sorn.orc.api.Tool;
import dev.sorn.orc.api.ToolRegistry;
import dev.sorn.orc.errors.OrcException;
import dev.sorn.orc.types.Id;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AppToolRegistry implements ToolRegistry {

    private final Map<Id, Tool<?, ?>> tools = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <I, O> Tool<I, O> get(Id id) {
        final var tool = tools.get(id);
        if (tool == null) {
            throw new OrcException("'%s' tool is not registered", id.value());
        }
        return (Tool<I, O>) tool;
    }

    @Override
    public <I, O> void register(Tool<I, O> tool) {
        final var id = tool.id();
        if (tools.containsKey(id)) {
            throw new OrcException("'%s' tool is already registered", id.value());
        }
        tools.put(id, tool);
    }

    public int size() {
        return tools.size();
    }

}
