package com.marta.flowstate.service;

import com.marta.flowstate.model.Instance;
import com.marta.flowstate.model.Instance_History;
import com.marta.flowstate.model.State;
import com.marta.flowstate.model.Workflow;
import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.model.StateType;

import com.marta.flowstate.repository.InstanceRepository;
import com.marta.flowstate.repository.Instance_HistoryRepository;
import com.marta.flowstate.repository.StateRepository;
import com.marta.flowstate.repository.WorkflowRepository;
import com.marta.flowstate.repository.AppUserRepository;
import com.marta.flowstate.repository.StateRepository;
import com.marta.flowstate.security.SessionUserService;
import com.marta.flowstate.dto.InstanceDTO;

import org.springframework.stereotype.Service;

import java.util.List;

import com.marta.flowstate.security.SessionUserService;

@Service
public class InstanceService {

    private final InstanceRepository instanceRepository;
    private final Instance_HistoryRepository instanceHistoryRepository;
    private final StateRepository stateRepository;
    private final WorkflowRepository workflowRepository;
    private final AppUserRepository appUserRepository;
    private final SessionUserService sessionUserService;

    public InstanceService(
            InstanceRepository instanceRepository,
            Instance_HistoryRepository instanceHistoryRepository,
            StateRepository stateRepository,
            WorkflowRepository workflowRepository,
            AppUserRepository appUserRepository,
            SessionUserService sessionUserService
    ) {
        this.instanceRepository = instanceRepository;
        this.instanceHistoryRepository = instanceHistoryRepository;
        this.stateRepository = stateRepository;
        this.workflowRepository = workflowRepository;
        this.appUserRepository = appUserRepository;
        this.sessionUserService = sessionUserService;
    }


    public List<Instance> getInstancesByWorkflowId(Long workflowId, Long companyId) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .filter(wf -> wf.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new RuntimeException("Flujo no encontrado o no pertenece a tu empresa"));
        return instanceRepository.findByWorkflowId(workflowId);
    }

    public Instance getInstanceById(Long id) {
        return instanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Instancia:" + id + " no encontrada"));
    }

    public Instance createInstance(Long flowId, InstanceDTO dto, Long companyId) {
        Workflow workflow = workflowRepository.findById(flowId)
                .filter(w -> w.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new RuntimeException("Flujo no encontrado o no pertenece a tu empresa"));
        System.out.println("flowId: " + flowId);
        System.out.println("companyId del token: " + companyId);
        workflowRepository.findById(flowId).ifPresent(w -> {
            System.out.println("companyId del flujo: " + w.getCompany().getId());
        });

        AppUser user = sessionUserService.getCurrentUser();
        if (user == null) throw new RuntimeException("Usuario no encontrado");

        // Encuentra el estado inicial automáticamente
        State initialState = stateRepository
                .findFirstByWorkflowIdAndType(flowId, StateType.INITIAL)
                .orElseThrow(() -> new RuntimeException("Estado inicial no encontrado"));

        Instance instance = new Instance();
        instance.setData(dto.getData());
        instance.setState(initialState);
        instance.setWorkflow(workflow);
        instance.setUser(user);

        return instanceRepository.save(instance);
    }

    public void deleteInstance(Long id, Long companyId) {
        Instance instance = getInstanceById(id);
        if (!instance.getWorkflow().getCompany().getId().equals(companyId)) {
            throw new RuntimeException("No tienes permisos para eliminar esta instancia");
        }
        instanceRepository.delete(instance);
    }

    public List<Instance_History> getInstanceHistory(Long instanceId, Long companyId) {
        Instance instance = getInstanceById(instanceId);
        if (!instance.getWorkflow().getCompany().getId().equals(companyId)) {
            throw new RuntimeException("No tienes acceso al historial de esta instancia");
        }
        return instanceHistoryRepository.findByInstanceId(instanceId);
    }

    public Instance updateInstance(Long id, InstanceDTO dto, Long flowId, Long companyId) {
        Instance instance = getInstanceById(id);

        if (!instance.getWorkflow().getCompany().getId().equals(companyId)) {
            throw new RuntimeException("No tienes permisos para modificar esta instancia");
        }

        Workflow workflow = workflowRepository.findById(flowId)
                .filter(wf -> wf.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new RuntimeException("Flujo no encontrado o no pertenece a tu empresa"));

        AppUser user = sessionUserService.getCurrentUser();

        instance.setData(dto.getData());
        instance.setWorkflow(workflow);
        instance.setUser(user);

        return instanceRepository.save(instance);
    }
}
