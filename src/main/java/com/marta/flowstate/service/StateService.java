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
import com.marta.flowstate.security.SessionUserService;


@Service
public class StateService {

    private final StateRepository stateRepository;
    private final WorkflowRepository workflowRepository;
    private final SessionUserService sessionUserService;

    public StateService(StateRepository stateRepository,
                        WorkflowRepository workflowRepository,
                        SessionUserService sessionUserService) {
        this.stateRepository = stateRepository;
        this.workflowRepository = workflowRepository;
        this.sessionUserService = sessionUserService;
    }


    public List<State> getStatesByWorkflowId(Long workflowId) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        Workflow workflow = workflowRepository.findById(workflowId)
                .filter(wf -> wf.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new NotFoundException("Flujo no encontrado o no pertenece a tu empresa"));
        return stateRepository.findByWorkflowId(workflowId);
    }

    public State getStateById(Long id) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        return stateRepository.findByIdAndWorkflowCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("Estado: " + id + " no encontrado o no pertenece a tu empresa"));
    }

    public State createState(Long workflowId, StateDTO dto, Long companyId) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .filter(wf -> wf.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new NotFoundException("Flujo no encontrado o no pertenece a tu empresa"));

        if (dto.getType() == StateType.INITIAL &&
                stateRepository.existsByWorkflowIdAndType(workflowId, StateType.INITIAL)) {
            throw new IllegalArgumentException("Ya existe un estado inicial para este flujo");
        }

        State state = new State();
        state.setName(dto.getName());
        state.setType(dto.getType() != null ? dto.getType() : StateType.NORMAL);
        state.setWorkflow(workflow);

        return stateRepository.save(state);
    }

    public void deleteState(Long id) {
        State state = getStateById(id); // ya validamos empresa aquí
        stateRepository.delete(state);
    }


    public State updateState(Long stateId, StateDTO dto, Long workflowId, Long companyId) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .filter(wf -> wf.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new NotFoundException("Flujo no encontrado o no pertenece a tu empresa"));

        State state = getStateById(stateId);

        if (!state.getWorkflow().getId().equals(workflowId)) {
            throw new IllegalArgumentException("El estado no pertenece al flujo indicado");
        }

        if (dto.getType() == StateType.INITIAL &&
                state.getType() != StateType.INITIAL &&
                stateRepository.existsByWorkflowIdAndType(workflowId, StateType.INITIAL)) {
            throw new IllegalArgumentException("Ya existe un estado inicial para este flujo");
        }

        state.setName(dto.getName());
        state.setType(dto.getType() != null ? dto.getType() : StateType.NORMAL);
        state.setWorkflow(workflow);

        return stateRepository.save(state);
    }

}