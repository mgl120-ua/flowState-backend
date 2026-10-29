package com.marta.flowstate.controller;

import com.marta.flowstate.dto.LlmUsageDTO;
import com.marta.flowstate.service.LlmUsageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/usage")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class LlmUsageController {

    private final LlmUsageService llmUsageService;

    public LlmUsageController(LlmUsageService llmUsageService) {
        this.llmUsageService = llmUsageService;
    }

    //Por defecto, el mes en curso hasta hoy
    @GetMapping("/llm")
    public LlmUsageDTO getLlmUsage(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return llmUsageService.getUsage(from, to);
    }
}
