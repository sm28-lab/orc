package dev.sorn.orc.parsers;

import dev.sorn.orc.errors.ToolCallException;
import dev.sorn.orc.types.Id;
import dev.sorn.orc.types.ToolCall;
import io.vavr.collection.List;
import java.util.regex.Pattern;
import static dev.sorn.orc.json.Json.fromJson;

public final class ToolCallParser {

    private static final Pattern TOOL_CALL_PATTERN = Pattern.compile(
        "<tool_call>\\s*(\\{.*?\\})\\s*</tool_call>",
        Pattern.DOTALL
    );

    private ToolCallParser() {}

    public static List<ToolCall> parse(String text) {
        final var matcher = TOOL_CALL_PATTERN.matcher(text);
        var calls = List.<ToolCall>empty();
        while (matcher.find()) {
            final var jsonStr = matcher.group(1).trim();
            final var node = fromJson(jsonStr);
            if (!node.has("tool") || !node.has("arguments")) {
                throw new ToolCallException("Tool call must contain 'tool' and 'arguments' fields");
            }
            final var toolId = Id.of(node.get("tool").asText());
            final var arguments = node.get("arguments");
            calls = calls.append(ToolCall.of(toolId, arguments));
        }
        return calls;
    }

}
