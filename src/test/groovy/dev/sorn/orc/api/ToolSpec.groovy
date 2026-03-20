package dev.sorn.orc.api

import dev.sorn.orc.types.Result
import spock.lang.Specification

import static dev.sorn.orc.types.Result.ok

class ToolSpec extends Specification {

    def "calls no-arg execute on Void Tool"() {
        given:
        def tool = new VoidTool()

        when:
        def result = tool.execute()

        then:
        result.isOk()
        result.get() == "ok"
    }

    def "throws calling no-arg execute on a non-Void Tool"() {
        given:
        def tool = new NonVoidTool()

        when:
        tool.execute()

        then:
        def ex = thrown(UnsupportedOperationException)
        ex.message == "No-arg execute() is only supported for Tool<Void, O>"
    }

    static class VoidTool implements Tool<Void, String> {
        @Override
        Result<String> execute(Void input) {
            return ok("ok")
        }

        @Override
        Class<Void> inputType() {
            return Void.class
        }
    }

    static class NonVoidTool implements Tool<String, String> {
        @Override
        Result<String> execute(String input) {
            return ok(input)
        }

        @Override
        Class<String> inputType() {
            return String.class
        }
    }

}
