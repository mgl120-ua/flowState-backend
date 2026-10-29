package com.marta.flowstate.service.llm;

import java.math.BigDecimal;

/**
 * Lo que se guarda en la traza de una llamada a un LLM, haya ido bien o mal.
 */
public record LlmUsage(String model, Integer inputTokens, Integer outputTokens, BigDecimal costUsd,
                       String prompt, String stopReason) {
}
