package com.marta.flowstate.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransitionActionDTO {
    @NotNull(message = "La acción es obligatoria")
    private Long actionId;
    @Min(value = 0, message = "La posición no puede ser negativa")
    private Integer position;
}
