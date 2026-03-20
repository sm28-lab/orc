package dev.sorn.orc.api;

import dev.sorn.orc.types.Result;

public interface Tool<I, O> {

    default Result<O> execute() {
        if (!Void.class.equals(inputType())) {
            throw new UnsupportedOperationException("No-arg execute() is only supported for Tool<Void, O>");
        }
        return execute(null);
    }

    Result<O> execute(I input);

    Class<I> inputType();

}
