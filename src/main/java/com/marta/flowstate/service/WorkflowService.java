package com.marta.flowstate.service;

import com.marta.flowstate.exception.NotFoundException;
import com.marta.flowstate.model.Company;
import com.marta.flowstate.model.Workflow;
import com.marta.flowstate.repository.WorkflowRepository;
import com.marta.flowstate.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
public class WorkflowService {
    @Autowired
    private AppUserRepository userRepo;

    private final WorkflowRepository workflowRepository;

    public WorkflowService(WorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    public List<Workflow> getFlowsByCompanyId(Long companyId) {
        return workflowRepository.findByCompanyId(companyId);
    }

    public Workflow getFlowByIdAndCompany(Long id, Long companyId) {
        return workflowRepository.findById(id)
                .filter(wf -> wf.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new NotFoundException("Flujo: " + id + " no encontrado o no pertenece a tu empresa"));
    }

    public Workflow createFlow(String name) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser user = userRepo.findByEmail(email);

        if (user == null) {
            throw new NotFoundException("Usuario autenticado no encontrado");
        }

        Workflow workflow = new Workflow();
        workflow.setName(name);
        workflow.setCompany(user.getCompany());

        return workflowRepository.save(workflow);
    }

    public void deleteFlow(Long id, Long companyId) {
        Workflow wf = getFlowByIdAndCompany(id, companyId);
        workflowRepository.delete(wf);
    }

    public Workflow updateFlow(Long id, String name, Long companyId) {
        Workflow existingWorkflow = getFlowByIdAndCompany(id, companyId);
        existingWorkflow.setName(name);
        return workflowRepository.save(existingWorkflow);
    }

}
