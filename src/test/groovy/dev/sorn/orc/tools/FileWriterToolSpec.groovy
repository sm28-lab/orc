package dev.sorn.orc.tools

import dev.sorn.orc.OrcSpecification
import dev.sorn.orc.errors.OrcException
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

import static dev.sorn.orc.json.Json.jsonObjectNode
import static dev.sorn.orc.tools.FileWriterTool.FILE_WRITER_TOOL_ID
import static java.nio.file.Files.readString
import static java.nio.file.Files.writeString

class FileWriterToolSpec extends OrcSpecification {
    @TempDir
    Path tempDir

    def "creates new file when file does not exist"() {
        given:
        def tool = toolRegistry.get(FILE_WRITER_TOOL_ID)
        def file = tempDir.resolve(UUID.randomUUID().toString() + ".txt")
        def content = "Hello, World!"
        Files.deleteIfExists(file)
        assert !Files.exists(file) : "File should not exist before test"

        when:
        def result = tool.execute(new FileWriterTool.Input(file, content))

        then:
        result.fold(
            { msg ->
                assert msg != null
                assert msg.contains("File written to: ${file.toAbsolutePath()}")
                assert msg.contains("${content.length()} bytes")
                assert !msg.contains("appended")
                assert readString(file) == content
                return true
            },
            { err -> assert false : "Should not fail" }
        )
    }

    def "appends to existing file"() {
        given:
        def tool = toolRegistry.get(FILE_WRITER_TOOL_ID)
        def file = tempDir.resolve("existing.txt")
        def original = "First line\n"
        def append = "Second line"
        writeString(file, original)
        assert Files.exists(file) : "File should exist before append"

        when:
        def result = tool.execute(new FileWriterTool.Input(file, append))

        then:
        result.fold(
            { msg ->
                assert msg != null
                assert msg.contains("File written to: ${file.toAbsolutePath()}")
                assert msg.contains("${append.length()} bytes")
                assert msg.contains("appended")
                assert readString(file) == original + append
                return true
            },
            { err -> assert false : "Should not fail" }
        )
    }

    def "creates parent directories automatically"() {
        given:
        def tool = toolRegistry.get(FILE_WRITER_TOOL_ID)
        def deepDir = tempDir.resolve("a/b/c")
        def file = deepDir.resolve("deep.txt")
        def content = "Deep content"

        when:
        def result = tool.execute(new FileWriterTool.Input(file, content))

        then:
        result.fold(
            { msg ->
                assert msg != null
                assert msg.contains("File written to: ${file.toAbsolutePath()}")
                assert Files.exists(deepDir)
                assert readString(file) == content
                return true
            },
            { err -> assert false : "Should not fail" }
        )
    }

    def "handles multiple appends correctly"() {
        given:
        def tool = toolRegistry.get(FILE_WRITER_TOOL_ID)
        def file = tempDir.resolve("multiple.txt")
        def lines = ["Line1", "Line2", "Line3"]

        when:
        lines.each { line ->
            tool.execute(new FileWriterTool.Input(file, line + "\n"))
        }

        then:
        def actual = readString(file)
        assert actual == "Line1\nLine2\nLine3\n"
    }

    def "handles empty content gracefully"() {
        given:
        def tool = toolRegistry.get(FILE_WRITER_TOOL_ID)
        def file = tempDir.resolve("empty.txt")
        Files.deleteIfExists(file)
        assert !Files.exists(file)

        when:
        def result = tool.execute(new FileWriterTool.Input(file, ""))

        then:
        result.fold(
            { msg ->
                assert msg != null
                assert msg.contains("0 bytes")
                assert Files.exists(file)
                assert readString(file) == ""
                assert !msg.contains("appended")
                return true
            },
            { err -> assert false : "Should not fail" }
        )
    }

    def "parseArguments extracts path and content correctly"() {
        given:
        def tool = toolRegistry.get(FILE_WRITER_TOOL_ID)
        def node = jsonObjectNode()
            .put("path", "/some/path.txt")
            .put("content", "some content")

        when:
        def input = tool.parseArguments(node)

        then:
        input.path() == Path.of("/some/path.txt")
        input.content() == "some content"
    }

    def "parseArguments throws when path missing"() {
        given:
        def tool = toolRegistry.get(FILE_WRITER_TOOL_ID)
        def node = jsonObjectNode().put("content", "content")

        when:
        tool.parseArguments(node)

        then:
        def ex = thrown(OrcException)
        ex.message == "'path' is required"
    }

    def "parseArguments throws when content missing"() {
        given:
        def tool = toolRegistry.get(FILE_WRITER_TOOL_ID)
        def node = jsonObjectNode().put("path", "/path")

        when:
        tool.parseArguments(node)

        then:
        def ex = thrown(OrcException)
        ex.message == "'content' is required"
    }

    def "inputDescription is informative"() {
        given:
        def tool = toolRegistry.get(FILE_WRITER_TOOL_ID)

        expect:
        tool.inputDescription().contains("Writes content")
    }

}
