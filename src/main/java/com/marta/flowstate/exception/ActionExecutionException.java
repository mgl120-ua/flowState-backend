package com.marta.flowstate.exception;

import com.marta.flowstate.model.ActionExecution;

import java.util.List;

/**
 * Una acción de la transición ha fallado y la transición se ha deshecho.
 * Lleva las trazas de la ejecución (incluida la fallida) para guardarlas tras el rollback.
 */
public class ActionExecutionException extends RuntimeException {

    private final String actionName;
    private final transient List<ActionExecution> executions;

    public ActionExecutionException(String actionName, String reason, List<ActionExecution> executions, Throwable cause) {
        super(reason, cause);
        this.actionName = actionName;
        this.executions = List.copyOf(executions);
    }

    public String getActionName() {
        return actionName;
    }

    public List<ActionExecution> getExecutions() {
        return executions;
    }
}
