package com.marta.flowstate.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marta.flowstate.exception.ActionFailedException;
import com.marta.flowstate.model.Action;
import com.marta.flowstate.util.TemplateRenderer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActionExecutorService {

    private final MailService mailService;
    private final ObjectMapper objectMapper;
    private final TemplateRenderer templateRenderer;
    private final HttpActionHandler httpActionHandler;

    public ActionResult execute(Action action, Map<String, Object> instanceData) {
        Map<String, Object> config;
        try {
            config = action.getConfig() == null ? Map.of()
                    : objectMapper.readValue(action.getConfig(), new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new ActionFailedException("La configuración de la acción no es un JSON válido");
        }
        String type = action.getType().toUpperCase(Locale.ROOT);

        return switch (type) {
            case "EMAIL" -> handleEmail(config, instanceData);
            case "HTTP" -> httpActionHandler.execute(config, instanceData);
            default -> throw new ActionFailedException("La acción " + type + " no está implementada");
        };
    }

    /**
     * Comprueba que la configuración tiene la forma que espera el tipo de acción.
     * Se usa al crear o editar una acción, para no descubrir el error al ejecutarla.
     */
    public void validateConfig(String type, Map<String, Object> config) {
        switch (type) {
            case "EMAIL" -> {
                requireString(config, "to");
                requireString(config, "subject");
                requireString(config, "body");
            }
            case "HTTP" -> httpActionHandler.validateConfig(config);
            default -> throw new IllegalArgumentException("Tipo de acción no soportado: " + type);
        }
    }

    private static void requireString(Map<String, Object> config, String key) {
        if (!(config.get(key) instanceof String value) || value.isBlank()) {
            throw new IllegalArgumentException("La configuración necesita el campo '" + key + "' como texto");
        }
    }

    private ActionResult handleEmail(Map<String, Object> config, Map<String, Object> instanceData) {
        String to = templateRenderer.render((String) config.get("to"), instanceData);
        String subject = templateRenderer.render((String) config.get("subject"), instanceData);
        String body = templateRenderer.render((String) config.get("body"), instanceData);

        mailService.sendEmail(to, subject, body);
        return new ActionResult(null, "EMAIL " + to, null, null);
    }
}
