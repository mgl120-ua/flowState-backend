package com.marta.flowstate.controller;

import com.marta.flowstate.model.*;
import com.marta.flowstate.service.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.marta.flowstate.dto.RolDTO;

@RestController
@RequestMapping("/roles")
@SecurityRequirement(name = "bearerAuth")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public List<Rol> getAllRoles() {
        return rolService.getAllRoles();
    }

    @GetMapping("/{id}")
    public Rol getRolById(@PathVariable Long id) {
        return rolService.getRolById(id);
    }

    @PostMapping
    public Rol createRol(@RequestBody RolDTO rolDTO) {return rolService.createRolFromDto(rolDTO);
    }

    @PutMapping("/{id}")
    public Rol updateRol(@PathVariable Long id, @RequestBody RolDTO dto) {
        return rolService.updateRolFromDto(id, dto);
    }


    @DeleteMapping("/{id}")
    public void deleteRol(@PathVariable Long id) {
        rolService.deleteRol(id);
    }
}