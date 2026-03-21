package dev.sorn.orc.parsers

import dev.sorn.orc.OrcSpecification
import dev.sorn.orc.errors.OrcException
import dev.sorn.orc.types.Id

class ToolCallParserSpec extends OrcSpecification {

    def "parses single tool call"() {
        given:
        def text = """
            Some text before.
            <tool_call>
            {
              "tool": "file_reader_tool",
              "arguments": {
                "path": "/tmp/test.txt",
                "lineNumberRange": { "from": 1, "to": 10 }
              }
            }
            </tool_call>
            Some text after.
        """

        when:
        def calls = ToolCallParser.parse(text)

        then:
        calls.size() == 1
        calls[0].toolId() == Id.of("file_reader_tool")
        calls[0].arguments().get("path").asText() == "/tmp/test.txt"
        calls[0].arguments().get("lineNumberRange").get("from").asInt() == 1
        calls[0].arguments().get("lineNumberRange").get("to").asInt() == 10
    }

    def "parses multiple tool calls"() {
        given:
        def text = """
            <tool_call>{"tool":"tool1","arguments":{}}</tool_call>
            <tool_call>{"tool":"tool2","arguments":{"x":1}}</tool_call>
        """

        when:
        def calls = ToolCallParser.parse(text)

        then:
        calls.size() == 2
        calls[0].toolId() == Id.of("tool1")
        calls[1].toolId() == Id.of("tool2")
    }

    def "throws on invalid JSON"() {
        given:
        def text = "<tool_call>{not json}</tool_call>"

        when:
        ToolCallParser.parse(text)

        then:
        def ex = thrown(OrcException)
        ex.message.contains("Invalid JSON in tool call")
    }

    def "throws on missing fields"() {
        given:
        def text = "<tool_call>{\"tool\":\"foo\"}</tool_call>"

        when:
        ToolCallParser.parse(text)

        then:
        def ex = thrown(OrcException)
        ex.message == "Tool call must contain 'tool' and 'arguments' fields"
    }

}
