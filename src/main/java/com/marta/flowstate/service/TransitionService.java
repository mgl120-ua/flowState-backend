package com.marta.flowstate.service;

import org.springframework.stereotype.Service;
import com.marta.flowstate.dto.TransitionDTO;
import com.marta.flowstate.model.*;
import com.marta.flowstate.repository.*;
import com.marta.flowstate.exception.NotFoundException;
import com.marta.flowstate.action.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class TransitionService {

    private final TransitionRepository transitionRepository;
    private final StateRepository stateRepository;
    private final WorkflowRepository workflowRepository;
    private final Transition_ActionRepository transitionActionRepository;
    private final ActionExecutorFactory actionExecutorFactory;

    public TransitionService(TransitionRepository transitionRepository, StateRepository stateRepository, WorkflowRepository workflowRepository, Transition_ActionRepository transitionActionRepository, ActionExecutorFactory actionExecutorFactory) {
        this.transitionRepository = transitionRepository;
        this.stateRepository = stateRepository;
        this.workflowRepository = workflowRepository;
        this.transitionActionRepository = transitionActionRepository;
        this.actionExecutorFactory = actionExecutorFactory;
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
                .orElseThrow(() -> new RuntimeException("Workflow " + dto.getWorkflowId() + " no encontrado"));

        State source = stateRepository.findById(dto.getSourceStateId())
                .orElseThrow(() -> new RuntimeException("Estado origen " + dto.getSourceStateId() + " no encontrado"));

        State target = stateRepository.findById(dto.getTargetStateId())
                .orElseThrow(() -> new RuntimeException("Estado destino " + dto.getTargetStateId() + " no encontrado"));

        transition.setAction(dto.getAction());
        transition.setCondition(dto.getCondition());
        transition.setWorkflow(workflow);
        transition.setSource_state(source);
        transition.setTarget_state(target);

        return transitionRepository.save(transition);
    }

    public void executeActionsForTransition(Long transitionId) {
        List<TransitionAction> transitionActions = transitionActionRepository.findByTransitionId(transitionId);
        for (TransitionAction ta : transitionActions) {
            Action action = ta.getAction();
            ActionExecutor executor = actionExecutorFactory.getExecutor(action.getType());
            if (executor != null) {
                try {
                    executor.execute(action.getConfig());
                    System.out.println("Acticon ejecutada: " + action.getName() + " (" + action.getType() + ")");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("NO hay ejecutor para esta action: " + action.getName() + action.getType());
            }
        }
    }


}
