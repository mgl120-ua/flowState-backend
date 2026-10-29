package com.marta.flowstate.service;

import com.marta.flowstate.model.Company;
import com.marta.flowstate.repository.CompanyRepository;
import com.marta.flowstate.security.SessionUserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final SessionUserService sessionUserService;

    public CompanyService(CompanyRepository companyRepository, SessionUserService sessionUserService) {
        this.companyRepository = companyRepository;
        this.sessionUserService = sessionUserService;
    }

    //Solo se puede consultar la empresa del usuario autenticado
    public Company getCompanyById(Long id) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        return companyRepository.findById(id)
                .filter(company -> company.getId().equals(companyId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa: " + id + " no encontrada"));
    }

    public List<Company> getAllCompanies() {
        return List.of(getCompanyById(sessionUserService.getCurrentCompanyId()));
    }

    public void deleteCompany(Long id) {
        Company company = getCompanyById(id); //ya valida que es la empresa del usuario
        companyRepository.delete(company);
    }
}
