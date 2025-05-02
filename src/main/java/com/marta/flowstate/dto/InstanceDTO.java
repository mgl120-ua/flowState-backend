package com.marta.flowstate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class InstanceDTO {

    @NotBlank(message = "Data es obligatorio")
    private String data; // JSON

    @NotNull(message = "El estado origen es obligatorio")
    @Min(value = 1, message = "El estado origen debe ser mayor que 0")
    private Long stateId;

    @NotNull(message = "El id del flujo es obligatorio")
    @Min(value = 1, message = "El id del flujo debe ser mayor que 0")
    private Long workflowId;

    @NotNull(message = "El id del user es obligatorio")
    @Min(value = 1, message = "El id del user debe ser mayor que 0")
    private Long userId;
}
