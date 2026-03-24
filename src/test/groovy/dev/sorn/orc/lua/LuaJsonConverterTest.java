package dev.sorn.orc.lua;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.sorn.orc.json.Json.jsonBooleanNode;
import static dev.sorn.orc.json.Json.jsonArrayNode;
import static dev.sorn.orc.json.Json.jsonObjectNode;
import static dev.sorn.orc.json.Json.jsonNullNode;
import static dev.sorn.orc.json.Json.jsonNumberNode;
import static dev.sorn.orc.json.Json.jsonTextNode;
import static dev.sorn.orc.lua.LuaJsonConverter.fromLua;
import static dev.sorn.orc.lua.LuaJsonConverter.toLua;
import static org.assertj.core.api.Assertions.assertThat;

class LuaJsonConverterTest {

    @Nested
    class PrimitiveConversions {

        @Test
        void converts_string() {
            // GIVEN
            var node = jsonTextNode("hello");

            // WHEN
            var lua = toLua(node);
            var back = fromLua(lua);

            // THEN
            assertThat(lua.isstring()).isTrue();
            assertThat("hello").isEqualTo(lua.tojstring());
            assertThat(back.isTextual()).isTrue();
            assertThat("hello").isEqualTo(back.asText());
        }

        @Test
        void converts_number() {
            // GIVEN
            var node = jsonNumberNode(42.28);

            // WHEN
            var lua = toLua(node);
            var back = fromLua(lua);

            // THEN
            assertThat(lua.isnumber()).isTrue();
            assertThat(lua.todouble()).isEqualTo(42.28);
            assertThat(back.isNumber()).isTrue();
            assertThat(back.asDouble()).isEqualTo(42.28);
        }

        @Test
        void converts_boolean() {
            // GIVEN
            var node = jsonBooleanNode(true);

            // WHEN
            var lua = toLua(node);
            var back = fromLua(lua);

            // THEN
            assertThat(lua.isboolean()).isTrue();
            assertThat(lua.toboolean()).isTrue();
            assertThat(back.isBoolean()).isTrue();
            assertThat(back.asBoolean()).isTrue();
        }

        @Test
        void converts_null() {
            // GIVEN
            var node = jsonNullNode();

            // WHEN
            var lua = toLua(node);
            var back = fromLua(lua);

            // THEN
            assertThat(lua.isnil()).isTrue();
            assertThat(back.isNull()).isTrue();
        }

    }

    @Nested
    class ArrayConversions {

        @Test
        void converts_simple_array() {
            // GIVEN
            var array = jsonArrayNode();
            array.add(jsonNumberNode(1));
            array.add(jsonNumberNode(2));
            array.add(jsonNumberNode(3));

            // WHEN
            var lua = toLua(array);
            var back = fromLua(lua);

            // THEN
            assertThat(lua.istable()).isTrue();
            assertThat(lua.length()).isEqualTo(3);
            assertThat(lua.get(1).todouble()).isEqualTo(1);
            assertThat(lua.get(2).todouble()).isEqualTo(2);
            assertThat(lua.get(3).todouble()).isEqualTo(3);

            assertThat(back.isArray()).isTrue();
            assertThat(back.size()).isEqualTo(3);
            assertThat(back.get(0).asInt()).isEqualTo(1);
            assertThat(back.get(1).asInt()).isEqualTo(2);
            assertThat(back.get(2).asInt()).isEqualTo(3);
        }

        @Test
        void converts_nested_array_in_object() {
            // GIVEN
            var obj = jsonObjectNode();
            var arr = jsonArrayNode();
            arr.add(jsonNumberNode(1));
            arr.add(jsonNumberNode(2));
            obj.set("numbers", arr);

            // WHEN
            var lua = toLua(obj);
            var back = fromLua(lua);

            // THEN
            assertThat(lua.istable()).isTrue();
            assertThat(lua.get("numbers").length()).isEqualTo(2);
            assertThat(lua.get("numbers").get(1).todouble()).isEqualTo(1);
            assertThat(lua.get("numbers").get(2).todouble()).isEqualTo(2);

            assertThat(back.isObject()).isTrue();
            assertThat(back.get("numbers").size()).isEqualTo(2);
            assertThat(back.get("numbers").get(0).asInt()).isEqualTo(1);
            assertThat(back.get("numbers").get(1).asInt()).isEqualTo(2);
        }

    }

    @Nested
    class ObjectConversions {

        @Test
        void converts_simple_object() {
            // GIVEN
            var obj = jsonObjectNode();
            obj.set("a", jsonNumberNode(10));
            obj.set("b", jsonTextNode("foo"));
            obj.set("c", jsonBooleanNode(true));

            // WHEN
            var lua = toLua(obj);
            var back = fromLua(lua);

            // THEN
            assertThat(lua.istable()).isTrue();
            assertThat(lua.get("a").todouble()).isEqualTo(10);
            assertThat(lua.get("b").tojstring()).isEqualTo("foo");
            assertThat(lua.get("c").toboolean()).isTrue();

            assertThat(back.isObject()).isTrue();
            assertThat(back.get("a").asInt()).isEqualTo(10);
            assertThat(back.get("b").asText()).isEqualTo("foo");
            assertThat(back.get("c").asBoolean()).isTrue();
        }

        @Test
        void converts_nested_object_with_array() {
            // GIVEN
            var obj = jsonObjectNode();
            var arr = jsonArrayNode();
            arr.add(jsonNumberNode(1));
            arr.add(jsonNumberNode(2));
            obj.set("numbers", arr);
            obj.set("name", jsonTextNode("test"));

            // WHEN
            var lua = toLua(obj);
            var back = fromLua(lua);

            // THEN
            assertThat(lua.istable()).isTrue();
            assertThat(lua.get("numbers").length()).isEqualTo(2);
            assertThat(lua.get("name").tojstring()).isEqualTo("test");

            assertThat(back.isObject()).isTrue();
            assertThat(back.get("numbers").size()).isEqualTo(2);
            assertThat(back.get("numbers").get(0).asInt()).isEqualTo(1);
            assertThat(back.get("numbers").get(1).asInt()).isEqualTo(2);
            assertThat(back.get("name").asText()).isEqualTo("test");
        }

    }

}