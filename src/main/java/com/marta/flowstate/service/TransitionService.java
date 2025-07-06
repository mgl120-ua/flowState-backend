package com.marta.flowstate.service;

import org.springframework.stereotype.Service;
import com.marta.flowstate.dto.TransitionDTO;
import com.marta.flowstate.model.*;
import com.marta.flowstate.repository.*;
import com.marta.flowstate.exception.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class TransitionService {

    private final TransitionRepository transitionRepository;
    private final StateRepository stateRepository;
    private final WorkflowRepository workflowRepository;
    private final TransitionActionRepository transitionActionRepository;

    public TransitionService(TransitionRepository transitionRepository, StateRepository stateRepository, WorkflowRepository workflowRepository, TransitionActionRepository transitionActionRepository) {
        this.transitionRepository = transitionRepository;
        this.stateRepository = stateRepository;
        this.workflowRepository = workflowRepository;
        this.transitionActionRepository = transitionActionRepository;
    }

    public Transition getTransitionById(Long id, Long companyId) {
        return transitionRepository.findById(id)
                .filter(t -> t.getWorkflow().getCompany().getId().equals(companyId))
                .orElseThrow(() -> new NotFoundException("Transición no encontrada o no pertenece a tu empresa"));
    }

    public List<Transition> getTransitionsByWorkflowId(Long workflowId, Long companyId) {
        Workflow wf = workflowRepository.findById(workflowId)
                .filter(w -> w.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new NotFoundException("Flujo " + workflowId + " no encontrado o no pertenece a tu empresa"));

        return transitionRepository.findByWorkflowId(wf.getId());
    }

    public Transition createTransitionFromDTO(TransitionDTO dto, Long pathFlowId, Long companyId) {
        Workflow workflow = workflowRepository.findById(pathFlowId)
                .filter(wf -> wf.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new NotFoundException("Flujo no encontrado o no pertenece a tu empresa"));

        State source = stateRepository.findById(dto.getSourceStateId())
                .filter(s -> s.getWorkflow().getId().equals(pathFlowId))
                .orElseThrow(() -> new IllegalArgumentException("El estado origen no pertenece al flujo"));

        State target = stateRepository.findById(dto.getTargetStateId())
                .filter(s -> s.getWorkflow().getId().equals(pathFlowId))
                .orElseThrow(() -> new IllegalArgumentException("El estado destino no pertenece al flujo"));

        Transition t = new Transition();
        t.setAction(dto.getAction());
        t.setCondition(dto.getCondition());
        t.setWorkflow(workflow);
        t.setSource_state(source);
        t.setTarget_state(target);

        return transitionRepository.save(t);
    }



    public void deleteTransition(Long id, Long companyId) {
        Transition t = getTransitionById(id, companyId);
        transitionRepository.delete(t);
    }

    public Transition updateTransitionFromDTO(Long id, TransitionDTO dto, Long pathFlowId, Long companyId) {
        Transition transition = getTransitionById(id, companyId);

        if (!transition.getWorkflow().getId().equals(pathFlowId)) {
            throw new IllegalArgumentException("La transición no pertenece al flujo indicado");
        }

        Workflow workflow = workflowRepository.findById(pathFlowId)
                .filter(wf -> wf.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new NotFoundException("Flujo no encontrado o no pertenece a tu empresa"));

        State source = stateRepository.findById(dto.getSourceStateId())
                .filter(s -> s.getWorkflow().getId().equals(pathFlowId))
                .orElseThrow(() -> new IllegalArgumentException("El estado origen no pertenece al flujo"));

        State target = stateRepository.findById(dto.getTargetStateId())
                .filter(s -> s.getWorkflow().getId().equals(pathFlowId))
                .orElseThrow(() -> new IllegalArgumentException("El estado destino no pertenece al flujo"));

        transition.setAction(dto.getAction());
        transition.setCondition(dto.getCondition());
        transition.setWorkflow(workflow);
        transition.setSource_state(source);
        transition.setTarget_state(target);

        return transitionRepository.save(transition);
    }

}
