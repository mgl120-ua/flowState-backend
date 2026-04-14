package com.marta.flowstate.service;

import com.marta.flowstate.dto.LoginDTO;
import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.model.Company;
import com.marta.flowstate.model.Rol;
import com.marta.flowstate.repository.AppUserRepository;
import com.marta.flowstate.repository.CompanyRepository;
import com.marta.flowstate.repository.RolRepository;
import com.marta.flowstate.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CompanyRepository companyRepo;
    @Mock
    private AppUserRepository userRepo;
    @Mock
    private RolRepository rolRepo;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginReturnsTokenAndUserInfoWhenCredentialsValid() {
        AppUser user = new AppUser();
        user.setId(10L);
        user.setName("Tester");
        user.setEmail("user@test.com");
        Company company = new Company();
        company.setId(5L);
        user.setCompany(company);
        Rol rol = new Rol();
        rol.setName("USER");
        user.setRol(rol);
        user.setPassword("encodedPassword");

        LoginDTO dto = new LoginDTO();
        dto.setEmail("user@test.com");
        dto.setPassword("secret");

        when(userRepo.findByEmail("user@test.com")).thenReturn(user);
        when(passwordEncoder.matches("secret", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("token-123");

        Map<String, Object> response = authService.login(dto);

        assertNotNull(response);
        assertEquals("token-123", response.get("token"));

        assertTrue(response.get("user") instanceof Map);
        Map<?, ?> userMap = (Map<?, ?>) response.get("user");
        assertEquals(10L, userMap.get("id"));
        assertEquals("Tester", userMap.get("name"));
        assertEquals("user@test.com", userMap.get("email"));
        assertEquals(5L, userMap.get("companyId"));
        assertEquals("USER", userMap.get("role"));
    }

    @Test
    void loginThrowsWhenCredentialsInvalid() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("user@test.com");
        dto.setPassword("wrong");

        when(userRepo.findByEmail("user@test.com")).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(dto));
        assertEquals("Credenciales incorrectas", exception.getMessage());
    }
}
