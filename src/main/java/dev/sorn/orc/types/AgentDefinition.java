package dev.sorn.orc.types;

import io.vavr.collection.List;

public record AgentDefinition(
    Id id,
    AgentRole role,
    List<AgentTrigger> triggers,
    List<Id> toolIds,
    List<AgentData> inputs,
    List<AgentData> outputs,
    List<BddInstructionGroup> instructions,
    String modelId,
    String baseUrl,
    int maxTokens
) {

    private AgentDefinition(Builder builder) {
        this(
            builder.id,
            builder.role,
            builder.triggers,
            builder.toolIds,
            builder.inputs,
            builder.outputs,
            builder.instructionGroups,
            builder.modelId,
            builder.baseUrl,
            builder.maxTokens);
    }

    public static final class Builder {
        private Id id;
        private AgentRole role;
        private List<AgentTrigger> triggers = List.empty();
        private List<Id> toolIds;
        private List<AgentData> inputs;
        private List<AgentData> outputs;
        private List<BddInstructionGroup> instructionGroups;
        private String modelId;
        private String baseUrl;
        private int maxTokens = 2048;

        private Builder() {}

        public static Builder agentDefinition() {
            return new Builder();
        }

        public Builder id(Id id) {
            this.id = id;
            return this;
        }

        public Builder role(AgentRole role) {
            this.role = role;
            return this;
        }

        public Builder toolIds(List<Id> toolIds) {
            this.toolIds = toolIds;
            return this;
        }

        public Builder inputs(List<AgentData> inputs) {
            this.inputs = inputs;
            return this;
        }

        public Builder outputs(List<AgentData> outputs) {
            this.outputs = outputs;
            return this;
        }

        public Builder instructionGroups(List<BddInstructionGroup> instructionGroups) {
            this.instructionGroups = instructionGroups;
            return this;
        }

        public Builder modelId(String modelId) {
            this.modelId = modelId;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder triggers(List<AgentTrigger> triggers) {
            this.triggers = triggers;
            return this;
        }

        public AgentDefinition build() {
            return new AgentDefinition(this);
        }

    }

}
