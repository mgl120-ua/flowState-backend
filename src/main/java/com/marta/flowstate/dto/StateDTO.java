package com.marta.flowstate.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.marta.flowstate.model.StateType;
import lombok.Data;

@Data
public class StateDTO {

    @NotBlank(message = "El nombre del estado no puede estar vacío")
    private String name;
    private StateType type;


    @NotNull(message = "El workflowId es obligatorio")
    @Min(value = 1, message = "El workflowId debe ser mayor que 0")
    private Long workflowId;
}
