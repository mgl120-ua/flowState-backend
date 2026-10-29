package com.marta.flowstate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class ActionDTO {
    @NotBlank(message = "El nombre de la acción es obligatorio")
    private String name;
    @NotBlank(message = "El tipo de la acción es obligatorio")
    @Schema(description = "Tipo de acción: EMAIL")
    private String type;
    @NotNull(message = "La configuración de la acción es obligatoria")
    @Schema(description = "Configuración según el tipo. EMAIL: {\"to\", \"subject\", \"body\"}")
    private Map<String, Object> config;
}
