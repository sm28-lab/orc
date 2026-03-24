package dev.sorn.orc.lua;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import static dev.sorn.orc.json.Json.jsonBooleanNode;
import static dev.sorn.orc.json.Json.jsonArrayNode;
import static dev.sorn.orc.json.Json.jsonObjectNode;
import static dev.sorn.orc.json.Json.jsonNullNode;
import static dev.sorn.orc.json.Json.jsonNumberNode;
import static dev.sorn.orc.json.Json.jsonTextNode;

public final class LuaJsonConverter {

    private LuaJsonConverter() {
        // non-instantiable
    }

    public static LuaValue toLua(JsonNode node) {
        switch (node.getNodeType()) {
            case OBJECT: {
                final var table = new LuaTable();
                final var obj = (ObjectNode) node;
                obj
                    .propertyStream()
                    .forEach(e -> table.set(e.getKey(), toLua(e.getValue())));
                return table;
            }
            case ARRAY: {
                final var table = new LuaTable();
                final var arr = (ArrayNode) node;
                for (var i = 0; i < arr.size(); i++) {
                    table.set(luaIndex(i), toLua(arr.get(i)));
                }
                return table;
            }
            case STRING: return LuaValue.valueOf(node.asText());
            case NUMBER: return LuaValue.valueOf(node.asDouble());
            case BOOLEAN: return LuaValue.valueOf(node.asBoolean());
            default: return LuaValue.NIL;
        }
    }

    public static JsonNode fromLua(LuaValue value) {
        switch (value.typename()) {
            case "table": {
                final var tbl = (LuaTable) value;
                var isArray = true;
                for (final var key : tbl.keys()) {
                    if (!key.isint() || key.toint() <= 0) {
                        isArray = false;
                        break;
                    }
                }
                if (isArray) {
                    final var arr = jsonArrayNode();
                    for (var i = 1; i <= tbl.length(); i++) {
                        arr.add(fromLua(tbl.get(i)));
                    }
                    return arr;
                } else {
                    final var obj = jsonObjectNode();
                    for (final var key : tbl.keys()) {
                        obj.set(key.tojstring(), fromLua(tbl.get(key)));
                    }
                    return obj;
                }
            }
            case "boolean": return jsonBooleanNode(value.toboolean());
            case "number": return jsonNumberNode(value.todouble());
            case "string": return jsonTextNode(value.tojstring());
            default: return jsonNullNode();
        }
    }

    private static int luaIndex(int javaIndex) {
        return javaIndex + 1;
    }

}