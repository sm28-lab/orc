package dev.sorn.orc.tools

import dev.sorn.orc.OrcSpecification
import dev.sorn.orc.errors.ToolError
import dev.sorn.orc.types.LineNumberRange
import spock.lang.TempDir

import java.nio.file.Path

import static dev.sorn.orc.tools.FileReaderTool.FILE_READER_TOOL_ID
import static java.nio.file.Files.writeString

class FileReaderToolSpec extends OrcSpecification {

    @TempDir
    Path tempDir

    def "reads entire file if no line range provided"() {
        given:
        def file = tempDir.resolve("file.txt")
        def contents = "this\nis\nsome\ntext\nin\na\nfile"
        writeString(file, contents)

        and:
        def tool = toolRegistry.get(FILE_READER_TOOL_ID)

        when:
        def result = tool.execute(new FileReaderTool.Input(file, LineNumberRange.empty()))

        then:
        result.isOk()
        result.get() == contents
    }

    def "reads only specified line range"() {
        given:
        def file = tempDir.resolve("file.txt")
        def contents = "this\nis\nsome\ntext\nin\na\nfile"
        writeString(file, contents)

        and:
        def tool = toolRegistry.get(FILE_READER_TOOL_ID)
        def range = LineNumberRange.of(from, to)

        when:
        def result = tool.execute(new FileReaderTool.Input(file, range))

        then:
        result.isOk()
        result.get() == expected

        where:
        from | to | expected
        1    | 2  | "this"
        3    | 4  | "some"
        3    | 5  | "some\ntext"
        3    | 6  | "some\ntext\nin"
        1    | 7  | "this\nis\nsome\ntext\nin\na"
        1    | 8  | "this\nis\nsome\ntext\nin\na\nfile"
    }

    def "returns error on file not found"() {
        given:
        def missing = tempDir.resolve("missing.txt")
        def tool = toolRegistry.get(FILE_READER_TOOL_ID)

        when:
        def result = tool.execute(new FileReaderTool.Input(missing, LineNumberRange.empty()))

        then:
        result.isError()
        result.getError() instanceof ToolError
    }

}
