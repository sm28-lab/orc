package dev.sorn.orc.json;

import io.vavr.jackson.datatype.VavrModule;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static tools.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;

public final class Json {

    private static final ObjectMapper mapper = JsonMapper.builder()
        .addModule(new VavrModule())
        .disable(FAIL_ON_EMPTY_BEANS)
        .build();

    private Json() {}

    public static String toJson(Object obj) {
        return mapper.writeValueAsString(obj);
    }

    public static JsonNode fromJson(String json) {
        return mapper.readTree(json);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return mapper.readValue(json, type);
    }

}