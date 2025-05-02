package com.marta.flowstate.controller;
import com.marta.flowstate.model.State;
import com.marta.flowstate.service.StateService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import com.marta.flowstate.dto.StateDTO;

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
    public State createState(@PathVariable Long flowId, @RequestBody StateDTO dto) {
        return stateService.createStateFromDTO(dto);
    }

    @GetMapping("/{stateId}")
    public State getStateById(@PathVariable Long stateId) {
        return stateService.getStateById(stateId);
    }

    @DeleteMapping("/{stateId}")
    public void deleteState(@PathVariable Long stateId) {
        stateService.deleteState(stateId);
    }

    @PutMapping("/{stateId}")
    public State updateState(@PathVariable Long stateId, @RequestBody StateDTO dto) {
        return stateService.updateStateFromDTO(stateId, dto);
    }
}
