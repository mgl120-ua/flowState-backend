package com.marta.flowstate.controller;
import com.marta.flowstate.service.PermissionService;
import com.marta.flowstate.dto.TransitionPermissionDTO;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/permissions")
@SecurityRequirement(name = "bearerAuth")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/grant")
    public ResponseEntity<?> grantPermission(@RequestBody TransitionPermissionDTO dto) {
        permissionService.grantPermission(dto.getTransitionId(), dto.getRolId());
        return ResponseEntity.ok("Permiso concedido");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/transition/{transitionId}/role/{rolId}")
    public ResponseEntity<?> removePermission(@PathVariable Long transitionId, @PathVariable Long rolId) {
        permissionService.revokePermission(transitionId, rolId);
        return ResponseEntity.ok("Permiso eliminado");
    }
}
