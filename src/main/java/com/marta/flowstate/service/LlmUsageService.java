package com.marta.flowstate.service;

import com.marta.flowstate.dto.LlmLimitDTO;
import com.marta.flowstate.dto.LlmUsageDTO;
import com.marta.flowstate.exception.ActionFailedException;
import com.marta.flowstate.exception.NotFoundException;
import com.marta.flowstate.model.Company;
import com.marta.flowstate.repository.ActionExecutionRepository;
import com.marta.flowstate.repository.CompanyRepository;
import com.marta.flowstate.security.SessionUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Consumo de las acciones LLM por empresa y límite mensual de tokens.
 */
@Service
public class LlmUsageService {

    private final ActionExecutionRepository actionExecutionRepository;
    private final CompanyRepository companyRepository;
    private final SessionUserService sessionUserService;

    public LlmUsageService(ActionExecutionRepository actionExecutionRepository, CompanyRepository companyRepository,
                           SessionUserService sessionUserService) {
        this.actionExecutionRepository = actionExecutionRepository;
        this.companyRepository = companyRepository;
        this.sessionUserService = sessionUserService;
    }

    /**
     * Falla si la empresa ya ha consumido su límite mensual de tokens. Se llama antes de cada llamada al modelo.
     */
    public void checkMonthlyLimit(Company company) {
        if (company == null || company.getLlmMonthlyTokenLimit() == null) return;
        long limit = company.getLlmMonthlyTokenLimit();
        long used = actionExecutionRepository.sumLlmTokensSince(company.getId(), startOfMonth());
        if (used >= limit) {
            throw new ActionFailedException("Límite mensual de tokens LLM alcanzado (" + used + " de " + limit + ")");
        }
    }

    public LlmUsageDTO getUsage(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now();
        LocalDate start = from != null ? from : today.withDayOfMonth(1);
        LocalDate end = to != null ? to : today;
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("'from' no puede ser posterior a 'to'");
        }
        Company company = currentCompany();
        LocalDateTime fromTime = start.atStartOfDay();
        LocalDateTime toTime = end.plusDays(1).atStartOfDay();

        List<LlmUsageDTO.ModelUsage> byModel = actionExecutionRepository.llmUsageByModel(company.getId(), fromTime, toTime)
                .stream()
                .map(r -> new LlmUsageDTO.ModelUsage((String) r[0], toLong(r[1]), toLong(r[2]), toLong(r[3]), toCost(r[4])))
                .toList();
        List<LlmUsageDTO.FlowUsage> byFlow = actionExecutionRepository.llmUsageByFlow(company.getId(), fromTime, toTime)
                .stream()
                .map(r -> new LlmUsageDTO.FlowUsage((Long) r[0], (String) r[1], toLong(r[2]), toLong(r[3]), toLong(r[4]), toCost(r[5])))
                .toList();
        LlmUsageDTO.Totals totals = new LlmUsageDTO.Totals(
                byModel.stream().mapToLong(LlmUsageDTO.ModelUsage::calls).sum(),
                byModel.stream().mapToLong(LlmUsageDTO.ModelUsage::inputTokens).sum(),
                byModel.stream().mapToLong(LlmUsageDTO.ModelUsage::outputTokens).sum(),
                byModel.stream().map(LlmUsageDTO.ModelUsage::costUsd).reduce(BigDecimal.ZERO, BigDecimal::add));
        long thisMonth = actionExecutionRepository.sumLlmTokensSince(company.getId(), startOfMonth());

        return new LlmUsageDTO(start, end, totals, byModel, byFlow, company.getLlmMonthlyTokenLimit(), thisMonth);
    }

    @Transactional
    public LlmLimitDTO setMonthlyTokenLimit(Long monthlyTokenLimit) {
        Company company = currentCompany();
        company.setLlmMonthlyTokenLimit(monthlyTokenLimit);
        companyRepository.save(company);
        return new LlmLimitDTO(monthlyTokenLimit);
    }

    private Company currentCompany() {
        Long companyId = sessionUserService.getCurrentCompanyId();
        return companyRepository.findById(companyId).orElseThrow(() -> new NotFoundException("Empresa no encontrada"));
    }

    private static LocalDateTime startOfMonth() {
        return LocalDate.now().withDayOfMonth(1).atStartOfDay();
    }

    private static long toLong(Object value) {
        return value instanceof Number n ? n.longValue() : 0L;
    }

    private static BigDecimal toCost(Object value) {
        return value instanceof BigDecimal d ? d : BigDecimal.ZERO;
    }
}
