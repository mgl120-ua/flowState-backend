package com.marta.flowstate.exception;

/**
 * Fallo de una acción concreta (timeout, respuesta no 2xx, configuración inválida...).
 */
public class ActionFailedException extends RuntimeException {

    private final Integer httpStatus;
    private final String requestSummary;

    public ActionFailedException(String message) {
        this(message, null, (String) null);
    }

    public ActionFailedException(String message, Integer httpStatus, String requestSummary) {
        super(message);
        this.httpStatus = httpStatus;
        this.requestSummary = requestSummary;
    }

    public ActionFailedException(String message, String requestSummary, Throwable cause) {
        super(message, cause);
        this.httpStatus = null;
        this.requestSummary = requestSummary;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public String getRequestSummary() {
        return requestSummary;
    }
}
