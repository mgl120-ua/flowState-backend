package com.marta.flowstate.controller;

import com.marta.flowstate.model.Instance;
import com.marta.flowstate.service.InstanceService;
import com.marta.flowstate.model.Instance_History;
import com.marta.flowstate.repository.Instance_HistoryRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.marta.flowstate.dto.InstanceDTO;
import com.marta.flowstate.repository.StateRepository;
import com.marta.flowstate.dto.InstanceDTO;
import com.marta.flowstate.service.TransitionExecutionService;
import java.util.List;
import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/flows/{flowId}/instances")
public class InstanceController {

    private final InstanceService instanceService;
    private final TransitionExecutionService transitionExecutionService;

    public InstanceController(InstanceService instanceService,
                              TransitionExecutionService transitionExecutionService) {
        this.instanceService = instanceService;
        this.transitionExecutionService = transitionExecutionService;
    }

    @GetMapping
    public List<Instance> getInstancesByWorkflowId(@PathVariable Long flowId) {
        return instanceService.getInstancesByWorkflowId(flowId);
    }

    @PostMapping
    public Instance createInstance(@Valid @RequestBody InstanceDTO dto) {
        return instanceService.createInstanceFromDTO(dto);
    }

    @GetMapping("/{instanceId}")
    public Instance getInstanceById(@PathVariable Long instanceId) {
        return instanceService.getInstanceById(instanceId);
    }

    @DeleteMapping("/{instanceId}")
    public void deleteInstance(@PathVariable Long instanceId) {
        instanceService.deleteInstance(instanceId);
    }

    @GetMapping("/{instanceId}/history")
    public List<Instance_History> getInstanceHistory(@PathVariable Long instanceId) {
        return instanceService.getInstanceHistory(instanceId);
    }

    @PutMapping("/{instanceId}")
    public Instance updateInstance(@PathVariable Long instanceId, @Valid @RequestBody InstanceDTO dto) {
        return instanceService.updateInstanceFromDTO(instanceId, dto);
    }

    @PostMapping("/{instanceId}/transitions/{transitionId}/execute")
    public ResponseEntity<Void> executeTransition(@PathVariable Long flowId,
                                                  @PathVariable Long instanceId,
                                                  @PathVariable Long transitionId,
                                                  @RequestHeader("userId") Long userId) {
        transitionExecutionService.executeTransition(instanceId, transitionId, userId);
        return ResponseEntity.ok().build();
    }
}
