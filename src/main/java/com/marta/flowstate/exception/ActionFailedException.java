package com.marta.flowstate.exception;

import com.marta.flowstate.service.llm.LlmUsage;

/**
 * Fallo de una acción concreta (timeout, respuesta no 2xx, configuración inválida...).
 */
public class ActionFailedException extends RuntimeException {

    private final Integer httpStatus;
    private final String requestSummary;
    private final LlmUsage llmUsage;

    public ActionFailedException(String message) {
        this(message, null, (String) null);
    }

    public ActionFailedException(String message, Integer httpStatus, String requestSummary) {
        super(message);
        this.httpStatus = httpStatus;
        this.requestSummary = requestSummary;
        this.llmUsage = null;
    }

    public ActionFailedException(String message, String requestSummary, Throwable cause) {
        super(message, cause);
        this.httpStatus = null;
        this.requestSummary = requestSummary;
        this.llmUsage = null;
    }

    //Fallo de una acción LLM: se guardan en la traza los tokens consumidos aunque la respuesta no sirva
    public ActionFailedException(String message, String requestSummary, LlmUsage llmUsage) {
        super(message);
        this.httpStatus = null;
        this.requestSummary = requestSummary;
        this.llmUsage = llmUsage;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public String getRequestSummary() {
        return requestSummary;
    }

    public LlmUsage getLlmUsage() {
        return llmUsage;
    }
}
