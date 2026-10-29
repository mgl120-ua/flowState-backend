package com.marta.flowstate.service.llm;

/**
 * Proveedor de modelos de lenguaje. Se elige con flowstate.llm.provider (ollama o fake).
 * Si la llamada no llega a producir respuesta, lanza ActionFailedException.
 */
public interface LlmClient {

    LlmResponse complete(LlmRequest request);
}
