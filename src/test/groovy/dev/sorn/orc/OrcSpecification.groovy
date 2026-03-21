package dev.sorn.orc

import dev.sorn.orc.api.ReaderFactory
import dev.sorn.orc.api.ToolRegistry
import dev.sorn.orc.module.AppToolRegistry
import dev.sorn.orc.tools.*
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Path

import static java.nio.file.Files.newBufferedReader

class OrcSpecification extends Specification {

    @Shared
    ToolRegistry toolRegistry = new AppToolRegistry()

    @Shared
    ReaderFactory readerFactory = Mock(ReaderFactory) {
        create(_ as Path) >> { Path file -> newBufferedReader(file) }
    }

    def setupSpec() {
        toolRegistry.register(new ListDirectoryContentsTool())
        toolRegistry.register(new PrintWorkingDirectoryTool())
        toolRegistry.register(new FileReaderTool(readerFactory))
        toolRegistry.register(new FileWriterTool())
        toolRegistry.register(new GrepTool())
    }

}
