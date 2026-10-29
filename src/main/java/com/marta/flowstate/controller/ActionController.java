package com.marta.flowstate.controller;

import com.marta.flowstate.dto.ActionDTO;
import com.marta.flowstate.dto.ActionResponseDTO;
import com.marta.flowstate.service.ActionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/actions")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class ActionController {

    private final ActionService actionService;

    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    @GetMapping
    public List<ActionResponseDTO> getActions() {
        return actionService.getActions();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActionResponseDTO createAction(@Valid @RequestBody ActionDTO dto) {
        return actionService.createAction(dto);
    }

    @GetMapping("/{id}")
    public ActionResponseDTO getAction(@PathVariable Long id) {
        return actionService.getAction(id);
    }

    @PutMapping("/{id}")
    public ActionResponseDTO updateAction(@PathVariable Long id, @Valid @RequestBody ActionDTO dto) {
        return actionService.updateAction(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAction(@PathVariable Long id) {
        actionService.deleteAction(id);
    }
}
