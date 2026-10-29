package com.marta.flowstate.service;

import com.marta.flowstate.dto.*;
import com.marta.flowstate.model.*;
import com.marta.flowstate.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.marta.flowstate.exception.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.marta.flowstate.util.JwtUtil;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@Service
public class AuthService {

    @Autowired private CompanyRepository companyRepo;
    @Autowired private AppUserRepository userRepo;
    @Autowired private RolRepository rolRepo;
    @Autowired private InvitationRepository invitationRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    public AppUser registerAdmin(RegisterAdminDTO dto) {
        Company company = new Company();
        company.setName(dto.companyName);
        companyRepo.save(company);
        Rol rol = new Rol();
        rol.setName("ADMIN");
        rol.setCompany(company);
        rolRepo.save(rol);
        AppUser user = new AppUser();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setCompany(company);
        user.setRol(rol);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        return userRepo.save(user);
    }

    //El empleado solo puede registrarse con una invitación válida creada por un ADMIN de la empresa
    @Transactional
    public AppUser registerEmployee(RegisterEmployeeDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        //Mismo mensaje en todos los casos para no dar pistas sobre qué códigos existen
        Invitation invitation = invitationRepo.findByCode(dto.getInvitationCode())
                .filter(i -> i.getUsedAt() == null && i.getExpiresAt().isAfter(now))
                .filter(i -> i.getEmail() == null || i.getEmail().equalsIgnoreCase(dto.getEmail()))
                .orElseThrow(() -> new IllegalArgumentException("Invitación no válida o caducada"));
        if (userRepo.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email ya registrado");
        }
        if (invitationRepo.markAsUsed(invitation.getId(), now) == 0) {
            throw new IllegalArgumentException("Invitación no válida o caducada");
        }
        AppUser user = new AppUser();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setCompany(invitation.getCompany());
        user.setRol(invitation.getRol());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        return userRepo.save(user);
    }

    public Map<String, Object> login(LoginDTO dto) {
        AppUser user = userRepo.findByEmail(dto.getEmail());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Credenciales incorrectas");
        }

        String token = jwtUtil.generateToken(user);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "companyId", user.getCompany().getId(),
                "role", user.getRol().getName()
        ));

        return response;
    }
}
