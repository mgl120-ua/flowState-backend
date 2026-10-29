package com.marta.flowstate.service;

import com.marta.flowstate.dto.AppUserDTO;
import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.model.Company;
import com.marta.flowstate.model.Rol;
import com.marta.flowstate.repository.AppUserRepository;
import com.marta.flowstate.repository.CompanyRepository;
import com.marta.flowstate.repository.RolRepository;
import com.marta.flowstate.security.SessionUserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final CompanyRepository companyRepository;
    private final RolRepository rolRepository;
    private final SessionUserService sessionUserService;

    public AppUserService(AppUserRepository appUserRepository, CompanyRepository companyRepository, RolRepository rolRepository, SessionUserService sessionUserService) {
        this.appUserRepository = appUserRepository;
        this.companyRepository = companyRepository;
        this.rolRepository = rolRepository;
        this.sessionUserService = sessionUserService;
    }

    //Solo usuarios de la empresa del usuario autenticado
    public List<AppUser> getAllUsers() {
        Long companyId = sessionUserService.getCurrentCompanyId();
        return appUserRepository.findByCompanyId(companyId);
    }

    public AppUser getUserById(Long id) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        return appUserRepository.findById(id)
                .filter(user -> user.getCompany() != null && user.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario " + id + " no encontrado"));
    }

    public List<AppUser> getUsersByCompanyId(Long companyId) {
        if (!companyId.equals(sessionUserService.getCurrentCompanyId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a esta empresa");
        }
        return appUserRepository.findByCompanyId(companyId);
    }

    public AppUser createUser(AppUserDTO dto) {
        if (appUserRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email " + dto.getEmail() + "ya registrado");
        }
        Company company = companyRepository.findById(dto.getCompanyId()).orElseThrow(() -> new RuntimeException("Empresa " + dto.getCompanyId() + "no encontrada"));
        Rol rol = rolRepository.findById(dto.getRoleId()).orElseThrow(() -> new RuntimeException("Role " + dto.getRoleId() + "no encontrado"));
        AppUser user = new AppUser();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setCompany(company);
        user.setRol(rol);
        return appUserRepository.save(user);
    }

    public void deleteUser(Long id) {
        AppUser user = getUserById(id); //ya valida que es de la empresa
        appUserRepository.delete(user);
    }

    public AppUser updateUser(Long id, AppUserDTO dto) {
        AppUser user = getUserById(id); //ya valida que es de la empresa
        Long companyId = user.getCompany().getId();
        //No se permite mover un usuario a otra empresa
        if (dto.getCompanyId() != null && !dto.getCompanyId().equals(companyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede cambiar la empresa de un usuario");
        }
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        if (dto.getRoleId() != null) {
            Rol rol = rolRepository.findByCompanyIdAndId(companyId, dto.getRoleId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol " + dto.getRoleId() + " no encontrado o no pertenece a la empresa"));
            user.setRol(rol);
        }
        return appUserRepository.save(user);
    }

    public AppUser registerUser(AppUserDTO dto) {
        if (appUserRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email " + dto.getEmail() + "ya registrado");
        }
        boolean hasCompanyName = dto.getCompanyName() != null && !dto.getCompanyName().isBlank();
        boolean hasCompanyId = dto.getCompanyId() != null;
        if (!hasCompanyName && !hasCompanyId) {
            throw new RuntimeException("Falta nombre o id de empresa");
        }
        boolean isAdmin = hasCompanyName;
        Company company;
        Rol rol;
        if (isAdmin) {
            company = new Company();
            company.setName(dto.getCompanyName());
            company = companyRepository.save(company);
            // Guardamos como final para usarlo en la lambda
            Company finalCompany = company;
            rol = rolRepository.findByCompanyIdAndName(finalCompany.getId(), "ADMIN")
                    .orElseGet(() -> {
                        Rol newRol = new Rol();
                        newRol.setName("ADMIN");
                        newRol.setDescription("Administrador empresa");
                        newRol.setCompany(finalCompany);
                        return rolRepository.save(newRol);
                    });
        } else {
            company = companyRepository.findById(dto.getCompanyId()).orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

            rol = null;
            if (dto.getRoleId() != null) {
                rol = rolRepository.findById(dto.getRoleId()).orElseThrow(() -> new RuntimeException("Rol no encontrado"));
            }
        }
        AppUser user = new AppUser();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setCompany(company);
        user.setRol(rol);
        return appUserRepository.save(user);
    }
    public AppUser registerAdminUser(AppUserDTO dto) {
        if (appUserRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email ya registrado");
        }

        // Crear empresa
        Company company = new Company();
        company.setName(dto.getCompanyName());
        company = companyRepository.save(company);

        // Crear rol ADMIN
        Rol adminRol = new Rol();
        adminRol.setName("ADMIN");
        adminRol.setDescription("Administrador empresa");
        adminRol.setCompany(company);
        adminRol = rolRepository.save(adminRol);

        // Crear usuario con rol ADMIN
        AppUser user = new AppUser();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setCompany(company);
        user.setRol(adminRol);

        return appUserRepository.save(user);
    }

    public AppUser registerEmployeeUser(AppUserDTO dto) {
        if (appUserRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email ya registrado");
        }

        Company company = companyRepository.findById(dto.getCompanyId()).orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        Rol rol = null;
        if (dto.getRoleId() != null) {
            rol = rolRepository.findById(dto.getRoleId()).orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        }

        AppUser user = new AppUser();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setCompany(company);
        user.setRol(rol);

        return appUserRepository.save(user);
    }

    public AppUser getUserByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }
    public Map<String, Object> login(String email) {
        AppUser user = appUserRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("name", user.getName());
        response.put("companyId", user.getCompany().getId());
        response.put("role", user.getRol() != null ? user.getRol().getName() : null);

        return response;
    }

}
