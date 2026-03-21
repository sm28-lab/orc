package dev.sorn.orc.types;

import io.vavr.collection.List;

public record AgentData(
    String name,
    Type type
) {

    public enum Type {
        BOOLEAN(Boolean.class),
        COLLECTION(List.class),
        STRING(String.class);

        private final Class<?> javaClass;

        Type(Class<?> javaClass) {
            this.javaClass = javaClass;
        }

        public Class<?> javaClass() {
            return javaClass;
        }
    }

}
