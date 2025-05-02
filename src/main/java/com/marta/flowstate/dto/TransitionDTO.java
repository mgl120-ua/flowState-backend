package com.marta.flowstate.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransitionDTO {

    private String action;
    private String condition;

    @NotNull(message = "El estado origen es obligatorio")
    @Min(value = 1, message = "El estado origen debe ser mayor que 0")
    private Long sourceStateId;

    @NotNull(message = "El estado destino es obligatorio")
    @Min(value = 1, message = "El estado destino debe ser mayor que 0")
    private Long targetStateId;

    @NotNull(message = "El id workflow es obligatorio")
    @Min(value = 1, message = "El id workflow debe ser mayor que 0")
    private Long workflowId;
}
