package com.marta.flowstate.service;

import com.marta.flowstate.dto.LlmUsageDTO;
import com.marta.flowstate.exception.ActionFailedException;
import com.marta.flowstate.model.Company;
import com.marta.flowstate.repository.ActionExecutionRepository;
import com.marta.flowstate.repository.CompanyRepository;
import com.marta.flowstate.security.SessionUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LlmUsageServiceTest {

    @Mock
    private ActionExecutionRepository actionExecutionRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private SessionUserService sessionUserService;

    private LlmUsageService service;
    private Company company;

    @BeforeEach
    void setUp() {
        service = new LlmUsageService(actionExecutionRepository, companyRepository, sessionUserService);
        company = new Company(1L, "Acme");
    }

    @Test
    void withoutLimitDoesNotQueryUsage() {
        service.checkMonthlyLimit(company);
        service.checkMonthlyLimit(null);

        verifyNoInteractions(actionExecutionRepository);
    }

    @Test
    void failsWhenMonthlyLimitIsReached() {
        company.setLlmMonthlyTokenLimit(1000L);
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        when(actionExecutionRepository.sumLlmTokensSince(1L, startOfMonth)).thenReturn(999L, 1000L);

        assertDoesNotThrow(() -> service.checkMonthlyLimit(company));
        ActionFailedException ex = assertThrows(ActionFailedException.class, () -> service.checkMonthlyLimit(company));
        assertEquals("Límite mensual de tokens LLM alcanzado (1000 de 1000)", ex.getMessage());
    }

    @Test
    void usageAggregatesRowsOfTheSessionCompany() {
        company.setLlmMonthlyTokenLimit(50_000L);
        when(sessionUserService.getCurrentCompanyId()).thenReturn(1L);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        LocalDateTime from = LocalDateTime.of(2026, 10, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 11, 1, 0, 0);
        when(actionExecutionRepository.llmUsageByModel(1L, from, to)).thenReturn(List.of(
                new Object[]{"llama3.2:3b", 2L, 100L, 20L, new BigDecimal("0.000000")},
                new Object[]{"qwen2.5:3b", 3L, 300L, null, null}));
        when(actionExecutionRepository.llmUsageByFlow(1L, from, to)).thenReturn(List.<Object[]>of(
                new Object[]{7L, "Triaje", 5L, 400L, 20L, BigDecimal.ZERO}));
        when(actionExecutionRepository.sumLlmTokensSince(eq(1L), any())).thenReturn(420L);

        LlmUsageDTO usage = service.getUsage(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

        assertEquals(5, usage.totals().calls());
        assertEquals(400, usage.totals().inputTokens());
        assertEquals(20, usage.totals().outputTokens());
        assertEquals(2, usage.byModel().size());
        assertEquals(0, usage.byModel().get(1).outputTokens());
        assertEquals("Triaje", usage.byFlow().get(0).flowName());
        assertEquals(50_000L, usage.monthlyTokenLimit());
        assertEquals(420L, usage.tokensThisMonth());
    }

    @Test
    void usageRejectsInvertedRange() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getUsage(LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 1)));
    }

    @Test
    void setLimitUpdatesOnlyTheSessionCompany() {
        when(sessionUserService.getCurrentCompanyId()).thenReturn(1L);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        assertEquals(2000L, service.setMonthlyTokenLimit(2000L).getMonthlyTokenLimit());
        assertEquals(2000L, company.getLlmMonthlyTokenLimit());
        assertNull(service.setMonthlyTokenLimit(null).getMonthlyTokenLimit());
        assertNull(company.getLlmMonthlyTokenLimit());
        verify(companyRepository, times(2)).save(company);
    }
}
