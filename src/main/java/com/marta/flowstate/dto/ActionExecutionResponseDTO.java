package com.marta.flowstate.dto;

import com.marta.flowstate.model.ActionExecution;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionExecutionResponseDTO {
    private Long id;
    private Long transitionId;
    private Long actionId;
    private String actionName;
    private String actionType;
    private String status;
    private Integer httpStatus;
    private Long durationMs;
    private String error;
    private String request;
    private Map<String, Object> response;
    private LocalDateTime startedAt;
    private String model;
    private Integer inputTokens;
    private Integer outputTokens;
    private BigDecimal costUsd;
    private String prompt;
    private String stopReason;

    public static ActionExecutionResponseDTO from(ActionExecution e) {
        return new ActionExecutionResponseDTO(
                e.getId(),
                e.getTransition() != null ? e.getTransition().getId() : null,
                e.getAction() != null ? e.getAction().getId() : null,
                e.getActionName(),
                e.getActionType(),
                e.getStatus(),
                e.getHttpStatus(),
                e.getDurationMs(),
                e.getError(),
                e.getRequestSummary(),
                e.getResponseSummary(),
                e.getStartedAt(),
                e.getModel(),
                e.getInputTokens(),
                e.getOutputTokens(),
                e.getCostUsd(),
                e.getPrompt(),
                e.getStopReason()
        );
    }
}
