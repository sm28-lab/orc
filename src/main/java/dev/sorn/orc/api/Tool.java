package dev.sorn.orc.api;

import dev.sorn.orc.types.Id;

public interface Tool<I, O> {

    Id id();

    Result<O> execute(I input);

}
