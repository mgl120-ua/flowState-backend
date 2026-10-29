package com.marta.flowstate.service;

import java.util.Map;

/**
 * Resultado de una acción ejecutada con éxito.
 *
 * @param httpStatus      código HTTP de la respuesta, o null si la acción no es HTTP
 * @param requestSummary  descripción breve de lo que se hizo, sin secretos (para la traza)
 * @param saveAs          clave de los datos de la instancia donde guardar output, o null
 * @param output          valor a guardar en los datos de la instancia y en la traza
 */
public record ActionResult(Integer httpStatus, String requestSummary, String saveAs, Map<String, Object> output) {
}
