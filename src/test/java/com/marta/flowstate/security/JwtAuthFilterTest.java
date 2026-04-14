package com.marta.flowstate.security;

import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.model.Rol;
import com.marta.flowstate.repository.AppUserRepository;
import com.marta.flowstate.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AppUserRepository userRepo;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternalAllowsValidToken() throws Exception {
        String token = "valid-token";
        String email = "user@test.com";

        AppUser user = new AppUser();
        user.setEmail(email);
        Rol rol = new Rol();
        rol.setName("USER");
        user.setRol(rol);
        user.setPassword("encoded");

        when(request.getRequestURI()).thenReturn("/api/resource");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(user);
        when(jwtUtil.isTokenValid(token, email)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(email, SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilterInternalReturns401ForInvalidToken() throws Exception {
        String token = "invalid-token";

        when(request.getRequestURI()).thenReturn("/api/resource");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenThrow(new io.jsonwebtoken.JwtException("invalid token"));

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
        writer.flush();
        assertTrue(stringWriter.toString().contains("Invalid or expired JWT token"));
    }
}
