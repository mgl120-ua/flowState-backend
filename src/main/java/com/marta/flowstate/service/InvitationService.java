package com.marta.flowstate.service;

import com.marta.flowstate.dto.InvitationDTO;
import com.marta.flowstate.exception.NotFoundException;
import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.model.Invitation;
import com.marta.flowstate.model.Rol;
import com.marta.flowstate.repository.InvitationRepository;
import com.marta.flowstate.repository.RolRepository;
import com.marta.flowstate.security.SessionUserService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
public class InvitationService {

    static final int DAYS_VALID = 7;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final InvitationRepository invitationRepository;
    private final RolRepository rolRepository;
    private final SessionUserService sessionUserService;

    public InvitationService(InvitationRepository invitationRepository,
                             RolRepository rolRepository,
                             SessionUserService sessionUserService) {
        this.invitationRepository = invitationRepository;
        this.rolRepository = rolRepository;
        this.sessionUserService = sessionUserService;
    }

    public Invitation createInvitation(InvitationDTO dto) {
        AppUser admin = sessionUserService.getCurrentUser();
        Long companyId = admin.getCompany().getId();
        Rol rol = rolRepository.findByCompanyIdAndId(companyId, dto.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Rol " + dto.getRoleId() + " no encontrado o no pertenece a tu empresa"));

        LocalDateTime now = LocalDateTime.now();
        Invitation invitation = new Invitation();
        invitation.setCode(generateCode());
        invitation.setEmail(dto.getEmail() != null && !dto.getEmail().isBlank() ? dto.getEmail().trim() : null);
        invitation.setCompany(admin.getCompany());
        invitation.setRol(rol);
        invitation.setCreatedAt(now);
        invitation.setExpiresAt(now.plusDays(DAYS_VALID));
        return invitationRepository.save(invitation);
    }

    public List<Invitation> getInvitations() {
        return invitationRepository.findByCompanyIdOrderByCreatedAtDesc(sessionUserService.getCurrentCompanyId());
    }

    public void deleteInvitation(Long id) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        Invitation invitation = invitationRepository.findById(id)
                .filter(i -> i.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new NotFoundException("Invitación " + id + " no encontrada"));
        invitationRepository.delete(invitation);
    }

    //32 bytes aleatorios en Base64 URL: 43 caracteres, imposible de adivinar
    private String generateCode() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
