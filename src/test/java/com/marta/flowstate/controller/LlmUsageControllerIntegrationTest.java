package com.marta.flowstate.controller;

import com.marta.flowstate.dto.LlmLimitDTO;
import com.marta.flowstate.dto.LlmUsageDTO;
import com.marta.flowstate.service.CompanyService;
import com.marta.flowstate.service.LlmUsageService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({LlmUsageController.class, CompanyController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(LlmUsageControllerIntegrationTest.MethodSecurityConfig.class)
class LlmUsageControllerIntegrationTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LlmUsageService llmUsageService;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private com.marta.flowstate.util.JwtUtil jwtUtil;

    @MockBean
    private com.marta.flowstate.repository.AppUserRepository appUserRepository;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private static void loginAs(String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin@acme.com", null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    @Test
    void adminGetsUsageForRange() throws Exception {
        loginAs("ADMIN");
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);
        when(llmUsageService.getUsage(from, to)).thenReturn(new LlmUsageDTO(from, to,
                new LlmUsageDTO.Totals(3, 300, 30, BigDecimal.ZERO),
                List.of(new LlmUsageDTO.ModelUsage("qwen2.5:3b", 3, 300, 30, BigDecimal.ZERO)),
                List.of(), 1000L, 330));

        mockMvc.perform(get("/usage/llm").param("from", "2026-10-01").param("to", "2026-10-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totals.calls").value(3))
                .andExpect(jsonPath("$.byModel[0].model").value("qwen2.5:3b"))
                .andExpect(jsonPath("$.monthlyTokenLimit").value(1000))
                .andExpect(jsonPath("$.tokensThisMonth").value(330));
    }

    @Test
    void nonAdminCannotSeeUsage() throws Exception {
        loginAs("USER");

        mockMvc.perform(get("/usage/llm")).andExpect(status().isForbidden());
        verify(llmUsageService, never()).getUsage(any(), any());
    }

    @Test
    void adminSetsAndValidatesLimit() throws Exception {
        loginAs("ADMIN");
        when(llmUsageService.setMonthlyTokenLimit(5000L)).thenReturn(new LlmLimitDTO(5000L));

        mockMvc.perform(put("/companies/me/llm-limit").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monthlyTokenLimit\":5000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyTokenLimit").value(5000));

        mockMvc.perform(put("/companies/me/llm-limit").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monthlyTokenLimit\":-1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonAdminCannotChangeLimit() throws Exception {
        loginAs("USER");

        mockMvc.perform(put("/companies/me/llm-limit").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monthlyTokenLimit\":5000}"))
                .andExpect(status().isForbidden());
        verify(llmUsageService, never()).setMonthlyTokenLimit(any());
    }
}
