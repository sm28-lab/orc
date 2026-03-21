package dev.sorn.orc.clients;

import dev.sorn.orc.api.JsonHttpClient;
import dev.sorn.orc.api.Result;
import dev.sorn.orc.errors.OrcException;
import dev.sorn.orc.json.Json;
import tools.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static dev.sorn.orc.json.Json.fromJson;
import static java.net.http.HttpClient.newHttpClient;

public final class DefaultJsonHttpClient implements JsonHttpClient {

    private final HttpClient javaHttpClient;

    public DefaultJsonHttpClient() {
        this(newHttpClient());
    }

    public DefaultJsonHttpClient(HttpClient javaHttpClient) {
        this.javaHttpClient = javaHttpClient;
    }

    @Override
    public Result<JsonNode> get(URI uri) {
        try {
            final var request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
            final var response = javaHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final var body = fromJson(response.body());
            return Result.Success.of(body);
        } catch (IOException | InterruptedException e) {
            return Result.Failure.of(new OrcException(e));
        }
    }

    @Override
    public Result<JsonNode> post(URI uri, JsonNode body) {
        try {
            final var request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(Json.toJson(body)))
                .header("Content-Type", "application/json")
                .build();
            final var response = javaHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final var responseBody = fromJson(response.body());
            return Result.Success.of(responseBody);
        } catch (IOException | InterruptedException e) {
            return Result.Failure.of(new OrcException(e));
        }
    }

}
