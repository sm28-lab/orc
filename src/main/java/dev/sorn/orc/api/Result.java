package dev.sorn.orc.api;

import dev.sorn.orc.errors.OrcException;
import java.util.function.Function;

public sealed interface Result<T> permits Result.Success, Result.Failure {

    <R> R fold(
        Function<? super T, ? extends R> onSuccess,
        Function<? super OrcException, ? extends R> onFailure);

    record Success<T>(T value) implements Result<T> {
        public static <T> Success<T> of(T value) {
            return new Success<>(value);
        }

        @Override
        public <R> R fold(
            Function<? super T, ? extends R> onSuccess,
            Function<? super OrcException, ? extends R> onFailure
        ) {
            return onSuccess.apply(value);
        }
    }

    record Failure<T>(OrcException value) implements Result<T> {
        public static <T> Failure<T> of(OrcException value) {
            return new Failure<>(value);
        }

        @Override
        public <R> R fold(
            Function<? super T, ? extends R> onSuccess,
            Function<? super OrcException, ? extends R> onFailure
        ) {
            return onFailure.apply(value);
        }
    }

}
