package com.marta.flowstate.service;

import com.marta.flowstate.dto.AppUserDTO;
import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.model.Company;
import com.marta.flowstate.model.Rol;
import com.marta.flowstate.repository.AppUserRepository;
import com.marta.flowstate.repository.CompanyRepository;
import com.marta.flowstate.repository.RolRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final CompanyRepository companyRepository;
    private final RolRepository rolRepository;

    public AppUserService(AppUserRepository appUserRepository, CompanyRepository companyRepository, RolRepository rolRepository) {
        this.appUserRepository = appUserRepository;
        this.companyRepository = companyRepository;
        this.rolRepository = rolRepository;
    }

    public List<AppUser> getAllUsers() {
        return appUserRepository.findAll();
    }

    public AppUser getUserById(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public List<AppUser> getUsersByCompanyId(Long companyId) {
        return appUserRepository.findByCompanyId(companyId);
    }

    public AppUser createUser(AppUserDTO dto) {
        if (appUserRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered: " + dto.getEmail());
        }

        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + dto.getCompanyId()));

        Rol rol = rolRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + dto.getRoleId()));

        AppUser user = new AppUser();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setCompany(company);
        user.setRol(rol);

        return appUserRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!appUserRepository.existsById(id)) {
            throw new RuntimeException("Usuario : " + id + "no encontrado");
        }
        appUserRepository.deleteById(id);
    }

    public AppUser updateUser(Long id, AppUserDTO dto) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario : " + id + "no encontrado"));

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        if (dto.getCompanyId() != null) {
            Company company = companyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Empresa : " + dto.getCompanyId() + "no encontrada"));
            user.setCompany(company);
        }

        if (dto.getRoleId() != null) {
            Rol rol = rolRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role : " + dto.getRoleId() + "no encontrado"));
            user.setRol(rol);
        }

        return appUserRepository.save(user);
    }

}
