package com.marta.flowstate.service;

import com.marta.flowstate.model.Instance;
import com.marta.flowstate.repository.InstanceRepository;
import com.marta.flowstate.model.Instance_History;
import com.marta.flowstate.repository.Instance_HistoryRepository;
import com.marta.flowstate.repository.StateRepository;
import com.marta.flowstate.repository.WorkflowRepository;
import com.marta.flowstate.repository.AppUserRepository;
import com.marta.flowstate.model.State;
import com.marta.flowstate.model.Workflow;
import com.marta.flowstate.model.AppUser;
import org.springframework.stereotype.Service;
import com.marta.flowstate.dto.InstanceDTO;
import java.util.List;

@Service
public class InstanceService {

    private final InstanceRepository instanceRepository;
    private final Instance_HistoryRepository instanceHistoryRepository;
    private final StateRepository stateRepository;
    private final WorkflowRepository workflowRepository;
    private final AppUserRepository appUserRepository;

    public InstanceService(InstanceRepository instanceRepository, Instance_HistoryRepository instanceHistoryRepository, StateRepository stateRepository, WorkflowRepository workflowRepository, AppUserRepository appUserRepository) {
        this.instanceRepository = instanceRepository;
        this.instanceHistoryRepository = instanceHistoryRepository;
        this.stateRepository = stateRepository;
        this.workflowRepository = workflowRepository;
        this.appUserRepository = appUserRepository;
    }

    public List<Instance> getInstancesByWorkflowId(Long workflowId) {
        return instanceRepository.findByWorkflowId(workflowId);
    }

    public Instance getInstanceById(Long id) {
        return instanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Instancia:" + id + "no encontrada"));
    }

    public Instance createInstanceFromDTO(InstanceDTO dto) {
        State state = stateRepository.findById(dto.getStateId())
                .orElseThrow(() -> new RuntimeException("Estado: " + dto.getStateId() + " no encontrado"));
        Workflow workflow = workflowRepository.findById(dto.getWorkflowId())
                .orElseThrow(() -> new RuntimeException("Flujo: " + dto.getWorkflowId() + " no encontrado"));
        AppUser user = appUserRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario: " + dto.getUserId() + " no encontrado"));

        Instance instance = new Instance();
        instance.setData(dto.getData());
        instance.setState(state);
        instance.setWorkflow(workflow);
        instance.setUser(user);
        return instanceRepository.save(instance);
    }

    public void deleteInstance(Long id) {
        if (!instanceRepository.existsById(id)) {
            throw new RuntimeException("Instancia:" + id + "no encontrada");
        }
        instanceRepository.deleteById(id);
    }

    public List<Instance_History> getInstanceHistory(Long instanceId) {
        return instanceHistoryRepository.findByInstanceId(instanceId);
    }

    public Instance updateInstanceFromDTO(Long id, InstanceDTO dto) {
        Instance instance = getInstanceById(id);

        State state = stateRepository.findById(dto.getStateId())
                .orElseThrow(() -> new RuntimeException("Estado: " + dto.getStateId() + " no encontrado"));
        Workflow workflow = workflowRepository.findById(dto.getWorkflowId())
                .orElseThrow(() -> new RuntimeException("Flujo:" + dto.getWorkflowId() + " no encontrado"));
        AppUser user = appUserRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario: " + dto.getUserId() + " no encontrado"));

        instance.setData(dto.getData());
        instance.setState(state);
        instance.setWorkflow(workflow);
        instance.setUser(user);

        return instanceRepository.save(instance);
    }
}