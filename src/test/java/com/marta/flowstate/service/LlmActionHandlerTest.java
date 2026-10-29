package com.marta.flowstate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marta.flowstate.exception.ActionFailedException;
import com.marta.flowstate.service.llm.LlmClient;
import com.marta.flowstate.service.llm.LlmProperties;
import com.marta.flowstate.service.llm.LlmRequest;
import com.marta.flowstate.service.llm.LlmResponse;
import com.marta.flowstate.util.TemplateRenderer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class LlmActionHandlerTest {

    private static final Map<String, Object> SCHEMA = Map.of(
            "type", "object",
            "properties", Map.of(
                    "categoria", Map.of("type", "string", "enum", List.of("urgente", "normal")),
                    "confianza", Map.of("type", "number")),
            "required", List.of("categoria", "confianza"));

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicReference<LlmRequest> lastRequest = new AtomicReference<>();
    private final AtomicReference<LlmResponse> nextResponse = new AtomicReference<>();
    private final LlmClient client = request -> {
        lastRequest.set(request);
        return nextResponse.get();
    };
    private final LlmProperties properties = new LlmProperties("fake", "modelo-local", null,
            Map.of("modelo-pago", new LlmProperties.Price(new BigDecimal("2"), new BigDecimal("10"))));
    private final LlmActionHandler handler = new LlmActionHandler(client, properties, new TemplateRenderer(objectMapper), objectMapper);

    private static Map<String, Object> config() {
        Map<String, Object> config = new HashMap<>();
        config.put("system", "Clasifica incidencias de {{empresa}}");
        config.put("prompt", "Asunto: {{asunto}}");
        config.put("outputSchema", SCHEMA);
        config.put("saveAs", "triaje");
        return config;
    }

    @Test
    void rendersPromptValidatesJsonAndReturnsUsage() {
        nextResponse.set(new LlmResponse("modelo-local", "{\"categoria\":\"urgente\",\"confianza\":0.92}", 30, 12, "stop"));

        ActionResult result = handler.execute(config(), Map.of("asunto", "Web caída", "empresa", "ACME"));

        assertEquals("modelo-local", lastRequest.get().model());
        assertEquals("Asunto: Web caída", lastRequest.get().prompt());
        assertEquals("Clasifica incidencias de ACME", lastRequest.get().system());
        assertEquals(LlmActionHandler.DEFAULT_MAX_TOKENS, lastRequest.get().maxTokens());
        assertEquals("triaje", result.saveAs());
        assertEquals("ok", result.output().get("status"));
        assertEquals(Map.of("categoria", "urgente", "confianza", 0.92), result.output().get("body"));
        assertEquals("LLM modelo-local", result.requestSummary());
        assertEquals(30, result.llmUsage().inputTokens());
        assertEquals(12, result.llmUsage().outputTokens());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.llmUsage().costUsd()));
        assertEquals("Asunto: Web caída", result.llmUsage().prompt());
    }

    @Test
    void withoutSchemaKeepsText() {
        Map<String, Object> config = config();
        config.remove("outputSchema");
        nextResponse.set(new LlmResponse("modelo-local", "Resumen libre", 5, 3, "stop"));

        ActionResult result = handler.execute(config, Map.of());

        assertEquals("Resumen libre", result.output().get("body"));
    }

    @Test
    void outputThatBreaksSchemaFailsKeepingTokens() {
        nextResponse.set(new LlmResponse("modelo-local", "{\"categoria\":\"otra\"}", 30, 6, "stop"));

        ActionFailedException ex = assertThrows(ActionFailedException.class, () -> handler.execute(config(), Map.of()));

        assertTrue(ex.getMessage().startsWith("La respuesta del modelo no cumple el esquema"), ex.getMessage());
        assertEquals(30, ex.getLlmUsage().inputTokens());
        assertEquals(6, ex.getLlmUsage().outputTokens());
    }

    @Test
    void invalidJsonAndCutResponsesFail() {
        nextResponse.set(new LlmResponse("modelo-local", "no es json", 30, 3, "stop"));
        assertEquals("La respuesta del modelo no es un JSON válido",
                assertThrows(ActionFailedException.class, () -> handler.execute(config(), Map.of())).getMessage());

        nextResponse.set(new LlmResponse("modelo-local", "{\"categoria\":", 30, 1024, "length"));
        ActionFailedException cut = assertThrows(ActionFailedException.class, () -> handler.execute(config(), Map.of()));
        assertTrue(cut.getMessage().contains("maxTokens"));
        assertEquals("length", cut.getLlmUsage().stopReason());
    }

    @Test
    void providerFailureKeepsPromptInUsage() {
        LlmActionHandler failing = new LlmActionHandler(request -> {
            throw new ActionFailedException("No se puede conectar con Ollama");
        }, properties, new TemplateRenderer(objectMapper), objectMapper);

        ActionFailedException ex = assertThrows(ActionFailedException.class,
                () -> failing.execute(config(), Map.of("asunto", "x")));

        assertEquals("No se puede conectar con Ollama", ex.getMessage());
        assertEquals("LLM modelo-local", ex.getRequestSummary());
        assertEquals("Asunto: x", ex.getLlmUsage().prompt());
        assertNull(ex.getLlmUsage().inputTokens());
    }

    @Test
    void costUsesPriceTableAndTruncatesLongPrompt() {
        Map<String, Object> config = config();
        config.put("model", "modelo-pago");
        config.remove("outputSchema");
        nextResponse.set(new LlmResponse("modelo-pago", "ok", 1_000_000, 500_000, "stop"));

        ActionResult result = handler.execute(config, Map.of("asunto", "x".repeat(10_000)));

        assertEquals(new BigDecimal("7.000000"), result.llmUsage().costUsd());
        assertEquals(LlmActionHandler.MAX_TRACE_PROMPT_CHARS, result.llmUsage().prompt().length());
    }

    @Test
    void validateConfigRejectsBadFields() {
        assertDoesNotThrow(() -> handler.validateConfig(config()));
        assertInvalid(Map.of("prompt", " "));
        assertInvalid(Map.of("prompt", "p", "model", ""));
        assertInvalid(Map.of("prompt", "p", "outputSchema", "object"));
        assertInvalid(Map.of("prompt", "p", "maxTokens", 0));
        assertInvalid(Map.of("prompt", "p", "maxTokens", LlmActionHandler.MAX_MAX_TOKENS + 1));
        assertInvalid(Map.of("prompt", "p", "timeoutMs", LlmActionHandler.MAX_TIMEOUT_MS + 1));
        assertInvalid(Map.of("prompt", "p", "saveAs", "con espacio"));
    }

    private void assertInvalid(Map<String, Object> config) {
        assertThrows(IllegalArgumentException.class, () -> handler.validateConfig(config), config.toString());
    }
}
