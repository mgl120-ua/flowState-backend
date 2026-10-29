package com.marta.flowstate.service.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marta.flowstate.exception.ActionFailedException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Proveedor sin red para tests y CI. Responde según la configuración de la acción:
 * "fakeRules": [{"contains": "texto", "response": ...}] se compara con el prompt (gana la primera),
 * y si ninguna coincide se usa "fakeResponse". Los tokens se estiman en 4 caracteres por token.
 */
public class FakeLlmClient implements LlmClient {

    private final ObjectMapper objectMapper;
    private final AtomicInteger calls = new AtomicInteger();

    public FakeLlmClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public LlmResponse complete(LlmRequest request) {
        calls.incrementAndGet();
        Object response = pickResponse(request);
        String text;
        try {
            text = response instanceof String s ? s : objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new ActionFailedException("La respuesta simulada no se puede convertir a JSON");
        }
        String input = (request.system() != null ? request.system() : "") + request.prompt();
        return new LlmResponse(request.model(), text, estimateTokens(input), estimateTokens(text), LlmResponse.STOP);
    }

    public int calls() {
        return calls.get();
    }

    private Object pickResponse(LlmRequest request) {
        Map<String, Object> config = request.actionConfig() != null ? request.actionConfig() : Map.of();
        if (config.get("fakeRules") instanceof List<?> rules) {
            for (Object rule : rules) {
                if (rule instanceof Map<?, ?> r && r.get("contains") instanceof String contains
                        && request.prompt().contains(contains)) {
                    return r.get("response");
                }
            }
        }
        if (config.containsKey("fakeResponse")) {
            return config.get("fakeResponse");
        }
        throw new ActionFailedException("El proveedor fake necesita 'fakeResponse' o 'fakeRules' en la configuración de la acción");
    }

    private static int estimateTokens(String text) {
        return (text.length() + 3) / 4;
    }
}
