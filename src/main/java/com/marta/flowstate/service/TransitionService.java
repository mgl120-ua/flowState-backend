package com.marta.flowstate.service;

import com.marta.flowstate.model.Transition;
import com.marta.flowstate.repository.TransitionRepository;
import org.springframework.stereotype.Service;
import com.marta.flowstate.dto.TransitionDTO;
import com.marta.flowstate.model.State;
import com.marta.flowstate.model.Workflow;
import com.marta.flowstate.repository.StateRepository;
import com.marta.flowstate.repository.WorkflowRepository;
import com.marta.flowstate.exception.NotFoundException;

import java.util.List;

@Service
public class TransitionService {

    private final TransitionRepository transitionRepository;
    private final StateRepository stateRepository;
    private final WorkflowRepository workflowRepository;

    public TransitionService(TransitionRepository transitionRepository, StateRepository stateRepository, WorkflowRepository workflowRepository) {
        this.transitionRepository = transitionRepository;
        this.stateRepository = stateRepository;
        this.workflowRepository = workflowRepository;
    }

    public List<Transition> getTransitionsByWorkflowId(Long workflowId) {
        return transitionRepository.findByWorkflowId(workflowId);
    }

    public Transition getTransitionById(Long id) {
        return transitionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transicion :" + id + "no encontrado"));
    }

    public Transition createTransitionFromDTO(TransitionDTO dto, Long pathFlowId) {
        Workflow workflow = workflowRepository.findById(dto.getWorkflowId())
                .orElseThrow(() -> new NotFoundException("Flujo  :" + dto.getWorkflowId() + " no encontrado"));

        State source = stateRepository.findById(dto.getSourceStateId())
                .orElseThrow(() -> new NotFoundException("Estado origen : " + dto.getSourceStateId() + " no encontrado"));

        State target = stateRepository.findById(dto.getTargetStateId())
                .orElseThrow(() -> new NotFoundException("Estado destino  :" + dto.getTargetStateId() + " no encontrado"));

        Transition t = new Transition();
        t.setAction(dto.getAction());
        t.setCondition(dto.getCondition());
        t.setWorkflow(workflow);
        t.setSource_state(source);
        t.setTarget_state(target);

        return transitionRepository.save(t);
    }

    public void deleteTransition(Long id) {
        if (!transitionRepository.existsById(id)) {
            throw new RuntimeException("Error id:" + id + "no encontrado");
        }
        transitionRepository.deleteById(id);
    }

    public Transition updateTransitionFromDTO(Long id, TransitionDTO dto) {
        Transition transition = getTransitionById(id);

        Workflow workflow = workflowRepository.findById(dto.getWorkflowId())
                .orElseThrow(() -> new RuntimeException("Workflow id " + dto.getWorkflowId() + " no encontrado"));

        State source = stateRepository.findById(dto.getSourceStateId())
                .orElseThrow(() -> new RuntimeException("Estado origen id " + dto.getSourceStateId() + " no encontrado"));

        State target = stateRepository.findById(dto.getTargetStateId())
                .orElseThrow(() -> new RuntimeException("Estado destino id " + dto.getTargetStateId() + " no encontrado"));

        transition.setAction(dto.getAction());
        transition.setCondition(dto.getCondition());
        transition.setWorkflow(workflow);
        transition.setSource_state(source);
        transition.setTarget_state(target);

        return transitionRepository.save(transition);
    }
}
