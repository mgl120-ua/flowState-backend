package com.marta.flowstate.controller;

import com.marta.flowstate.dto.AppUserDTO;
import com.marta.flowstate.dto.AppUserResponseDTO;
import com.marta.flowstate.service.AppUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public List<AppUserResponseDTO> getAllUsers() {
        return appUserService.getAllUsers().stream().map(AppUserResponseDTO::from).toList();
    }

    @GetMapping("/{id}")
    public AppUserResponseDTO getUserById(@PathVariable Long id) {
        return AppUserResponseDTO.from(appUserService.getUserById(id));
    }

    @GetMapping("/company/{companyId}")
    public List<AppUserResponseDTO> getUsersByCompanyId(@PathVariable Long companyId) {
        return appUserService.getUsersByCompanyId(companyId).stream().map(AppUserResponseDTO::from).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        appUserService.deleteUser(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public AppUserResponseDTO updateUser(@PathVariable Long id, @RequestBody AppUserDTO dto) {
        return AppUserResponseDTO.from(appUserService.updateUser(id, dto));
    }
}
