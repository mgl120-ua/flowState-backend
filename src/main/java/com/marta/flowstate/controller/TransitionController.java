package com.marta.flowstate.controller;

import com.marta.flowstate.dto.TransitionDTO;
import com.marta.flowstate.model.Transition;
import com.marta.flowstate.security.SessionUserService;
import com.marta.flowstate.service.TransitionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequestMapping("/flows/{flowId}/transitions")
@SecurityRequirement(name = "bearerAuth")
public class TransitionController {

    private final TransitionService transitionService;
    private final SessionUserService sessionUserService;

    public TransitionController(TransitionService transitionService,
                                SessionUserService sessionUserService) {
        this.transitionService = transitionService;
        this.sessionUserService = sessionUserService;
    }

    @GetMapping
    public List<Transition> getTransitionsByWorkflowId(@PathVariable Long flowId) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        return transitionService.getTransitionsByWorkflowId(flowId, companyId);
    }

    @PostMapping
    public ResponseEntity<?> createTransition(@Valid @RequestBody TransitionDTO dto, BindingResult result, @PathVariable Long flowId) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        Long companyId = sessionUserService.getCurrentCompanyId();
        return ResponseEntity.ok(transitionService.createTransitionFromDTO(dto, flowId, companyId));
    }

    @GetMapping("/{transitionId}")
    public Transition getTransitionById(@PathVariable Long flowId,@PathVariable Long transitionId) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        return transitionService.getTransitionById(transitionId, companyId);
    }

    @PutMapping("/{transitionId}")
    public Transition updateTransition(@PathVariable Long flowId,@PathVariable Long transitionId,@RequestBody TransitionDTO dto) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        return transitionService.updateTransitionFromDTO(transitionId, dto, flowId, companyId);
    }

    @DeleteMapping("/{transitionId}")
    public void deleteTransition(@PathVariable Long flowId,@PathVariable Long transitionId) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        transitionService.deleteTransition(transitionId, companyId);
    }
}
