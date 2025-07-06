package com.marta.flowstate.controller;

import com.marta.flowstate.model.State;
import com.marta.flowstate.service.StateService;
import com.marta.flowstate.dto.StateDTO;
import com.marta.flowstate.security.SessionUserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flows/{flowId}/states")
@SecurityRequirement(name = "bearerAuth")
public class StateController {

    private final StateService stateService;
    private final SessionUserService sessionUserService;

    public StateController(StateService stateService, SessionUserService sessionUserService) {
        this.stateService = stateService;
        this.sessionUserService = sessionUserService;
    }

    @GetMapping
    public List<State> getStatesByWorkflowId(@PathVariable Long flowId) {
        return stateService.getStatesByWorkflowId(flowId);
    }

    @PostMapping
    public State createState(@PathVariable Long flowId, @Valid @RequestBody StateDTO dto) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        return stateService.createState(flowId, dto, companyId);
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
    public State updateState(@PathVariable Long flowId, @PathVariable Long stateId, @RequestBody StateDTO dto) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        return stateService.updateState(stateId, dto, flowId, companyId);
    }
}
