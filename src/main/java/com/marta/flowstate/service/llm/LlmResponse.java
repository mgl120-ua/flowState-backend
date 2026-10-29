package com.marta.flowstate.service.llm;

/**
 * Respuesta de un modelo de lenguaje.
 *
 * @param model         modelo que respondió
 * @param text          texto generado (JSON si se pidió un esquema)
 * @param inputTokens   tokens del prompt, o null si el proveedor no los da
 * @param outputTokens  tokens generados, o null si el proveedor no los da
 * @param stopReason    motivo de fin: "stop" si terminó, "length" si se cortó por maxTokens
 */
public record LlmResponse(String model, String text, Integer inputTokens, Integer outputTokens, String stopReason) {

    public static final String STOP = "stop";
    public static final String LENGTH = "length";
}
