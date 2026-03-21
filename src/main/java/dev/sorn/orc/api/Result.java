package dev.sorn.orc.api;

import dev.sorn.orc.errors.OrcException;

import java.util.function.Function;
import java.util.function.Supplier;


public sealed interface Result<T> permits Result.Success, Result.Failure, Result.Empty {

    <R> R fold(
        Function<? super T, ? extends R> onSuccess,
        Function<? super OrcException, ? extends R> onFailure,
        Supplier<? extends R> onEmpty);

    record Success<T>(T value) implements Result<T> {
        public static <T> Success<T> of(T value) {
            return new Success<>(value);
        }

        @Override
        public <R> R fold(
            Function<? super T, ? extends R> onSuccess,
            Function<? super OrcException, ? extends R> onFailure,
            Supplier<? extends R> onEmpty
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
            Function<? super OrcException, ? extends R> onFailure,
            Supplier<? extends R> onEmpty
        ) {
            return onFailure.apply(value);
        }
    }

    record Empty<T>() implements Result<T> {
        public static <T> Empty<T> of() {
            return new Empty<>();
        }

        @Override
        public <R> R fold(
            Function<? super T, ? extends R> onSuccess,
            Function<? super OrcException, ? extends R> onFailure,
            Supplier<? extends R> onEmpty
        ) {
            return onEmpty.get();
        }
    }

}
