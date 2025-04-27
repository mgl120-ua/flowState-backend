package com.marta.flowstate.controller;

import com.marta.flowstate.model.Transition;
import com.marta.flowstate.service.TransitionService;
import org.springframework.web.bind.annotation.*;

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
    public Transition createTransition(@RequestBody Transition transition) {
        return transitionService.createTransition(transition);
    }

    @GetMapping("/{transitionId}")
    public Transition getTransitionById(@PathVariable Long transitionId) {
        return transitionService.getTransitionById(transitionId);
    }

    @DeleteMapping("/{transitionId}")
    public void deleteTransition(@PathVariable Long transitionId) {
        transitionService.deleteTransition(transitionId);
    }
}
