package com.marta.flowstate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marta.flowstate.exception.ActionFailedException;
import com.marta.flowstate.util.TemplateRenderer;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba la acción HTTP contra un servidor HTTP local (sin salir a internet).
 */
class HttpActionHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TemplateRenderer renderer = new TemplateRenderer(objectMapper);
    private final HttpActionHandler handler = new HttpActionHandler(new HttpTargetValidator(true), renderer, objectMapper);

    private HttpServer server;
    private String baseUrl;
    private final AtomicReference<String> lastRequest = new AtomicReference<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/ok", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            lastRequest.set(exchange.getRequestMethod() + " " + exchange.getRequestURI() + " "
                    + exchange.getRequestHeaders().getFirst("X-Token") + " "
                    + exchange.getRequestHeaders().getFirst("Content-Type") + " " + body);
            respond(exchange, 200, "{\"ok\":true,\"score\":87}");
        });
        server.createContext("/error", exchange -> respond(exchange, 503, "caído"));
        server.createContext("/text", exchange -> respond(exchange, 200, "hola"));
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            respond(exchange, 200, "{}");
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private static void respond(com.sun.net.httpserver.HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    @Test
    void postRendersTemplatesAndReturnsParsedJson() {
        Map<String, Object> config = new HashMap<>();
        config.put("method", "post");
        config.put("url", baseUrl + "/ok?cliente={{cliente.id}}");
        config.put("headers", Map.of("X-Token", "{{token}}"));
        config.put("body", Map.of("importe", "{{importe}}"));
        config.put("saveAs", "validacion");
        Map<String, Object> data = Map.of("cliente", Map.of("id", 7), "token", "abc", "importe", 120);

        ActionResult result = handler.execute(config, data);

        assertEquals("POST /ok?cliente=7 abc application/json {\"importe\":\"120\"}", lastRequest.get());
        assertEquals(200, result.httpStatus());
        assertEquals("validacion", result.saveAs());
        assertEquals(Map.of("ok", true, "score", 87), result.output().get("body"));
        assertEquals("POST " + baseUrl + "/ok?cliente=7", result.requestSummary());
    }

    @Test
    void nonJsonResponseIsKeptAsText() {
        ActionResult result = handler.execute(Map.of("url", baseUrl + "/text"), Map.of());

        assertEquals("hola", result.output().get("body"));
    }

    @Test
    void non2xxResponseFailsWithStatus() {
        ActionFailedException ex = assertThrows(ActionFailedException.class,
                () -> handler.execute(Map.of("url", baseUrl + "/error"), Map.of()));

        assertEquals(503, ex.getHttpStatus());
        assertEquals("GET " + baseUrl + "/error", ex.getRequestSummary());
    }

    @Test
    void slowResponseFailsAfterTimeout() {
        long start = System.currentTimeMillis();
        ActionFailedException ex = assertThrows(ActionFailedException.class,
                () -> handler.execute(Map.of("url", baseUrl + "/slow", "timeoutMs", 300), Map.of()));

        assertTrue(System.currentTimeMillis() - start < 1900, "debe cortar antes de que responda el servidor");
        assertTrue(ex.getMessage().startsWith("Error llamando a la API"));
    }

    @Test
    void internalAddressIsBlockedByDefault() {
        HttpActionHandler strict = new HttpActionHandler(new HttpTargetValidator(false), renderer, objectMapper);

        ActionFailedException ex = assertThrows(ActionFailedException.class,
                () -> strict.execute(Map.of("url", baseUrl + "/ok"), Map.of()));

        assertTrue(ex.getMessage().contains("internas"));
        assertNull(lastRequest.get());
    }

    @Test
    void internalAddressInTemplateIsBlockedAtExecution() {
        HttpActionHandler strict = new HttpActionHandler(new HttpTargetValidator(false), renderer, objectMapper);
        Map<String, Object> config = Map.of("url", "http://{{host}}/ok");
        strict.validateConfig(config);

        assertThrows(ActionFailedException.class, () -> strict.execute(config, Map.of("host", "localhost:1")));
    }

    @Test
    void validateConfigRejectsInvalidValues() {
        HttpActionHandler strict = new HttpActionHandler(new HttpTargetValidator(false), renderer, objectMapper);

        assertThrows(IllegalArgumentException.class, () -> strict.validateConfig(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> strict.validateConfig(Map.of("url", "ftp://example.com")));
        assertThrows(IllegalArgumentException.class, () -> strict.validateConfig(Map.of("url", "http://10.0.0.5/x")));
        assertThrows(IllegalArgumentException.class, () -> strict.validateConfig(Map.of("url", "http://{{h}}/x", "method", "TRACE")));
        assertThrows(IllegalArgumentException.class, () -> strict.validateConfig(Map.of("url", "http://{{h}}/x", "timeoutMs", 60000)));
        assertThrows(IllegalArgumentException.class, () -> strict.validateConfig(Map.of("url", "http://{{h}}/x", "saveAs", "a.b")));
        assertThrows(IllegalArgumentException.class, () -> strict.validateConfig(Map.of("url", "http://{{h}}/x", "headers", Map.of("X", 1))));
    }
}
