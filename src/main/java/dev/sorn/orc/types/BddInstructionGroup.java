package dev.sorn.orc.types;

import io.vavr.collection.List;
import static io.vavr.collection.List.empty;

public record BddInstructionGroup(
    List<String> given,
    List<String> when,
    List<String> then
) {

    private BddInstructionGroup(Builder builder) {
        this(builder.given, builder.when, builder.then);
    }

    public static final class Builder {
        private List<String> given = empty();
        private List<String> when = empty();
        private List<String> then = empty();

        private Builder() {}

        public static Builder bddInstructionGroup() {
            return new Builder();
        }

        public Builder given(List<String> given) {
            this.given = given;
            return this;
        }

        public Builder when(List<String> when) {
            this.when = when;
            return this;
        }

        public Builder then(List<String> then) {
            this.then = then;
            return this;
        }

        public BddInstructionGroup build() {
            return new BddInstructionGroup(this);
        }
    }

    public static BddInstructionGroup of(String given, String when, String then) {
        return new Builder()
            .given(List.of(given))
            .when(List.of(when))
            .then(List.of(then))
            .build();
    }

}
