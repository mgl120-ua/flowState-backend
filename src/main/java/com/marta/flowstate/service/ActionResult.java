package com.marta.flowstate.service;

import com.marta.flowstate.service.llm.LlmUsage;

import java.util.Map;

/**
 * Resultado de una acción ejecutada con éxito.
 *
 * @param httpStatus      código HTTP de la respuesta, o null si la acción no es HTTP
 * @param requestSummary  descripción breve de lo que se hizo, sin secretos (para la traza)
 * @param saveAs          clave de los datos de la instancia donde guardar output, o null
 * @param output          valor a guardar en los datos de la instancia y en la traza
 * @param llmUsage        modelo, tokens y coste si la acción es LLM, o null
 */
public record ActionResult(Integer httpStatus, String requestSummary, String saveAs, Map<String, Object> output,
                           LlmUsage llmUsage) {

    public ActionResult(Integer httpStatus, String requestSummary, String saveAs, Map<String, Object> output) {
        this(httpStatus, requestSummary, saveAs, output, null);
    }
}
