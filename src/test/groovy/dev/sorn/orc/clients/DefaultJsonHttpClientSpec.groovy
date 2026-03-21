package dev.sorn.orc.clients

import dev.sorn.orc.OrcSpecification
import dev.sorn.orc.api.Result
import tools.jackson.databind.JsonNode

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

import static dev.sorn.orc.json.Json.jsonObjectNode
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class DefaultJsonHttpClientSpec extends OrcSpecification {

    def "GET returns parsed JSON node"() {
        given:
        def uri = URI.create("https://example.com")
        def clientMock = mock(HttpClient)
        def responseMock = mock(HttpResponse)
        when(responseMock.body())
            .thenReturn('{"key":"value"}')
        when(clientMock.send(any(HttpRequest), any(HttpResponse.BodyHandler)))
            .thenReturn(responseMock)

        def httpClient = new DefaultJsonHttpClient(clientMock)

        when:
        def result = httpClient.get(uri)

        then:
        result instanceof Result.Success
        result.fold({ JsonNode node -> node.get("key").asText() == "value" }, { false }, { false })
    }

    def "POST returns parsed JSON node"() {
        given:
        def uri = URI.create("https://example.com")
        def body = jsonObjectNode().put("foo", "bar")
        def clientMock = mock(HttpClient)
        def responseMock = mock(HttpResponse)
        when(responseMock.body())
            .thenReturn('{"response":"ok"}')
        when(clientMock.send(any(HttpRequest), any(HttpResponse.BodyHandler)))
            .thenReturn(responseMock)

        def httpClient = new DefaultJsonHttpClient(clientMock)

        when:
        def result = httpClient.post(uri, body)

        then:
        result instanceof Result.Success
        result.fold({ JsonNode node -> node.get("response").asText() == "ok" }, { false }, { false })
    }

}
