package com.marta.flowstate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marta.flowstate.exception.ActionFailedException;
import com.marta.flowstate.util.TemplateRenderer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Acción HTTP: llama a una API externa con plantillas {{clave}} en url, cabeceras y cuerpo.
 *
 * Configuración: {"method", "url", "headers", "body", "timeoutMs", "saveAs"}.
 * Éxito = respuesta 2xx. Si hay saveAs, se guarda {"status", "body"} en los datos de la instancia.
 */
@Component
public class HttpActionHandler {

    static final int DEFAULT_TIMEOUT_MS = 10_000;
    static final int MAX_TIMEOUT_MS = 30_000;
    static final int MAX_RESPONSE_BYTES = 256 * 1024;
    private static final Set<String> METHODS = Set.of("GET", "POST", "PUT", "PATCH", "DELETE");
    private static final Pattern SAVE_AS = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    //Sin redirecciones: una redirección podría llevar a una dirección interna que no se ha validado
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private final HttpTargetValidator targetValidator;
    private final TemplateRenderer templateRenderer;
    private final ObjectMapper objectMapper;

    public HttpActionHandler(HttpTargetValidator targetValidator, TemplateRenderer templateRenderer, ObjectMapper objectMapper) {
        this.targetValidator = targetValidator;
        this.templateRenderer = templateRenderer;
        this.objectMapper = objectMapper;
    }

    public void validateConfig(Map<String, Object> config) {
        if (!(config.get("url") instanceof String url) || url.isBlank()) {
            throw new IllegalArgumentException("La configuración necesita el campo 'url' como texto");
        }
        method(config);
        timeoutMs(config);
        headers(config);
        Object body = config.get("body");
        if (body != null && !(body instanceof String) && !(body instanceof Map<?, ?>) && !(body instanceof java.util.List<?>)) {
            throw new IllegalArgumentException("El campo 'body' debe ser texto o JSON");
        }
        Object saveAs = config.get("saveAs");
        if (saveAs != null && !(saveAs instanceof String s && SAVE_AS.matcher(s).matches())) {
            throw new IllegalArgumentException("El campo 'saveAs' debe ser un nombre de clave (letras, números y _)");
        }
        //Si el host no lleva variables se puede comprobar ya; si las lleva, se comprueba al ejecutar
        if (templateRenderer.hasVariables(url)) {
            targetValidator.parse(url.replaceAll("\\{\\{[^}]*}}", "x"));
        } else {
            targetValidator.validate(url);
        }
    }

    public ActionResult execute(Map<String, Object> config, Map<String, Object> data) {
        HttpMethod method = HttpMethod.valueOf(method(config));
        String url = templateRenderer.render((String) config.get("url"), data);
        String summary = method.name() + " " + url;
        URI uri;
        try {
            uri = targetValidator.validate(url);
        } catch (IllegalArgumentException e) {
            throw new ActionFailedException(e.getMessage(), (Integer) null, summary);
        }

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(timeoutMs(config)));
        RestClient.RequestBodySpec request = RestClient.builder().requestFactory(factory).build()
                .method(method)
                .uri(uri);

        Map<String, String> headers = headers(config);
        headers.forEach((name, value) -> request.header(name, templateRenderer.render(value, data)));
        String body = body(config, data);
        if (body != null) {
            if (headers.keySet().stream().noneMatch(h -> h.equalsIgnoreCase("Content-Type"))) {
                request.contentType(MediaType.APPLICATION_JSON);
            }
            request.body(body);
        }

        try {
            return request.exchange((req, res) -> {
                int status = res.getStatusCode().value();
                String text = readLimited(res.getBody());
                if (text == null) {
                    throw new ActionFailedException("La respuesta supera el máximo de " + MAX_RESPONSE_BYTES / 1024 + " KB", status, summary);
                }
                Object responseBody = parseBody(text);
                if (!res.getStatusCode().is2xxSuccessful()) {
                    throw new ActionFailedException("La API respondió " + status, status, summary);
                }
                Map<String, Object> output = new LinkedHashMap<>();
                output.put("status", status);
                output.put("body", responseBody);
                return new ActionResult(status, summary, (String) config.get("saveAs"), output);
            });
        } catch (ActionFailedException e) {
            throw e;
        } catch (Exception e) {
            String reason = rootMessage(e);
            throw new ActionFailedException("Error llamando a la API: " + reason, summary, e);
        }
    }

    private String method(Map<String, Object> config) {
        Object value = config.getOrDefault("method", "GET");
        if (!(value instanceof String s) || !METHODS.contains(s.toUpperCase())) {
            throw new IllegalArgumentException("Método HTTP no soportado: " + value);
        }
        return s.toUpperCase();
    }

    private int timeoutMs(Map<String, Object> config) {
        Object value = config.get("timeoutMs");
        if (value == null) return DEFAULT_TIMEOUT_MS;
        if (!(value instanceof Number n) || n.intValue() < 1 || n.intValue() > MAX_TIMEOUT_MS) {
            throw new IllegalArgumentException("'timeoutMs' debe estar entre 1 y " + MAX_TIMEOUT_MS);
        }
        return n.intValue();
    }

    private Map<String, String> headers(Map<String, Object> config) {
        Object value = config.get("headers");
        Map<String, String> headers = new LinkedHashMap<>();
        if (value == null) return headers;
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("'headers' debe ser un objeto de texto a texto");
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getValue() instanceof String headerValue)) {
                throw new IllegalArgumentException("La cabecera '" + entry.getKey() + "' debe ser texto");
            }
            headers.put(entry.getKey().toString(), headerValue);
        }
        return headers;
    }

    private String body(Map<String, Object> config, Map<String, Object> data) {
        Object body = config.get("body");
        if (body == null) return null;
        try {
            String template = body instanceof String s ? s : objectMapper.writeValueAsString(body);
            return templateRenderer.render(template, data);
        } catch (IOException e) {
            throw new IllegalArgumentException("El campo 'body' no es un JSON válido");
        }
    }

    //Devuelve null si la respuesta es mayor que el máximo permitido
    private String readLimited(InputStream in) throws IOException {
        byte[] bytes = in.readNBytes(MAX_RESPONSE_BYTES + 1);
        if (bytes.length > MAX_RESPONSE_BYTES) return null;
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private Object parseBody(String text) {
        if (text.isBlank()) return null;
        try {
            return objectMapper.readValue(text, Object.class);
        } catch (IOException e) {
            return text;
        }
    }

    private static String rootMessage(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null) root = root.getCause();
        String name = root.getClass().getSimpleName();
        return root.getMessage() != null ? name + ": " + root.getMessage() : name;
    }
}
