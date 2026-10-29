package com.marta.flowstate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionResponseDTO {
    private Long id;
    private String name;
    private String type;
    private Map<String, Object> config;
}
