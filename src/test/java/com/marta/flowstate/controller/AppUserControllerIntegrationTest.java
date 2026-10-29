package com.marta.flowstate.controller;

import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.model.Company;
import com.marta.flowstate.model.Rol;
import com.marta.flowstate.service.AppUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AppUserControllerIntegrationTest.MethodSecurityConfig.class)
class AppUserControllerIntegrationTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppUserService appUserService;

    @MockBean
    private com.marta.flowstate.util.JwtUtil jwtUtil;

    @MockBean
    private com.marta.flowstate.repository.AppUserRepository appUserRepository;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllUsersDoesNotExposePassword() throws Exception {
        authenticateAs("EMPLEADO");
        Company company = new Company(1L, "Empresa");
        Rol rol = new Rol(3L, "EMPLEADO", null, company);
        AppUser user = new AppUser(10L, "Ana", "ana@example.com", "$2a$10$hash", company, rol);
        when(appUserService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("ana@example.com"))
                .andExpect(jsonPath("$[0].companyId").value(1))
                .andExpect(jsonPath("$[0].roleName").value("EMPLEADO"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void deleteUserWithoutAdminRoleIsForbidden() throws Exception {
        authenticateAs("EMPLEADO");

        mockMvc.perform(delete("/users/10"))
                .andExpect(status().isForbidden());

        verify(appUserService, never()).deleteUser(anyLong());
    }

    @Test
    void deleteUserAsAdminCallsService() throws Exception {
        authenticateAs("ADMIN");

        mockMvc.perform(delete("/users/10"))
                .andExpect(status().isOk());

        verify(appUserService).deleteUser(10L);
    }

    private void authenticateAs(String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "user@example.com", null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
}
