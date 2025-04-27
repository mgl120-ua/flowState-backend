package com.marta.flowstate.service;

import com.marta.flowstate.model.Instance;
import com.marta.flowstate.repository.InstanceRepository;
import com.marta.flowstate.model.Instance_History;
import com.marta.flowstate.repository.Instance_HistoryRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstanceService {

    private final InstanceRepository instanceRepository;
    private final Instance_HistoryRepository instanceHistoryRepository;

    public InstanceService(InstanceRepository instanceRepository, Instance_HistoryRepository instanceHistoryRepository) {
        this.instanceRepository = instanceRepository;
        this.instanceHistoryRepository = instanceHistoryRepository;
    }

    public List<Instance> getInstancesByWorkflowId(Long workflowId) {
        return instanceRepository.findByWorkflowId(workflowId);
    }

    public Instance getInstanceById(Long id) {
        return instanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error id:" + id + "no encontrado"));
    }

    public Instance createInstance(Instance instance) {
        return instanceRepository.save(instance);
    }

    public void deleteInstance(Long id) {
        if (!instanceRepository.existsById(id)) {
            throw new RuntimeException("Error id:" + id + "no encontrado");
        }
        instanceRepository.deleteById(id);
    }

    public List<Instance_History> getInstanceHistory(Long instanceId) {
        return instanceHistoryRepository.findByInstanceId(instanceId);
    }
}