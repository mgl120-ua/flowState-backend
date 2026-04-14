package com.marta.flowstate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marta.flowstate.dto.LoginDTO;
import com.marta.flowstate.repository.AppUserRepository;
import com.marta.flowstate.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private AppUserRepository userRepo;

    @MockBean
    private com.marta.flowstate.util.JwtUtil jwtUtil;

    @Test
    void loginEndpointReturnsTokenAndStatus200() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("user@example.com");
        loginDTO.setPassword("password123");

        Map<String, Object> responsePayload = Map.of(
                "token", "jwt-token-123",
                "user", Map.of(
                        "id", 1,
                        "name", "Test User",
                        "email", "user@example.com",
                        "companyId", 10,
                        "role", "ADMIN"
                )
        );

        when(authService.login(any(LoginDTO.class))).thenReturn(responsePayload);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responsePayload)));
    }
}
