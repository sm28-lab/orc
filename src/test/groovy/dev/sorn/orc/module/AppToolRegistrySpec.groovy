package dev.sorn.orc.module

import dev.sorn.orc.OrcSpecification
import dev.sorn.orc.errors.Error
import dev.sorn.orc.tools.ListDirectoryContentsTool
import dev.sorn.orc.tools.PrintWorkingDirectoryTool
import dev.sorn.orc.types.Id

class AppToolRegistrySpec extends OrcSpecification {

    def "registers and retrieves tools"() {
        given:
        def registry = new AppToolRegistry()
        def pwdTool = new PrintWorkingDirectoryTool()
        def listTool = new ListDirectoryContentsTool()

        when:
        registry.register(pwdTool)
        registry.register(listTool)

        then:
        registry.get(Id.of("print_working_directory_tool")) == pwdTool
        registry.get(Id.of("list_directory_contents_tool")) == listTool
    }

    def "retrieved tool executes correctly"() {
        given:
        def registry = new AppToolRegistry()
        def pwdTool = new PrintWorkingDirectoryTool()
        registry.register(pwdTool)

        when:
        def result = registry.get(Id.of("print_working_directory_tool")).execute()

        then:
        result.isOk()
        result.get().toString() == System.getProperty("user.dir")
    }

    def "throws error when retrieving unregistered tool"() {
        given:
        def registry = new AppToolRegistry()

        when:
        registry.get(Id.of("non_existent_tool"))

        then:
        def ex = thrown(Error)
        ex.message == "'non_existent_tool' tool is not registered"
    }

}