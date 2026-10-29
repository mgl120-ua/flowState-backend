package com.marta.flowstate.service;

import com.marta.flowstate.dto.LoginDTO;
import com.marta.flowstate.dto.RegisterEmployeeDTO;
import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.model.Company;
import com.marta.flowstate.model.Invitation;
import com.marta.flowstate.model.Rol;
import com.marta.flowstate.repository.AppUserRepository;
import com.marta.flowstate.repository.CompanyRepository;
import com.marta.flowstate.repository.InvitationRepository;
import com.marta.flowstate.repository.RolRepository;
import com.marta.flowstate.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
    @Mock
    private InvitationRepository invitationRepo;

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

    @Test
    void registerEmployeeWithValidInvitationUsesItsCompanyAndRole() {
        Invitation invitation = invitation(null, LocalDateTime.now().plusDays(1), null);
        when(invitationRepo.findByCode("codigo")).thenReturn(Optional.of(invitation));
        when(userRepo.existsByEmail("emp@example.com")).thenReturn(false);
        when(invitationRepo.markAsUsed(eq(invitation.getId()), any(LocalDateTime.class))).thenReturn(1);
        when(passwordEncoder.encode("secreta")).thenReturn("hash");
        when(userRepo.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        AppUser user = authService.registerEmployee(employeeDto("emp@example.com"));

        assertEquals(invitation.getCompany(), user.getCompany());
        assertEquals(invitation.getRol(), user.getRol());
        assertEquals("hash", user.getPassword());
    }

    @Test
    void registerEmployeeWithUnknownCodeFails() {
        when(invitationRepo.findByCode("codigo")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.registerEmployee(employeeDto("emp@example.com")));
        verify(userRepo, never()).save(any());
    }

    @Test
    void registerEmployeeWithExpiredInvitationFails() {
        when(invitationRepo.findByCode("codigo")).thenReturn(Optional.of(invitation(null, LocalDateTime.now().minusMinutes(1), null)));

        assertThrows(IllegalArgumentException.class, () -> authService.registerEmployee(employeeDto("emp@example.com")));
        verify(userRepo, never()).save(any());
    }

    @Test
    void registerEmployeeWithUsedInvitationFails() {
        when(invitationRepo.findByCode("codigo")).thenReturn(Optional.of(invitation(null, LocalDateTime.now().plusDays(1), LocalDateTime.now())));

        assertThrows(IllegalArgumentException.class, () -> authService.registerEmployee(employeeDto("emp@example.com")));
        verify(userRepo, never()).save(any());
    }

    @Test
    void registerEmployeeWithOtherEmailThanInvitedFails() {
        when(invitationRepo.findByCode("codigo")).thenReturn(Optional.of(invitation("invitado@example.com", LocalDateTime.now().plusDays(1), null)));

        assertThrows(IllegalArgumentException.class, () -> authService.registerEmployee(employeeDto("otro@example.com")));
        verify(userRepo, never()).save(any());
    }

    @Test
    void registerEmployeeFailsWhenInvitationIsUsedConcurrently() {
        Invitation invitation = invitation(null, LocalDateTime.now().plusDays(1), null);
        when(invitationRepo.findByCode("codigo")).thenReturn(Optional.of(invitation));
        when(userRepo.existsByEmail("emp@example.com")).thenReturn(false);
        when(invitationRepo.markAsUsed(anyLong(), any(LocalDateTime.class))).thenReturn(0);

        assertThrows(IllegalArgumentException.class, () -> authService.registerEmployee(employeeDto("emp@example.com")));
        verify(userRepo, never()).save(any());
    }

    private RegisterEmployeeDTO employeeDto(String email) {
        RegisterEmployeeDTO dto = new RegisterEmployeeDTO();
        dto.setName("Empleado");
        dto.setEmail(email);
        dto.setPassword("secreta");
        dto.setInvitationCode("codigo");
        return dto;
    }

    private Invitation invitation(String email, LocalDateTime expiresAt, LocalDateTime usedAt) {
        Company company = new Company(1L, "Empresa");
        Rol rol = new Rol(3L, "EMPLEADO", null, company);
        return new Invitation(7L, "codigo", email, company, rol, LocalDateTime.now().minusDays(1), expiresAt, usedAt);
    }
}
