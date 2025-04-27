package com.marta.flowstate.controller;

import com.marta.flowstate.model.Instance;
import com.marta.flowstate.service.InstanceService;
import com.marta.flowstate.model.Instance_History;
import com.marta.flowstate.repository.Instance_HistoryRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flows/{flowId}/instances")
public class InstanceController {

    private final InstanceService instanceService;

    public InstanceController(InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    @GetMapping
    public List<Instance> getInstancesByWorkflowId(@PathVariable Long flowId) {
        return instanceService.getInstancesByWorkflowId(flowId);
    }

    @PostMapping
    public Instance createInstance(@RequestBody Instance instance) {
        return instanceService.createInstance(instance);
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
}
