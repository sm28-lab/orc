package dev.sorn.orc.module

import dev.sorn.orc.OrcSpecification
import dev.sorn.orc.types.Id
import io.vavr.collection.List

import static dev.sorn.orc.types.AgentData.Type.*
import static dev.sorn.orc.types.AgentRole.WORKER

class AgentFactorySpec extends OrcSpecification {

    def "parses agents from JSON definition"() {
        given:
        def json = '''
        {
          "agents": [
            {
              "id": "code_reviewer_agent",
              "role": "worker",
              "toolIds": [
                "file_reader_tool",
                "list_directory_contents_tool",
                "print_working_directory_tool"
              ],
              "input": [
                { "type": "string", "name": "code" }
              ],
              "output": [
                { "type": "collection", "name": "review_comment" },
                { "type": "boolean", "name": "review_approved" }
              ],
              "instructions": [
                "Check given code adheres to SOLID principles",
                "Check given code adheres to DRY"
              ]
            }
          ]
        }
        '''

        when:
        def factory = new AgentFactory()
        def agents = factory.loadFromJson(json)

        then:
        agents.size() == 1

        and:
        def agent = agents[0]
        agent.id() == Id.of("code_reviewer_agent")
        agent.role() == WORKER
        agent.toolIds()*.value() == ["file_reader_tool", "list_directory_contents_tool", "print_working_directory_tool"]

        and:
        agent.input().size() == 1
        agent.input()[0].name == "code"
        agent.input()[0].type == STRING

        and:
        agent.output().size() == 2
        agent.output()[0].name == "review_comment"
        agent.output()[0].type == COLLECTION

        agent.output()[1].name == "review_approved"
        agent.output()[1].type == BOOLEAN

        and:
        agent.instructions() == List.of(
            "Check given code adheres to SOLID principles",
            "Check given code adheres to DRY"
        )
    }

}
