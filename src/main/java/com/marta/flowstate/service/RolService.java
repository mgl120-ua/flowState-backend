package com.marta.flowstate.service;

import com.marta.flowstate.dto.RolDTO;
import com.marta.flowstate.exception.NotFoundException;
import com.marta.flowstate.model.Company;
import com.marta.flowstate.model.Rol;
import com.marta.flowstate.repository.CompanyRepository;
import com.marta.flowstate.repository.RolRepository;
import com.marta.flowstate.security.SessionUserService;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolService {

    private final RolRepository rolRepository;
    private final CompanyRepository companyRepository;
    private final SessionUserService sessionUserService;

    public RolService(RolRepository rolRepository,
                      CompanyRepository companyRepository,
                      SessionUserService sessionUserService) {
        this.rolRepository = rolRepository;
        this.companyRepository = companyRepository;
        this.sessionUserService = sessionUserService;
    }

    public List<Rol> getAllRoles() {
        Long companyId = sessionUserService.getCurrentCompanyId();
        return rolRepository.findByCompanyId(companyId);
    }

    public Rol getRolById(Long id) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        return rolRepository.findById(id)
                .filter(rol -> rol.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new NotFoundException("Rol " + id + " no encontrado o no pertenece a tu empresa"));
    }

    public Rol createRolFromDto(RolDTO dto) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Empresa no encontrada"));

        Rol rol = new Rol();
        rol.setName(dto.getName());
        rol.setDescription(dto.getDescription());
        rol.setCompany(company);

        return rolRepository.save(rol);
    }

    public Rol updateRolFromDto(Long id, RolDTO dto) {
        Rol existing = getRolById(id); //ya valida que es de la empresa

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        return rolRepository.save(existing);
    }

    public void deleteRol(Long id) {
        Rol rol = getRolById(id); //ya valida que es de la empresa
        rolRepository.delete(rol);
    }
}
