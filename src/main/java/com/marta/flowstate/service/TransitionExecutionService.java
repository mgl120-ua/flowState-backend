package com.marta.flowstate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import com.marta.flowstate.model.Instance;
import com.marta.flowstate.model.Instance_History;
import com.marta.flowstate.model.Transition;
import com.marta.flowstate.repository.InstanceRepository;
import com.marta.flowstate.repository.TransitionRepository;
import com.marta.flowstate.repository.Transition_PermissionRepository;
import com.marta.flowstate.repository.Instance_HistoryRepository;
import com.marta.flowstate.util.ConditionCheck;
import com.marta.flowstate.repository.TransitionActionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import com.marta.flowstate.model.Action;
import com.marta.flowstate.model.TransitionAction;


@Service
public class TransitionExecutionService {

    private final InstanceRepository instanceRepository;
    private final TransitionRepository transitionRepository;
    private final Transition_PermissionRepository permissionRepository;
    private final Instance_HistoryRepository historyRepository;
    private final ConditionCheck conditionCheck;
    private final MailService mailService;
    private final ObjectMapper objectMapper;
    private final ActionExecutorService actionExecutorService;
    private final TransitionActionRepository transitionActionRepository;


    public TransitionExecutionService(
            InstanceRepository instanceRepository,
            TransitionRepository transitionRepository,
            Transition_PermissionRepository permissionRepository,
            Instance_HistoryRepository historyRepository,
            ConditionCheck conditionCheck,
            MailService mailService,
            ObjectMapper objectMapper,
            TransitionActionRepository transitionActionRepository,
            ActionExecutorService actionExecutorService
    ) {
        this.instanceRepository = instanceRepository;
        this.transitionRepository = transitionRepository;
        this.permissionRepository = permissionRepository;
        this.historyRepository = historyRepository;
        this.conditionCheck = conditionCheck;
        this.mailService = mailService;
        this.objectMapper = objectMapper;
        this.transitionActionRepository = transitionActionRepository;
        this.actionExecutorService = actionExecutorService;

    }

    @Transactional
    public void executeTransition(Long instanceId, Long transitionId, Long userId) {
        Instance instance = instanceRepository.findById(instanceId).orElseThrow(() -> new IllegalArgumentException("Instancia no encontrada"));

        Transition transition = transitionRepository.findById(transitionId).orElseThrow(() -> new IllegalArgumentException("Transicion no encontrada"));

        // Validar estado actual
        if (!instance.getState().getId().equals(transition.getSource_state().getId())) {
            throw new IllegalArgumentException("Transición no permitida desde el estado actual");
        }

        //Validar permisos
        boolean hasPermission = permissionRepository.existsByTransition_IdAndRol_Id(
                transitionId, instance.getUser().getRol().getId());

        if (!hasPermission) {
            throw new IllegalArgumentException("Usuario no tiene permisos de ejecucion");
        }

        //Evaluar condición
        Map<String, Object> dataMap;
        try {
            dataMap = instance.getData();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al interpretar los datos de la instancia: " + e.getMessage());
        }

        if (transition.getCondition() != null && !transition.getCondition().isBlank()) {
            System.out.println("DATA MAP: " + dataMap);
            System.out.println("CONDICION: " + transition.getCondition());
            boolean conditionOk = conditionCheck.evaluate(transition.getCondition(), dataMap);
            if (!conditionOk) {
                throw new IllegalArgumentException("No cumple la condición: " + transition.getCondition());
            }
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

        List<TransitionAction> actions = transitionActionRepository.findByTransitionId(transitionId);
        for (int i = 0; i < actions.size(); i++) {
            TransitionAction ta = actions.get(i);
            Action action = ta.getAction();
            actionExecutorService.execute(action, instance.getData());
        }


        String subject = "Transición completada";
        String message = String.format(
                "Tu proceso '%s' ha pasado de '%s' a '%s'.",
                instance.getWorkflow().getName(),
                transition.getSource_state().getName(),
                transition.getTarget_state().getName()
        );
        mailService.sendEmail(instance.getUser().getEmail(), subject, message);
    }
}
