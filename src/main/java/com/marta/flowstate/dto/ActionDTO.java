package com.marta.flowstate.dto;

import lombok.Data;

@Data
public class ActionDTO {
    private String name;
    private String type;
    private String config; // JSON as string
}