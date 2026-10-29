package com.marta.flowstate.dto;
import lombok.Data;

@Data
public class RegisterEmployeeDTO {
    public String name;
    public String email;
    public String invitationCode; //la empresa y el rol salen de la invitación
    public String password;

}
