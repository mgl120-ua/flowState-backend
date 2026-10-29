package com.marta.flowstate.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Consumo de acciones LLM de la empresa entre dos fechas (ambas incluidas), más el límite mensual
 * y lo consumido en el mes en curso.
 */
public record LlmUsageDTO(LocalDate from, LocalDate to, Totals totals, List<ModelUsage> byModel,
                          List<FlowUsage> byFlow, Long monthlyTokenLimit, long tokensThisMonth) {

    public record Totals(long calls, long inputTokens, long outputTokens, BigDecimal costUsd) {
    }

    public record ModelUsage(String model, long calls, long inputTokens, long outputTokens, BigDecimal costUsd) {
    }

    public record FlowUsage(Long flowId, String flowName, long calls, long inputTokens, long outputTokens,
                            BigDecimal costUsd) {
    }
}
