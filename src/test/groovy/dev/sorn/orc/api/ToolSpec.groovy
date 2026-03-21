package dev.sorn.orc.api

import dev.sorn.orc.OrcSpecification
import dev.sorn.orc.types.Id

class ToolSpec extends OrcSpecification {

    def "executes tool"() {
        given:
        def input = "abc"
        def tool = new SomeTool()

        when:
        def result = tool.execute(input)

        then:
        result.fold(value -> value == "abc", { false }, { false })
    }

    static class SomeTool implements Tool<String, String> {
        @Override
        Id id() {
            return Id.of("some_tool")
        }

        @Override
        Result execute(String input) {
            return Result.Success.of(input)
        }
    }

}
