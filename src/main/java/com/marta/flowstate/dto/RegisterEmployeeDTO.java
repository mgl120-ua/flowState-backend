package com.marta.flowstate.dto;
import lombok.Data;

@Data
public class RegisterEmployeeDTO {
    public String name;
    public String email;
    public Long companyId;
    public Long roleId;
    public String password;

}
