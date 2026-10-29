package com.marta.flowstate.controller;

import com.marta.flowstate.dto.ActionResponseDTO;
import com.marta.flowstate.service.ActionService;
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

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ActionController.class, TransitionActionController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(ActionControllerIntegrationTest.MethodSecurityConfig.class)
class ActionControllerIntegrationTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {
    }

    private static final String EMAIL_ACTION = """
            {"name":"Aviso","type":"EMAIL","config":{"to":"a@b.com","subject":"Hola","body":"Texto"}}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActionService actionService;

    @MockBean
    private com.marta.flowstate.util.JwtUtil jwtUtil;

    @MockBean
    private com.marta.flowstate.repository.AppUserRepository appUserRepository;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void employeeCannotCreateActions() throws Exception {
        authenticateAs("EMPLEADO");

        mockMvc.perform(post("/actions").contentType(MediaType.APPLICATION_JSON).content(EMAIL_ACTION))
                .andExpect(status().isForbidden());

        verify(actionService, never()).createAction(any());
    }

    @Test
    void employeeCannotAttachActionsToTransitions() throws Exception {
        authenticateAs("EMPLEADO");

        mockMvc.perform(post("/flows/1/transitions/2/actions").contentType(MediaType.APPLICATION_JSON).content("{\"actionId\":5}"))
                .andExpect(status().isForbidden());

        verify(actionService, never()).attachAction(any(), any(), any());
    }

    @Test
    void adminCreatesAction() throws Exception {
        authenticateAs("ADMIN");
        when(actionService.createAction(any())).thenReturn(
                new ActionResponseDTO(5L, "Aviso", "EMAIL", Map.of("to", "a@b.com")));

        mockMvc.perform(post("/actions").contentType(MediaType.APPLICATION_JSON).content(EMAIL_ACTION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.config.to").value("a@b.com"));
    }

    @Test
    void actionWithoutConfigIsRejected() throws Exception {
        authenticateAs("ADMIN");

        mockMvc.perform(post("/actions").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Aviso\",\"type\":\"EMAIL\"}"))
                .andExpect(status().isBadRequest());

        verify(actionService, never()).createAction(any());
    }

    @Test
    void adminListsActions() throws Exception {
        authenticateAs("ADMIN");
        when(actionService.getActions()).thenReturn(List.of(new ActionResponseDTO(5L, "Aviso", "EMAIL", Map.of())));

        mockMvc.perform(get("/actions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Aviso"));
    }

    private void authenticateAs(String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "user@example.com", null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
}
