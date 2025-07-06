package com.marta.flowstate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkflowDTO {

    @NotBlank(message = "El nombre del flujo no puede estar vacío")
    private String name;
}
