package com.marta.flowstate.controller;

import com.marta.flowstate.dto.*;
import com.marta.flowstate.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.repository.AppUserRepository;
import java.util.Map;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private AppUserRepository userRepo;

    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterAdminDTO dto) {
        return ResponseEntity.ok(authService.registerAdmin(dto));
    }

    @PostMapping("/register/employee")
    public ResponseEntity<?> registerEmployee(@RequestBody RegisterEmployeeDTO dto) {
        return ResponseEntity.ok(authService.registerEmployee(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

}
