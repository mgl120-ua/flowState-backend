package com.marta.flowstate.service;

import com.marta.flowstate.model.Company;
import com.marta.flowstate.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import com.marta.flowstate.exception.NotFoundException;
import com.marta.flowstate.dto.CompanyDTO;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Empresa: " + id + " no encontrada"));
    }
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company createCompany(CompanyDTO dto) {
        if (companyRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Ya existe una empresa con el nombre " + dto.getName());
        }
        Company company = new Company();
        company.setName(dto.getName());
        return companyRepository.save(company);
    }

    public void deleteCompany(Long id) {
        if (!companyRepository.existsById(id)) {
            throw new RuntimeException("Empresa: " + id + " no encontrada");
        }
        companyRepository.deleteById(id);
    }
}
