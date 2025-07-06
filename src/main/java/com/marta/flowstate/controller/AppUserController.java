package com.marta.flowstate.controller;

import com.marta.flowstate.dto.AppUserDTO;
import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.service.AppUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "bearerAuth")
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping
    public List<AppUser> getAllUsers() {
        return appUserService.getAllUsers();
    }

    @GetMapping("/{id}")
    public AppUser getUserById(@PathVariable Long id) {
        return appUserService.getUserById(id);
    }

    @GetMapping("/company/{companyId}")
    public List<AppUser> getUsersByCompanyId(@PathVariable Long companyId) {
        return appUserService.getUsersByCompanyId(companyId);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        appUserService.deleteUser(id);
    }

    @PutMapping("/{id}")
    public AppUser updateUser(@PathVariable Long id, @RequestBody AppUserDTO dto) {
        return appUserService.updateUser(id, dto);
    }
}
