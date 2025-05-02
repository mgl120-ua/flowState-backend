package com.marta.flowstate.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkflowDTO {

    @NotBlank(message = "El nombre del flujo no puede estar vacío")
    private String name;

    @NotNull(message = "El id de la empresa es obligatorio")
    @Min(value = 1, message = "El id de la empresa debe ser mayor que 0")
    private Long companyId;
}
