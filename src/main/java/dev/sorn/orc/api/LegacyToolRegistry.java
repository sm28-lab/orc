package dev.sorn.orc.api;

import dev.sorn.orc.types.Id;

public interface LegacyToolRegistry {

    <I, O> LegacyTool<I, O> get(Id id);

    <I, O> void register(LegacyTool<I, O> tool);

}
