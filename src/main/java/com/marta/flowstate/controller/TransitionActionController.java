package com.marta.flowstate.controller;

import com.marta.flowstate.dto.TransitionActionDTO;
import com.marta.flowstate.dto.TransitionActionResponseDTO;
import com.marta.flowstate.service.ActionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flows/{flowId}/transitions/{transitionId}/actions")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class TransitionActionController {

    private final ActionService actionService;

    public TransitionActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    @GetMapping
    public List<TransitionActionResponseDTO> getTransitionActions(@PathVariable Long flowId, @PathVariable Long transitionId) {
        return actionService.getTransitionActions(flowId, transitionId).stream()
                .map(TransitionActionResponseDTO::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransitionActionResponseDTO attachAction(@PathVariable Long flowId, @PathVariable Long transitionId,
                                                    @Valid @RequestBody TransitionActionDTO dto) {
        return TransitionActionResponseDTO.from(actionService.attachAction(flowId, transitionId, dto));
    }

    @DeleteMapping("/{actionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void detachAction(@PathVariable Long flowId, @PathVariable Long transitionId, @PathVariable Long actionId) {
        actionService.detachAction(flowId, transitionId, actionId);
    }
}
