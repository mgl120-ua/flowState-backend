package com.marta.flowstate.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marta.flowstate.repository.InstanceRepository;
import com.marta.flowstate.repository.TransitionRepository;
import com.marta.flowstate.repository.Transition_PermissionRepository;
import com.marta.flowstate.repository.Instance_HistoryRepository;
import com.marta.flowstate.model.Instance;
import com.marta.flowstate.model.Transition;
import com.marta.flowstate.model.Instance_History;

import java.time.LocalDateTime;
import com.marta.flowstate.utils.ConditionCheck;

@Service
public class TransitionExecutionService {

    private final InstanceRepository instanceRepository;
    private final TransitionRepository transitionRepository;
    private final Transition_PermissionRepository permissionRepository;
    private final Instance_HistoryRepository historyRepository;
    private final ConditionCheck conditionCheck;

    public TransitionExecutionService(InstanceRepository instanceRepository,
                                      TransitionRepository transitionRepository,
                                      Transition_PermissionRepository permissionRepository,
                                      Instance_HistoryRepository historyRepository,
                                      ConditionCheck conditionCheck) {
        this.instanceRepository = instanceRepository;
        this.transitionRepository = transitionRepository;
        this.permissionRepository = permissionRepository;
        this.historyRepository = historyRepository;
        this.conditionCheck = conditionCheck;
    }

    @Transactional
    public void executeTransition(Long instanceId, Long transitionId, Long userId) {
        Instance instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("Instance not found"));

        Transition transition = transitionRepository.findById(transitionId)
                .orElseThrow(() -> new IllegalArgumentException("Transition not found"));

        // Validar estado actual
        if (!instance.getState().getId().equals(transition.getSource_state().getId())) {
            throw new IllegalArgumentException("Transition not allowed from current state");
        }

        // Validar permisos
        boolean hasPermission = permissionRepository.existsByTransitionIdAndRolId(
                transitionId, instance.getUser().getRol().getId());

        if (!hasPermission) {
            throw new IllegalArgumentException("User has no permission to execute transition");
        }

        // Evaluar condición
        boolean conditionOk = conditionCheck.evaluate(transition.getCondition(), instance.getData());
        if (!conditionOk) {
            throw new IllegalArgumentException("Condition not satisfied");
        }

        // Guardar historial
        Instance_History history = new Instance_History();
        history.setInstance(instance);
        history.setFrom_state(instance.getState());
        history.setTo_state(transition.getTarget_state());
        history.setTransition(transition);
        history.setChanged_by(userId.intValue());
        history.setTimestamp(LocalDateTime.now());
        historyRepository.save(history);

        // Actualizar estado
        instance.setState(transition.getTarget_state());
        instanceRepository.save(instance);
    }
}
