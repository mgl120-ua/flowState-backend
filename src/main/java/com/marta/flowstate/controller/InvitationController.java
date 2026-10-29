package com.marta.flowstate.controller;

import com.marta.flowstate.dto.InvitationDTO;
import com.marta.flowstate.dto.InvitationResponseDTO;
import com.marta.flowstate.service.InvitationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invitations")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvitationResponseDTO createInvitation(@Valid @RequestBody InvitationDTO dto) {
        return InvitationResponseDTO.from(invitationService.createInvitation(dto));
    }

    @GetMapping
    public List<InvitationResponseDTO> getInvitations() {
        return invitationService.getInvitations().stream().map(InvitationResponseDTO::from).toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInvitation(@PathVariable Long id) {
        invitationService.deleteInvitation(id);
    }
}
