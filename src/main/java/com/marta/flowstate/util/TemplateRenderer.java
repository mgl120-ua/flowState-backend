package com.marta.flowstate.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sustituye las variables {{clave}} o {{a.b}} de una plantilla por los datos de la instancia.
 * Una clave inexistente se sustituye por una cadena vacía; mapas y listas se escriben como JSON.
 */
@Component
public class TemplateRenderer {

    private static final Pattern VARIABLE = Pattern.compile("\\{\\{\\s*([\\w.-]+)\\s*}}");

    private final ObjectMapper objectMapper;

    public TemplateRenderer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String render(String template, Map<String, Object> data) {
        if (template == null) return null;
        Matcher matcher = VARIABLE.matcher(template);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            Object value = DataPath.resolve(data, matcher.group(1));
            matcher.appendReplacement(result, Matcher.quoteReplacement(asText(value)));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public boolean hasVariables(String template) {
        return template != null && VARIABLE.matcher(template).find();
    }

    private String asText(Object value) {
        if (value == null) return "";
        if (value instanceof Map<?, ?> || value instanceof List<?>) {
            try {
                return objectMapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                return value.toString();
            }
        }
        return value.toString();
    }
}
