package com.marta.flowstate.dto;

import com.marta.flowstate.model.Invitation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponseDTO {
    private Long id;
    private String code;
    private String email;
    private Long roleId;
    private String roleName;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;

    public static InvitationResponseDTO from(Invitation invitation) {
        return new InvitationResponseDTO(
                invitation.getId(),
                invitation.getCode(),
                invitation.getEmail(),
                invitation.getRol().getId(),
                invitation.getRol().getName(),
                invitation.getExpiresAt(),
                invitation.getUsedAt()
        );
    }
}
