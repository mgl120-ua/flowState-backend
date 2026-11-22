package com.marta.flowstate.service.llm;

import java.time.Duration;
import java.util.Map;

/**
 * Petición a un modelo de lenguaje.
 *
 * @param model         nombre del modelo en el proveedor
 * @param system        instrucciones de sistema, o null
 * @param prompt        mensaje del usuario, ya renderizado
 * @param outputSchema  JSON Schema que debe cumplir la respuesta, o null para texto libre
 * @param maxTokens     máximo de tokens de salida
 * @param timeout       tiempo máximo de espera de la respuesta
 * @param actionConfig  configuración completa de la acción (la usa el proveedor fake)
 */
public record LlmRequest(String model, String system, String prompt, Map<String, Object> outputSchema,
                         int maxTokens, Duration timeout, Map<String, Object> actionConfig) {
}
