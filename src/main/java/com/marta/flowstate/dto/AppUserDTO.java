package com.marta.flowstate.dto;

import lombok.Data;

@Data
public class AppUserDTO {
    private String name;
    private String email;
    private Long companyId;
    private Long roleId;
}