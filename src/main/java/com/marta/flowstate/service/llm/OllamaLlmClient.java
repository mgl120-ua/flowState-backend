package com.marta.flowstate.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marta.flowstate.exception.ActionFailedException;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.ConnectException;
import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Modelos locales con Ollama (https://ollama.com), sin clave ni coste. Usa POST /api/chat sin streaming;
 * si hay esquema se envía en "format" para que el modelo genere un JSON que lo cumpla.
 */
public class OllamaLlmClient implements LlmClient {

    //HTTP/1.1: Ollama no usa HTTP/2 y así se evita la negociación de h2c en cada petición
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public OllamaLlmClient(String baseUrl, ObjectMapper objectMapper) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.objectMapper = objectMapper;
    }

    @Override
    public LlmResponse complete(LlmRequest request) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(request.timeout());
        RestClient client = RestClient.builder().requestFactory(factory).build();

        try {
            return client.post()
                    .uri(baseUrl + "/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    //Serializado aquí para enviar Content-Length en lugar de un cuerpo por trozos
                    .body(objectMapper.writeValueAsBytes(body(request)))
                    .exchange((req, res) -> {
                        String text = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        int status = res.getStatusCode().value();
                        if (status == 404) {
                            throw new ActionFailedException("El modelo '" + request.model()
                                    + "' no está descargado en Ollama (ejecuta: ollama pull " + request.model() + ")");
                        }
                        if (!res.getStatusCode().is2xxSuccessful()) {
                            throw new ActionFailedException("Ollama respondió " + status + ": " + errorMessage(text));
                        }
                        return parse(text, request.model());
                    });
        } catch (ActionFailedException e) {
            throw e;
        } catch (Exception e) {
            if (hasCause(e, HttpTimeoutException.class)) {
                throw new ActionFailedException("Ollama no respondió en " + request.timeout().toSeconds() + " s");
            }
            if (hasCause(e, ConnectException.class)) {
                throw new ActionFailedException("No se puede conectar con Ollama en " + baseUrl + ". ¿Está arrancado?");
            }
            throw new ActionFailedException("Error llamando a Ollama: " + e.getMessage());
        }
    }

    private Map<String, Object> body(LlmRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();
        if (request.system() != null && !request.system().isBlank()) {
            messages.add(Map.of("role", "system", "content", request.system()));
        }
        messages.add(Map.of("role", "user", "content", request.prompt()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", request.model());
        body.put("messages", messages);
        body.put("stream", false);
        if (request.outputSchema() != null) {
            body.put("format", request.outputSchema());
        }
        //Temperatura 0 para que la misma entrada dé la misma clasificación
        body.put("options", Map.of("num_predict", request.maxTokens(), "temperature", 0));
        return body;
    }

    private LlmResponse parse(String text, String requestedModel) throws IOException {
        JsonNode json = objectMapper.readTree(text);
        JsonNode content = json.path("message").path("content");
        if (!content.isTextual()) {
            throw new ActionFailedException("Respuesta de Ollama sin contenido");
        }
        String model = json.path("model").asText(requestedModel);
        String doneReason = json.path("done_reason").asText(LlmResponse.STOP);
        return new LlmResponse(model, content.asText(), intOrNull(json, "prompt_eval_count"),
                intOrNull(json, "eval_count"), doneReason);
    }

    private String errorMessage(String text) {
        try {
            JsonNode error = objectMapper.readTree(text).path("error");
            if (error.isTextual()) return error.asText();
        } catch (IOException ignored) {
            //No es JSON: se devuelve el texto tal cual
        }
        return text.length() > 200 ? text.substring(0, 200) : text;
    }

    private static Integer intOrNull(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node != null && node.canConvertToInt() ? node.asInt() : null;
    }

    private static boolean hasCause(Throwable e, Class<? extends Throwable> type) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (type.isInstance(t)) return true;
        }
        return false;
    }
}
