package com.marta.flowstate.service;

import com.marta.flowstate.model.Workflow;
import com.marta.flowstate.repository.WorkflowRepository;
import org.springframework.stereotype.Service;
import com.marta.flowstate.model.Company;
import com.marta.flowstate.service.CompanyService;
import com.marta.flowstate.exception.NotFoundException;

import java.util.List;

@Service
public class WorkflowService {

    private final WorkflowRepository workflowRepository;

    public WorkflowService(WorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    public List<Workflow> getAllFlows() {
        return workflowRepository.findAll();
    }

    public Workflow getFlowById(Long id) {
        return workflowRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Flujo :" + id + "no encontrado"));
    }

    public Workflow createFlow(Workflow workflow) {
        return workflowRepository.save(workflow);
    }

    public void deleteFlow(Long id) {
        if (!workflowRepository.existsById(id)) {
            throw new RuntimeException("Flujo :" + id + "no encontrado");
        }
        workflowRepository.deleteById(id);
    }

    public Workflow updateFlow(Long id, String name, Company company) {
        Workflow existingWorkflow = getFlowById(id);

        existingWorkflow.setName(name);
        existingWorkflow.setCompany(company);

        return workflowRepository.save(existingWorkflow);
    }

}