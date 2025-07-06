package com.marta.flowstate.controller;

import com.marta.flowstate.dto.WorkflowDTO;
import com.marta.flowstate.model.Workflow;
import com.marta.flowstate.security.SessionUserService;
import com.marta.flowstate.service.WorkflowService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flows")
@SecurityRequirement(name = "bearerAuth")
public class WorkflowController {

    private final WorkflowService workflowService;
    private final SessionUserService sessionUserService;

    public WorkflowController(WorkflowService workflowService,
                              SessionUserService sessionUserService) {
        this.workflowService = workflowService;
        this.sessionUserService = sessionUserService;
    }

    @GetMapping
    public List<Workflow> getAllFlows() {
        Long companyId = sessionUserService.getCurrentCompanyId();
        return workflowService.getFlowsByCompanyId(companyId);
    }

    @PostMapping
    public Workflow createFlow(@RequestBody @Valid WorkflowDTO workflowDTO) {
        return workflowService.createFlow(workflowDTO.getName());
    }

    @GetMapping("/{flowId}")
    public Workflow getFlowById(@PathVariable Long flowId) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        return workflowService.getFlowByIdAndCompany(flowId, companyId);
    }

    @DeleteMapping("/{flowId}")
    public void deleteFlow(@PathVariable Long flowId) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        workflowService.deleteFlow(flowId, companyId);
    }

    @PutMapping("/{flowId}")
    public Workflow updateFlow(@PathVariable Long flowId,
                               @RequestBody @Valid WorkflowDTO workflowDTO) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        return workflowService.updateFlow(flowId, workflowDTO.getName(), companyId);
    }
}
