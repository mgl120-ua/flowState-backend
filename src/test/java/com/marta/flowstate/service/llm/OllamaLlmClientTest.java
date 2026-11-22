package com.marta.flowstate.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marta.flowstate.exception.ActionFailedException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba el cliente de Ollama contra un servidor HTTP local que imita /api/chat.
 */
class OllamaLlmClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpServer server;
    private final AtomicReference<String> lastBody = new AtomicReference<>();
    private final AtomicReference<Integer> status = new AtomicReference<>(200);
    private final AtomicReference<String> reply = new AtomicReference<>();
    private final AtomicReference<Long> delayMs = new AtomicReference<>(0L);

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/chat", exchange -> {
            lastBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            try {
                Thread.sleep(delayMs.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            respond(exchange, status.get(), reply.get());
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private OllamaLlmClient client() {
        return new OllamaLlmClient("http://127.0.0.1:" + server.getAddress().getPort() + "/", objectMapper);
    }

    private static LlmRequest request(Map<String, Object> schema, Duration timeout) {
        return new LlmRequest("qwen2.5:3b", "Clasifica", "La web está caída", schema, 256, timeout, Map.of());
    }

    @Test
    void sendsChatRequestWithSchemaAndParsesUsage() throws IOException {
        reply.set("""
                {"model":"qwen2.5:3b","message":{"role":"assistant","content":"{\\"categoria\\":\\"urgente\\"}"},
                 "done":true,"done_reason":"stop","prompt_eval_count":42,"eval_count":9}
                """);
        Map<String, Object> schema = Map.of("type", "object");

        LlmResponse response = client().complete(request(schema, Duration.ofSeconds(5)));

        assertEquals("qwen2.5:3b", response.model());
        assertEquals("{\"categoria\":\"urgente\"}", response.text());
        assertEquals(42, response.inputTokens());
        assertEquals(9, response.outputTokens());
        assertEquals(LlmResponse.STOP, response.stopReason());

        JsonNode sent = objectMapper.readTree(lastBody.get());
        assertEquals("qwen2.5:3b", sent.get("model").asText());
        assertFalse(sent.get("stream").asBoolean());
        assertTrue(sent.has("think"));
        assertFalse(sent.get("think").asBoolean());
        assertEquals("object", sent.get("format").get("type").asText());
        assertEquals(256, sent.get("options").get("num_predict").asInt());
        assertEquals(0, sent.get("options").get("temperature").asInt());
        List<String> roles = List.of(sent.get("messages").get(0).get("role").asText(), sent.get("messages").get(1).get("role").asText());
        assertEquals(List.of("system", "user"), roles);
        assertEquals("La web está caída", sent.get("messages").get(1).get("content").asText());
    }

    @Test
    void withoutSchemaSendsNoFormatAndReportsLength() throws IOException {
        reply.set("{\"model\":\"qwen2.5:3b\",\"message\":{\"content\":\"texto\"},\"done_reason\":\"length\"}");

        LlmResponse response = client().complete(request(null, Duration.ofSeconds(5)));

        assertEquals(LlmResponse.LENGTH, response.stopReason());
        assertNull(response.inputTokens());
        assertFalse(objectMapper.readTree(lastBody.get()).has("format"));
    }

    @Test
    void missingModelExplainsHowToDownloadIt() {
        status.set(404);
        reply.set("{\"error\":\"model \\\"qwen2.5:3b\\\" not found, try pulling it first\"}");

        ActionFailedException ex = assertThrows(ActionFailedException.class,
                () -> client().complete(request(null, Duration.ofSeconds(5))));

        assertTrue(ex.getMessage().contains("ollama pull qwen2.5:3b"), ex.getMessage());
    }

    @Test
    void serverErrorIncludesOllamaMessage() {
        status.set(500);
        reply.set("{\"error\":\"out of memory\"}");

        ActionFailedException ex = assertThrows(ActionFailedException.class,
                () -> client().complete(request(null, Duration.ofSeconds(5))));

        assertEquals("Ollama respondió 500: out of memory", ex.getMessage());
    }

    @Test
    void timeoutIsReported() {
        delayMs.set(1500L);
        reply.set("{}");

        ActionFailedException ex = assertThrows(ActionFailedException.class,
                () -> client().complete(request(null, Duration.ofMillis(300))));

        assertTrue(ex.getMessage().startsWith("Ollama no respondió"), ex.getMessage());
    }

    @Test
    void unreachableOllamaIsReported() throws IOException {
        int freePort;
        try (ServerSocket socket = new ServerSocket(0)) {
            freePort = socket.getLocalPort();
        }
        OllamaLlmClient client = new OllamaLlmClient("http://127.0.0.1:" + freePort, objectMapper);

        ActionFailedException ex = assertThrows(ActionFailedException.class,
                () -> client.complete(request(null, Duration.ofSeconds(2))));

        assertTrue(ex.getMessage().startsWith("No se puede conectar con Ollama"), ex.getMessage());
    }
}
