package com.marta.flowstate.service;

import com.marta.flowstate.model.Transition;
import com.marta.flowstate.repository.TransitionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransitionService {

    private final TransitionRepository transitionRepository;

    public TransitionService(TransitionRepository transitionRepository) {
        this.transitionRepository = transitionRepository;
    }

    public List<Transition> getTransitionsByWorkflowId(Long workflowId) {
        return transitionRepository.findByWorkflowId(workflowId);
    }

    public Transition getTransitionById(Long id) {
        return transitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error id:" + id + "no encontrado"));
    }

    public Transition createTransition(Transition transition) {
        return transitionRepository.save(transition);
    }

    public void deleteTransition(Long id) {
        if (!transitionRepository.existsById(id)) {
            throw new RuntimeException("Error id:" + id + "no encontrado");
        }
        transitionRepository.deleteById(id);
    }
}
