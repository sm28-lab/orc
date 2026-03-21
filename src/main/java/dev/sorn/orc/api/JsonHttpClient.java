package dev.sorn.orc.api;

import tools.jackson.databind.JsonNode;
import java.net.URI;

public interface JsonHttpClient extends HttpClient<JsonNode, JsonNode> {

    Result<JsonNode> get(URI uri);

    Result<JsonNode> post(URI uri, JsonNode body);

}
