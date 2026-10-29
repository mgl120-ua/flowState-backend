package com.marta.flowstate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InvitationDTO {
    @NotNull(message = "El rol es obligatorio")
    private Long roleId;
    @Email(message = "El email no es válido")
    private String email; //opcional
}
