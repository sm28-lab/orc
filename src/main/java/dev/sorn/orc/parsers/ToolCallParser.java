package dev.sorn.orc.parsers;

import dev.sorn.orc.errors.ToolCallException;
import dev.sorn.orc.types.Id;
import dev.sorn.orc.types.ToolCall;
import io.vavr.collection.List;
import tools.jackson.databind.JsonNode;
import java.util.regex.Pattern;
import static dev.sorn.orc.json.Json.fromJson;

public final class ToolCallParser {

    private static final Pattern TOOL_CALL_PATTERN = Pattern.compile(
        "<tool_call>\\s*(\\{.*?\\})\\s*</tool_call>",
        Pattern.DOTALL
    );

    private ToolCallParser() {}

    public static List<ToolCall> parse(String text) {
        var matcher = TOOL_CALL_PATTERN.matcher(text);
        var calls = List.<ToolCall>empty();
        while (matcher.find()) {
            var jsonStr = matcher.group(1).trim();
            JsonNode node;
            try {
                node = fromJson(jsonStr);
            } catch (Exception e) {
                throw new ToolCallException("Invalid JSON in tool call: %s", e.getMessage());
            }
            if (!node.has("tool") || !node.has("arguments")) {
                throw new ToolCallException("Tool call must contain 'tool' and 'arguments' fields");
            }
            var toolId = Id.of(node.get("tool").asText());
            var arguments = node.get("arguments");
            calls = calls.append(ToolCall.of(toolId, arguments));
        }
        return calls;
    }

}
