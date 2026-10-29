package com.marta.flowstate.controller;

import com.marta.flowstate.model.Company;
import com.marta.flowstate.model.Invitation;
import com.marta.flowstate.model.Rol;
import com.marta.flowstate.service.InvitationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvitationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(InvitationControllerIntegrationTest.MethodSecurityConfig.class)
class InvitationControllerIntegrationTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvitationService invitationService;

    @MockBean
    private com.marta.flowstate.util.JwtUtil jwtUtil;

    @MockBean
    private com.marta.flowstate.repository.AppUserRepository appUserRepository;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void employeeCannotCreateInvitations() throws Exception {
        authenticateAs("EMPLEADO");

        mockMvc.perform(post("/invitations").contentType(MediaType.APPLICATION_JSON).content("{\"roleId\":3}"))
                .andExpect(status().isForbidden());

        verify(invitationService, never()).createInvitation(any());
    }

    @Test
    void adminCreatesInvitation() throws Exception {
        authenticateAs("ADMIN");
        Company company = new Company(1L, "Empresa");
        Rol rol = new Rol(3L, "EMPLEADO", null, company);
        Invitation invitation = new Invitation(5L, "codigo-secreto", null, company, rol, LocalDateTime.now(), LocalDateTime.now().plusDays(7), null);
        when(invitationService.createInvitation(any())).thenReturn(invitation);

        mockMvc.perform(post("/invitations").contentType(MediaType.APPLICATION_JSON).content("{\"roleId\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("codigo-secreto"))
                .andExpect(jsonPath("$.roleName").value("EMPLEADO"));
    }

    @Test
    void invitationWithoutRoleIsRejected() throws Exception {
        authenticateAs("ADMIN");

        mockMvc.perform(post("/invitations").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verify(invitationService, never()).createInvitation(any());
    }

    private void authenticateAs(String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "user@example.com", null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
}
