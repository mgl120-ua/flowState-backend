package com.marta.flowstate.dto;

import com.marta.flowstate.model.AppUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private Long companyId;
    private String companyName;
    private Long roleId;
    private String roleName;

    public static AppUserResponseDTO from(AppUser user) {
        return new AppUserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCompany() != null ? user.getCompany().getId() : null,
                user.getCompany() != null ? user.getCompany().getName() : null,
                user.getRol() != null ? user.getRol().getId() : null,
                user.getRol() != null ? user.getRol().getName() : null
        );
    }
}
