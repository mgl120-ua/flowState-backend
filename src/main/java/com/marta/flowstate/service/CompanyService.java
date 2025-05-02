package com.marta.flowstate.service;

import com.marta.flowstate.model.Company;
import com.marta.flowstate.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import com.marta.flowstate.exception.NotFoundException;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company con id " + id + " no encontrada"));
    }
}
