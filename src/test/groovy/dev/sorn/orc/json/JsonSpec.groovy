package dev.sorn.orc.json

import dev.sorn.orc.OrcSpecification
import io.vavr.collection.List
import io.vavr.control.Option

import static dev.sorn.orc.json.Json.fromJson
import static dev.sorn.orc.json.Json.toJson

class JsonSpec extends OrcSpecification {

    def "serializes object "() {
        given:
        def dto = new TestRecord("Alice", 30, Option.of("Ally"), List.of("a", "b"))

        when:
        def json = toJson(dto)

        then:
        json.contains('"name":"Alice"')
        json.contains('"age":30')
        json.contains('"nickname":"Ally"')
        json.contains('"tags":["a","b"]')
    }

    def "deserializes object into node"() {
        given:
        def json = '{"name":"Carol","age":40,"nickname":null,"tags":["tag1"]}'

        when:
        def node = fromJson(json)

        then:
        node.get("name").asText() == "Carol"
        node.get("age").asInt() == 40
        node.get("nickname").isNull()
        node.get("tags").isArray()
        node.get("tags")[0].asText() == "tag1"
    }

    def "deserializes object into class"() {
        given:
        def json = '{"name":"Bob","age":25,"nickname":"Bobby","tags":["x","y"]}'

        when:
        def dto = fromJson(json, TestRecord)

        then:
        dto.name == "Bob"
        dto.age == 25
        dto.nickname.get() == "Bobby"
        dto.tags == List.of("x","y")
    }

    static record TestRecord(
        String name,
        int age,
        Option<String> nickname,
        List<String> tags
    ) {}

}
