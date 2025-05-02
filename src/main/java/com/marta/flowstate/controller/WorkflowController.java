package com.marta.flowstate.controller;
import com.marta.flowstate.model.Workflow;
import com.marta.flowstate.service.WorkflowService;
import org.springframework.web.bind.annotation.*;
import com.marta.flowstate.dto.WorkflowDTO;
import com.marta.flowstate.model.Company;
import com.marta.flowstate.service.CompanyService;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/flows")
public class WorkflowController {

    private final WorkflowService workflowService;
    private final CompanyService companyService;

    public WorkflowController(WorkflowService workflowService, CompanyService companyService) {
        this.workflowService = workflowService;
        this.companyService = companyService;
    }

    @GetMapping
    public List<Workflow> getAllFlows() {
        return workflowService.getAllFlows();
    }

    @PostMapping
    public Workflow createFlow(@RequestBody WorkflowDTO workflowDTO) {
        Company company = companyService.getCompanyById(workflowDTO.getCompanyId());

        Workflow workflow = new Workflow();
        workflow.setName(workflowDTO.getName());
        workflow.setCompany(company);
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

    @PutMapping("/{flowId}")
    public Workflow updateFlow(@PathVariable Long flowId, @RequestBody WorkflowDTO workflowDTO) {
        Company company = companyService.getCompanyById(workflowDTO.getCompanyId());
        return workflowService.updateFlow(flowId, workflowDTO.getName(), company);
    }
}
