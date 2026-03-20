package dev.sorn.orc.tools

import spock.lang.Specification


class PrintWorkingDirectoryToolSpec extends Specification {

    def "returns current working directory"() {
        given:
        def tool = new PrintWorkingDirectoryTool()

        when:
        def result = tool.execute()

        then:
        result.isOk()
        with(result.get()) { r ->
            r.toString() == System.getProperty("user.dir")
        }

        and:
        !result.isEmpty()
        !result.isError()
    }

}
