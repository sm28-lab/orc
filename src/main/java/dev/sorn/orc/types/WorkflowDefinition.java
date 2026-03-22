package dev.sorn.orc.types;

import io.vavr.collection.List;

public record WorkflowDefinition(
    Id id,
    String description,
    List<Id> entryPoints
) {

    private WorkflowDefinition(Builder builder) {
        this(
            builder.id,
            builder.description,
            builder.entryPoints
        );
    }

    public static final class Builder {
        private Id id;
        private String description;
        private List<Id> entryPoints = List.empty();

        private Builder() {}

        public static Builder workflowDefinition() {
            return new Builder();
        }

        public Builder id(Id id) {
            this.id = id;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder entryPoints(List<Id> entryPoints) {
            this.entryPoints = entryPoints;
            return this;
        }

        public WorkflowDefinition build() {
            return new WorkflowDefinition(this);
        }

    }

}
