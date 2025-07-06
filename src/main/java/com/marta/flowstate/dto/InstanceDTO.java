package com.marta.flowstate.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.Map;

@Data
public class InstanceDTO {

    @NotNull(message = "Data es obligatorio")
    private Map<String, Object> data;
}
