package dev.sorn.orc.module

import dev.sorn.orc.OrcSpecification
import dev.sorn.orc.types.AgentTrigger
import dev.sorn.orc.types.Id
import io.vavr.collection.List

import static dev.sorn.orc.types.AgentData.Type.*
import static dev.sorn.orc.types.AgentRole.WORKER

class AgentFactorySpec extends OrcSpecification {

    def "parses grouped instructions"() {
        given:
        def json = '''
        {
          "agents": [
            {
              "id": "grouped_agent",
              "role": "worker",
              "toolIds": [],
              "input": [],
              "output": [],
              "instructions": [
                {
                  "given": "Given text",
                  "when": "When text",
                  "then": "Then text"
                },
                {
                  "given": [],
                  "when": ["When one", "When two"],
                  "then": ["Then one", "Then two"]
                },
                {
                  "given": "Given only",
                  "when": [],
                  "then": []
                }
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
        def groups = agents[0].instructions()
        groups.size() == 3

        groups[0].given() == List.of("Given text")
        groups[0].when() == List.of("When text")
        groups[0].then() == List.of("Then text")

        groups[1].given().isEmpty()
        groups[1].when() == List.of("When one", "When two")
        groups[1].then() == List.of("Then one", "Then two")

        groups[2].given() == List.of("Given only")
        groups[2].when().isEmpty()
        groups[2].then().isEmpty()
    }

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
                {
                  "given": [],
                  "when": [],
                  "then": ["Check given code adheres to SOLID principles"]
                },
                {
                  "given": [],
                  "when": [],
                  "then": ["Check given code adheres to DRY"]
                }
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
        agent.inputs().size() == 1
        agent.inputs()[0].name == "code"
        agent.inputs()[0].type == STRING

        and:
        agent.outputs().size() == 2
        agent.outputs()[0].name == "review_comment"
        agent.outputs()[0].type == COLLECTION
        agent.outputs()[1].name == "review_approved"
        agent.outputs()[1].type == BOOLEAN

        and:
        agent.instructions().size() == 2
        agent.instructions()[0].given().isEmpty()
        agent.instructions()[0].when().isEmpty()
        agent.instructions()[0].then() == List.of("Check given code adheres to SOLID principles")

        agent.instructions()[1].given().isEmpty()
        agent.instructions()[1].when().isEmpty()
        agent.instructions()[1].then() == List.of("Check given code adheres to DRY")
    }

    def "parses grouped instructions with mixed fields and arrays"() {
        given:
        def json = '''
        {
          "agents": [
            {
              "id": "grouped_agent",
              "role": "worker",
              "toolIds": [],
              "input": [],
              "output": [],
              "instructions": [
                {
                  "given": "Given text",
                  "when": "When text",
                  "then": "Then text"
                },
                {
                  "given": [],
                  "when": "When only",
                  "then": ["Then one", "Then two"]
                },
                {
                  "given": "Given only",
                  "when": [],
                  "then": []
                }
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
        def groups = agents[0].instructions()
        groups.size() == 3

        groups[0].given() == List.of("Given text")
        groups[0].when() == List.of("When text")
        groups[0].then() == List.of("Then text")

        groups[1].given().isEmpty()
        groups[1].when() == List.of("When only")
        groups[1].then() == List.of("Then one", "Then two")

        groups[2].given() == List.of("Given only")
        groups[2].when().isEmpty()
        groups[2].then().isEmpty()
    }

    def "parses agent triggers"() {
        given:
        def json = '''
        {
          "agents": [
            {
              "id": "trigger_agent",
              "role": "worker",
              "toolIds": [],
              "input": [],
              "output": [],
              "instructions": [
                {
                  "given": [],
                  "when": [],
                  "then": []
                }
              ],
              "triggers": [
                {
                  "targetAgentId": "other_agent",
                  "condition": "on_output",
                  "outputField": "result"
                },
                {
                  "targetAgentId": "always_agent",
                  "condition": "always"
                }
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
        def triggers = agents[0].triggers()
        triggers.size() == 2

        triggers[0].targetAgentId() == Id.of("other_agent")
        triggers[0].condition() == AgentTrigger.TriggerCondition.ON_OUTPUT
        triggers[0].outputField() == "result"

        triggers[1].targetAgentId() == Id.of("always_agent")
        triggers[1].condition() == AgentTrigger.TriggerCondition.ALWAYS
        triggers[1].outputField() == null
    }

    def "parses agent data"() {
        given:
        def json = '''
        {
          "agents": [
            {
              "id": "data_agent",
              "role": "worker",
              "toolIds": [],
              "input": [
                { "type": "string", "name": "input1" },
                { "type": "collection", "name": "input2" },
                { "type": "boolean", "name": "input3" }
              ],
              "output": [
                { "type": "string", "name": "output1" }
              ],
              "instructions": [
                {
                  "given": [],
                  "when": [],
                  "then": []
                }
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
        def agent = agents[0]

        agent.inputs().size() == 3
        agent.inputs()[0].name == "input1"
        agent.inputs()[0].type == STRING
        agent.inputs()[1].name == "input2"
        agent.inputs()[1].type == COLLECTION
        agent.inputs()[2].name == "input3"
        agent.inputs()[2].type == BOOLEAN

        agent.outputs().size() == 1
        agent.outputs()[0].name == "output1"
        agent.outputs()[0].type == STRING
    }

    def "parses workflows from JSON"() {
        given:
        def json = '''
        {
          "agents": [],
          "workflows": [
            {
              "id": "test_workflow",
              "description": "A test workflow",
              "entryPoints": ["agent1", "agent2"]
            }
          ]
        }
        '''

        when:
        def factory = new AgentFactory()
        def workflows = factory.loadWorkflowsFromJson(json)

        then:
        workflows.size() == 1
        def wf = workflows[0]
        wf.id() == Id.of("test_workflow")
        wf.description() == "A test workflow"
        wf.entryPoints() == List.of(Id.of("agent1"), Id.of("agent2"))
    }

}
