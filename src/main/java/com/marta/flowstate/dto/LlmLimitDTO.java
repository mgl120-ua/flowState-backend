package com.marta.flowstate.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlmLimitDTO {
    //Tokens LLM al mes; null quita el límite
    @PositiveOrZero
    private Long monthlyTokenLimit;
}
