package com.marta.flowstate.dto;

import lombok.Data;

@Data
public class AppUserDTO {
    private String name;
    private String email;
    private String password;
    private Long companyId; //para empleados
    private String companyName; //para admin
    private Long roleId; //opcional para empleados
}
