package com.marta.flowstate.service;

import com.marta.flowstate.model.State;
import com.marta.flowstate.repository.StateRepository;
import com.marta.flowstate.repository.WorkflowRepository;
import org.springframework.stereotype.Service;
import com.marta.flowstate.dto.StateDTO;
import com.marta.flowstate.model.State;
import com.marta.flowstate.model.Workflow;
import com.marta.flowstate.exception.NotFoundException;
import com.marta.flowstate.model.StateType;
import java.util.List;


@Service
public class StateService {

    private final StateRepository stateRepository;
    private final WorkflowRepository workflowRepository;

    public StateService(StateRepository stateRepository, WorkflowRepository workflowRepository) {
        this.stateRepository = stateRepository;
        this.workflowRepository = workflowRepository;
    }

    public List<State> getStatesByWorkflowId(Long workflowId) {
        return stateRepository.findByWorkflowId(workflowId);
    }

    public State getStateById(Long id) {
        return stateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Estado :" + id + "no encontrado"));
    }

    public State createStateFromDTO(StateDTO dto) {
        Workflow workflow = workflowRepository.findById(dto.getWorkflowId())
                .orElseThrow(() -> new NotFoundException("Flujo:" + dto.getWorkflowId() + " no encontrado"));

        if (dto.getType() == StateType.INITIAL && stateRepository.existsByWorkflowIdAndType(dto.getWorkflowId(), StateType.INITIAL)) {
            throw new IllegalArgumentException("Ya existe un estado inicial para este flujo");
        }

        State state = new State();
        state.setName(dto.getName());
        if(dto.getType() != null) {
            state.setType(dto.getType());
        }else{
            state.setType(StateType.NORMAL);
        }
        state.setWorkflow(workflow);
        return stateRepository.save(state);
    }

    public void deleteState(Long id) {
        if (!stateRepository.existsById(id)) {
            throw new NotFoundException("Estado :" + id + "no encontrado");
        }
        stateRepository.deleteById(id);
    }

    public State updateStateFromDTO(Long id, StateDTO dto) {
        State state = getStateById(id);

        Workflow workflow = workflowRepository.findById(dto.getWorkflowId())
                .orElseThrow(() -> new RuntimeException("Flujo: " + dto.getWorkflowId() + " no encontrado"));

        if (dto.getType() == StateType.INITIAL && state.getType() != StateType.INITIAL && stateRepository.existsByWorkflowIdAndType(dto.getWorkflowId(), StateType.INITIAL)) {
            throw new IllegalArgumentException("Ya existe un estado inicial para este flujo");
        }

        state.setName(dto.getName());
        if(dto.getType() != null) {
            state.setType(dto.getType());
        }else{
            state.setType(StateType.NORMAL);
        }
        state.setWorkflow(workflow);

        return stateRepository.save(state);
    }
}