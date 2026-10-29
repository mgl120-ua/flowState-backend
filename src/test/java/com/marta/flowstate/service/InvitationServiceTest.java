package com.marta.flowstate.service;

import com.marta.flowstate.dto.InvitationDTO;
import com.marta.flowstate.exception.NotFoundException;
import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.model.Company;
import com.marta.flowstate.model.Invitation;
import com.marta.flowstate.model.Rol;
import com.marta.flowstate.repository.InvitationRepository;
import com.marta.flowstate.repository.RolRepository;
import com.marta.flowstate.security.SessionUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private SessionUserService sessionUserService;

    @InjectMocks
    private InvitationService invitationService;

    private final Company company = new Company(1L, "Empresa");

    @Test
    void createInvitationForRoleOfOwnCompany() {
        Rol rol = new Rol(3L, "EMPLEADO", null, company);
        when(sessionUserService.getCurrentUser()).thenReturn(admin());
        when(rolRepository.findByCompanyIdAndId(1L, 3L)).thenReturn(Optional.of(rol));
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(inv -> inv.getArgument(0));
        InvitationDTO dto = new InvitationDTO();
        dto.setRoleId(3L);
        dto.setEmail("emp@example.com");

        Invitation invitation = invitationService.createInvitation(dto);

        assertEquals(company, invitation.getCompany());
        assertEquals(rol, invitation.getRol());
        assertEquals("emp@example.com", invitation.getEmail());
        assertEquals(43, invitation.getCode().length());
        assertTrue(invitation.getExpiresAt().isAfter(LocalDateTime.now().plusDays(InvitationService.DAYS_VALID - 1)));
        assertNull(invitation.getUsedAt());
    }

    @Test
    void createInvitationGeneratesDifferentCodes() {
        Rol rol = new Rol(3L, "EMPLEADO", null, company);
        when(sessionUserService.getCurrentUser()).thenReturn(admin());
        when(rolRepository.findByCompanyIdAndId(1L, 3L)).thenReturn(Optional.of(rol));
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(inv -> inv.getArgument(0));
        InvitationDTO dto = new InvitationDTO();
        dto.setRoleId(3L);

        assertNotEquals(invitationService.createInvitation(dto).getCode(), invitationService.createInvitation(dto).getCode());
    }

    @Test
    void createInvitationForRoleOfOtherCompanyFails() {
        when(sessionUserService.getCurrentUser()).thenReturn(admin());
        when(rolRepository.findByCompanyIdAndId(1L, 99L)).thenReturn(Optional.empty());
        InvitationDTO dto = new InvitationDTO();
        dto.setRoleId(99L);

        assertThrows(IllegalArgumentException.class, () -> invitationService.createInvitation(dto));
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void deleteInvitationOfOtherCompanyReturnsNotFound() {
        Company other = new Company(2L, "Otra");
        Invitation invitation = new Invitation(5L, "c", null, other, new Rol(9L, "X", null, other), LocalDateTime.now(), LocalDateTime.now().plusDays(1), null);
        when(sessionUserService.getCurrentCompanyId()).thenReturn(1L);
        when(invitationRepository.findById(5L)).thenReturn(Optional.of(invitation));

        assertThrows(NotFoundException.class, () -> invitationService.deleteInvitation(5L));
        verify(invitationRepository, never()).delete(any());
    }

    private AppUser admin() {
        AppUser admin = new AppUser();
        admin.setId(10L);
        admin.setCompany(company);
        return admin;
    }
}
