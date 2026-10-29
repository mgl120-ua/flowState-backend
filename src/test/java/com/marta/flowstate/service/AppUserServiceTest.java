package com.marta.flowstate.service;

import com.marta.flowstate.dto.AppUserDTO;
import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.model.Company;
import com.marta.flowstate.model.Rol;
import com.marta.flowstate.repository.AppUserRepository;
import com.marta.flowstate.repository.CompanyRepository;
import com.marta.flowstate.repository.RolRepository;
import com.marta.flowstate.security.SessionUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private SessionUserService sessionUserService;

    @InjectMocks
    private AppUserService appUserService;

    private AppUser otherCompanyUser;

    @BeforeEach
    void setUp() {
        when(sessionUserService.getCurrentCompanyId()).thenReturn(1L);
        otherCompanyUser = user(20L, 2L);
    }

    @Test
    void getAllUsersOnlyReturnsUsersOfCurrentCompany() {
        List<AppUser> users = List.of(user(10L, 1L));
        when(appUserRepository.findByCompanyId(1L)).thenReturn(users);

        assertEquals(users, appUserService.getAllUsers());
        verify(appUserRepository, never()).findAll();
    }

    @Test
    void getUserByIdOfOtherCompanyReturnsNotFound() {
        when(appUserRepository.findById(20L)).thenReturn(Optional.of(otherCompanyUser));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> appUserService.getUserById(20L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getUsersByOtherCompanyIdIsForbidden() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> appUserService.getUsersByCompanyId(2L));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(appUserRepository, never()).findByCompanyId(2L);
    }

    @Test
    void deleteUserOfOtherCompanyDoesNotDelete() {
        when(appUserRepository.findById(20L)).thenReturn(Optional.of(otherCompanyUser));

        assertThrows(ResponseStatusException.class, () -> appUserService.deleteUser(20L));
        verify(appUserRepository, never()).delete(any());
        verify(appUserRepository, never()).deleteById(any());
    }

    @Test
    void updateUserCannotMoveUserToOtherCompany() {
        when(appUserRepository.findById(10L)).thenReturn(Optional.of(user(10L, 1L)));
        AppUserDTO dto = new AppUserDTO();
        dto.setName("Nuevo");
        dto.setCompanyId(2L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> appUserService.updateUser(10L, dto));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void updateUserRejectsRoleOfOtherCompany() {
        when(appUserRepository.findById(10L)).thenReturn(Optional.of(user(10L, 1L)));
        when(rolRepository.findByCompanyIdAndId(1L, 99L)).thenReturn(Optional.empty());
        AppUserDTO dto = new AppUserDTO();
        dto.setName("Nuevo");
        dto.setRoleId(99L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> appUserService.updateUser(10L, dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void updateUserWithRoleOfSameCompanySaves() {
        AppUser existing = user(10L, 1L);
        Rol rol = new Rol();
        rol.setId(5L);
        when(appUserRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(rolRepository.findByCompanyIdAndId(1L, 5L)).thenReturn(Optional.of(rol));
        when(appUserRepository.save(existing)).thenReturn(existing);
        AppUserDTO dto = new AppUserDTO();
        dto.setName("Nuevo");
        dto.setEmail("nuevo@example.com");
        dto.setRoleId(5L);

        AppUser result = appUserService.updateUser(10L, dto);

        assertEquals("Nuevo", result.getName());
        assertEquals(rol, result.getRol());
    }

    private AppUser user(Long id, Long companyId) {
        Company company = new Company();
        company.setId(companyId);
        AppUser user = new AppUser();
        user.setId(id);
        user.setCompany(company);
        return user;
    }
}
