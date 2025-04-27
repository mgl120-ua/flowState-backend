package com.marta.flowstate.controller;
import com.marta.flowstate.model.Workflow;
import com.marta.flowstate.service.WorkflowService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flows")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    public List<Workflow> getAllFlows() {
        return workflowService.getAllFlows();
    }

    @PostMapping
    public Workflow createFlow(@RequestBody Workflow workflow) {
        return workflowService.createFlow(workflow);
    }

    @GetMapping("/{flowId}")
    public Workflow getFlowById(@PathVariable Long flowId) {
        return workflowService.getFlowById(flowId);
    }

    @DeleteMapping("/{flowId}")
    public void deleteFlow(@PathVariable Long flowId) {
        workflowService.deleteFlow(flowId);
    }
}
