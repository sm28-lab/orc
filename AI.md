# AI Instructions

## Coding Standards

### Code Architecture

* Functional/declarative style programming.
* Use `io.vavr` library for functional programming constructs.
* Adhere to SOLID principles.
* Adhere to DRY.
* Follow TDD (always write specs/tests first).
* Never add comments to code.
* Minimal changes to achieve result.
* Small, focused classes, and interface segregation.

### Code Style

* Use `final var` in prod code.
* Use Spock specs and stubs for integration testing.

## Value Objects

* Package: `dev.sorn.orc.types`.
* Use records and enums for value objects.
* Apply validations in the constructor.
* Provide static `.of(...)` factory methods for construction.

### Example

```java
public record Id(String value) {

    public Id {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Id cannot be blank");
        }
        if (!value.matches("[a-zA-Z0-9:._-]+")) {
            throw new ValidationException("Id must match [a-zA-Z0-9:._-]+");
        }
    }

    public static Id of(String value) {
        return new Id(value);
    }

}
```

## Unit Testing

* Use BDDMockito for unit testing.
* Use `var`.
* Use `// GIVEN`, `// WHEN`, `// THEN` comment blocks.

### Example

```java
class SomeClientTest {

    private final HttpClient httpClientMock = mock(HttpClient.class);
    private final SomeExecutor executor = new SomeExecutor(httpClientMock);

    @Test
    void propagates_http_failure() {
        // GIVEN
        given(httpClientMock.post(any(), any()))
            .willReturn(Failure.of(new Exception("some http failure")));

        // WHEN
        var result = executor.execute("some instruction");

        // THEN
        assertThat(result).isInstanceOf(Failure.class);
    }

}
```

## Test Fixtures

* Use test fixtures for setting up complex test data.
* Place test fixtures in `src/testFixtures/java/dev/sorn/orc`.

### Example

```java
package dev.sorn.orc;

import dev.sorn.orc.agents.DefaultAgent;
import dev.sorn.orc.types.AgentDefinition;
import dev.sorn.orc.types.Id;
import io.vavr.collection.List;
import static dev.sorn.orc.agents.DefaultAgent.Builder.defaultAgent;
import static dev.sorn.orc.types.AgentData.Builder.agentData;
import static dev.sorn.orc.types.AgentData.Type.COLLECTION;
import static dev.sorn.orc.types.AgentData.Type.STRING;
import static dev.sorn.orc.types.AgentDefinition.Builder.agentDefinition;
import static dev.sorn.orc.types.AgentRole.WORKER;
import static dev.sorn.orc.types.BddInstruction.given;
import static dev.sorn.orc.types.BddInstruction.then;
import static dev.sorn.orc.types.BddInstruction.when;

public interface DefaultAgentTestData {

    default DefaultAgent.Builder aDefaultAgent() {
        return defaultAgent()
            .agentDefinition(anAgentDefinition().build())
            .toolRegistry(new StubToolRegistry())
            .llmClient(new StubLlmClient());
    }

    default AgentDefinition.Builder anAgentDefinition() {
        return agentDefinition()
            .id(Id.of("some_agent"))
            .role(WORKER)
            .toolIds(List.of(
                Id.of("file_reader_tool"),
                Id.of("list_directory_contents_tool"),
                Id.of("print_working_directory_tool")))
            .inputs(List.of(agentData().name("code").type(STRING).build()))
            .outputs(List.of(agentData().name("suggestions").type(COLLECTION).build()))
            .instructions(List.of(
                given("some instruction 1"),
                when("some instruction 2"),
                then("some instruction 3")))
            .modelId("test-model")
            .baseUrl("http://test")
            .maxTokens(100);
    }

}
```

## Builder Pattern

Use the builder pattern for constructing complex objects.

### Example

```java
public record SomeClass(
    Id id,
    Option<String> description
) {

    private SomeClass(Builder builder) {
        this(requireNonNull(builder.id), builder.description);
    }

    public static final class Builder {

        private Id id;
        private Option<String> description = none();

        public static Builder someClass() {
            return new Builder();
        }

        public Builder id(Id id) {
            this.id = id;
            return this;
        }

        public Builder description(String description) {
            this.description = some(description);
            return this;
        }

        public SomeClass build() {
            return new SomeClass(this);
        }

    }

}
```

## Output

* Output the file path.
* Output if file is new or modified in parentheses.
* Output the full file content for easy copy-paste.

### Example

1. `src/main/java/com/example/ExampleClass.java` (new file)

```java
// Path: /src/main/java/com/example/ExampleClass.java
package com.example;

public class ExampleClass {

    private final String exampleField;

    public void exampleMethod() {
        // method implementation
    }

}
```

