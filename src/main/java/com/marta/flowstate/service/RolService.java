package com.marta.flowstate.service;

import com.marta.flowstate.model.*;
import com.marta.flowstate.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RolService {

    private final RolRepository rolRepository;
    private final CompanyRepository companyRepository;

    public RolService(RolRepository rolRepository, CompanyRepository companyRepository) {
        this.rolRepository = rolRepository;
        this.companyRepository = companyRepository;
    }

    public List<Rol> getAllRoles() {
        return rolRepository.findAll();
    }

    public List<Rol> getRolesByCompanyId(Long companyId) {
        return rolRepository.findByCompanyId(companyId);
    }

    public Rol getRolById(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol not found with id: " + id));
    }

    public Rol createRol(Rol rol) {
        return rolRepository.save(rol);
    }

    public Rol updateRol(Long id, Rol updatedRol) {
        Rol existing = getRolById(id);
        existing.setName(updatedRol.getName());
        existing.setDescription(updatedRol.getDescription());
        existing.setCompany(updatedRol.getCompany());
        return rolRepository.save(existing);
    }

    public void deleteRol(Long id) {
        if (!rolRepository.existsById(id)) {
            throw new RuntimeException("Rol not found with id: " + id);
        }
        rolRepository.deleteById(id);
    }
}