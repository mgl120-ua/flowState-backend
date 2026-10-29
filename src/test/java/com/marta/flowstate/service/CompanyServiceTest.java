package com.marta.flowstate.service;

import com.marta.flowstate.model.Company;
import com.marta.flowstate.repository.CompanyRepository;
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
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private SessionUserService sessionUserService;

    @InjectMocks
    private CompanyService companyService;

    @BeforeEach
    void setUp() {
        when(sessionUserService.getCurrentCompanyId()).thenReturn(1L);
    }

    @Test
    void getAllCompaniesOnlyReturnsCurrentCompany() {
        Company own = company(1L);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(own));

        assertEquals(List.of(own), companyService.getAllCompanies());
        verify(companyRepository, never()).findAll();
    }

    @Test
    void getOtherCompanyReturnsNotFound() {
        when(companyRepository.findById(2L)).thenReturn(Optional.of(company(2L)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> companyService.getCompanyById(2L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deleteOtherCompanyDoesNotDelete() {
        when(companyRepository.findById(2L)).thenReturn(Optional.of(company(2L)));

        assertThrows(ResponseStatusException.class, () -> companyService.deleteCompany(2L));
        verify(companyRepository, never()).delete(any());
    }

    private Company company(Long id) {
        Company company = new Company();
        company.setId(id);
        company.setName("Empresa " + id);
        return company;
    }
}
