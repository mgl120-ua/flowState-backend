package com.marta.flowstate.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    static class FailingController {
        @GetMapping("/not-found")
        void notFound() { throw new NotFoundException("Flujo no encontrado"); }

        @GetMapping("/bad-request")
        void badRequest() { throw new IllegalArgumentException("Dato no valido"); }

        @GetMapping("/forbidden")
        void forbidden() { throw new AccessDeniedException("Access Denied"); }

        @GetMapping("/conflict")
        void conflict() { throw new DataIntegrityViolationException("duplicate key"); }

        @GetMapping("/unauthorized")
        void unauthorized() { throw new UnauthorizedException("Credenciales incorrectas"); }

        @GetMapping("/status")
        void status() { throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no tiene permisos de ejecucion"); }

        @GetMapping("/unexpected")
        void unexpected() { throw new RuntimeException("detalle interno que no debe salir"); }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FailingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void notFoundExceptionReturns404WithMessage() throws Exception {
        mockMvc.perform(get("/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Flujo no encontrado"));
    }

    @Test
    void illegalArgumentReturns400() throws Exception {
        mockMvc.perform(get("/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Dato no valido"));
    }

    @Test
    void accessDeniedReturns403() throws Exception {
        mockMvc.perform(get("/forbidden")).andExpect(status().isForbidden());
    }

    @Test
    void dataIntegrityViolationReturns409() throws Exception {
        mockMvc.perform(get("/conflict")).andExpect(status().isConflict());
    }

    @Test
    void unauthorizedExceptionReturns401() throws Exception {
        mockMvc.perform(get("/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Credenciales incorrectas"));
    }

    @Test
    void responseStatusExceptionKeepsItsStatus() throws Exception {
        mockMvc.perform(get("/status"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Usuario no tiene permisos de ejecucion"));
    }

    @Test
    void unexpectedExceptionReturns500WithoutInternalDetails() throws Exception {
        mockMvc.perform(get("/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Error interno del servidor"));
    }
}
