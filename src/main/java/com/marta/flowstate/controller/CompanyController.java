package com.marta.flowstate.controller;

import com.marta.flowstate.dto.LlmLimitDTO;
import com.marta.flowstate.model.Company;
import com.marta.flowstate.service.CompanyService;
import com.marta.flowstate.service.LlmUsageService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/companies")
@SecurityRequirement(name = "bearerAuth")
public class CompanyController {

    private final CompanyService companyService;
    private final LlmUsageService llmUsageService;

    public CompanyController(CompanyService companyService, LlmUsageService llmUsageService) {
        this.companyService = companyService;
        this.llmUsageService = llmUsageService;
    }

    @GetMapping
    public List<Company> getAllCompanies() {
        return companyService.getAllCompanies();
    }

    @GetMapping("/{id}")
    public Company getCompanyById(@PathVariable Long id) {
        return companyService.getCompanyById(id);
    }

    //Las empresas se crean al registrar un administrador (/auth/register/admin)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
    }

    //Límite mensual de tokens LLM de la empresa del admin; monthlyTokenLimit null lo quita
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/me/llm-limit")
    public LlmLimitDTO setLlmLimit(@Valid @RequestBody LlmLimitDTO dto) {
        return llmUsageService.setMonthlyTokenLimit(dto.getMonthlyTokenLimit());
    }
}
