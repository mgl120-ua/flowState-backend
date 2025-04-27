package com.marta.flowstate.service;

import com.marta.flowstate.model.State;
import com.marta.flowstate.repository.StateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StateService {

    private final StateRepository stateRepository;

    public StateService(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    public List<State> getStatesByWorkflowId(Long workflowId) {
        return stateRepository.findByWorkflowId(workflowId);
    }

    public State getStateById(Long id) {
        return stateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error id:" + id + "no encontrado"));
    }

    public State createState(State state) {
        return stateRepository.save(state);
    }

    public void deleteState(Long id) {
        if (!stateRepository.existsById(id)) {
            throw new RuntimeException("Error id:" + id + "no encontrado");
        }
        stateRepository.deleteById(id);
    }
}