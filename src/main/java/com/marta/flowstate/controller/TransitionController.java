package com.marta.flowstate.controller;

import com.marta.flowstate.model.Transition;
import com.marta.flowstate.service.TransitionService;
import org.springframework.web.bind.annotation.*;
import com.marta.flowstate.dto.TransitionDTO;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/flows/{flowId}/transitions")
public class TransitionController {

    private final TransitionService transitionService;

    public TransitionController(TransitionService transitionService) {
        this.transitionService = transitionService;
    }

    @GetMapping
    public List<Transition> getTransitionsByWorkflowId(@PathVariable Long flowId) {
        return transitionService.getTransitionsByWorkflowId(flowId);
    }

    @PostMapping
    public Transition createTransition(@RequestBody TransitionDTO dto, @PathVariable Long flowId) {
        return transitionService.createTransitionFromDTO(dto, flowId);
    }

    @GetMapping("/{transitionId}")
    public Transition getTransitionById(@PathVariable Long transitionId) {
        return transitionService.getTransitionById(transitionId);
    }

    @DeleteMapping("/{transitionId}")
    public void deleteTransition(@PathVariable Long transitionId) {
        transitionService.deleteTransition(transitionId);
    }

    @PutMapping("/{transitionId}")
    public Transition updateTransition(@PathVariable Long transitionId, @RequestBody TransitionDTO dto) {
        return transitionService.updateTransitionFromDTO(transitionId, dto);
    }
}
