package com.marta.flowstate.controller;
import com.marta.flowstate.model.State;
import com.marta.flowstate.service.StateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flows/{flowId}/states")
public class StateController {

    private final StateService stateService;

    public StateController(StateService stateService) {
        this.stateService = stateService;
    }

    @GetMapping
    public List<State> getStatesByWorkflowId(@PathVariable Long flowId) {
        return stateService.getStatesByWorkflowId(flowId);
    }

    @PostMapping
    public State createState(@RequestBody State state) {
        return stateService.createState(state);
    }

    @GetMapping("/{stateId}")
    public State getStateById(@PathVariable Long stateId) {
        return stateService.getStateById(stateId);
    }

    @DeleteMapping("/{stateId}")
    public void deleteState(@PathVariable Long stateId) {
        stateService.deleteState(stateId);
    }
}
