package com.marta.flowstate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.marta.flowstate.exception.ActionExecutionException;
import com.marta.flowstate.exception.ActionFailedException;
import com.marta.flowstate.exception.NotFoundException;
import com.marta.flowstate.model.ActionExecution;
import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.model.Instance;
import com.marta.flowstate.model.Instance_History;
import com.marta.flowstate.model.Transition;
import com.marta.flowstate.repository.ActionExecutionRepository;
import com.marta.flowstate.repository.AppUserRepository;
import com.marta.flowstate.repository.InstanceRepository;
import com.marta.flowstate.repository.TransitionRepository;
import com.marta.flowstate.repository.Transition_PermissionRepository;
import com.marta.flowstate.repository.Instance_HistoryRepository;
import com.marta.flowstate.util.ConditionCheck;
import com.marta.flowstate.repository.TransitionActionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import com.marta.flowstate.model.Action;
import com.marta.flowstate.model.TransitionAction;


@Service
public class TransitionExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(TransitionExecutionService.class);
    //Tamaño máximo de la respuesta que se guarda en la traza (la completa queda en los datos de la instancia)
    private static final int MAX_TRACE_RESPONSE_CHARS = 8 * 1024;

    private final InstanceRepository instanceRepository;
    private final TransitionRepository transitionRepository;
    private final Transition_PermissionRepository permissionRepository;
    private final Instance_HistoryRepository historyRepository;
    private final ConditionCheck conditionCheck;
    private final MailService mailService;
    private final ObjectMapper objectMapper;
    private final ActionExecutorService actionExecutorService;
    private final TransitionActionRepository transitionActionRepository;
    private final AppUserRepository appUserRepository;
    private final ActionExecutionRepository actionExecutionRepository;
    private final ExecutionTraceService executionTraceService;
    private final TransactionTemplate transactionTemplate;


    public TransitionExecutionService(
            InstanceRepository instanceRepository,
            TransitionRepository transitionRepository,
            Transition_PermissionRepository permissionRepository,
            Instance_HistoryRepository historyRepository,
            ConditionCheck conditionCheck,
            MailService mailService,
            ObjectMapper objectMapper,
            TransitionActionRepository transitionActionRepository,
            ActionExecutorService actionExecutorService,
            AppUserRepository appUserRepository,
            ActionExecutionRepository actionExecutionRepository,
            ExecutionTraceService executionTraceService,
            TransactionTemplate transactionTemplate
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
        this.appUserRepository = appUserRepository;
        this.actionExecutionRepository = actionExecutionRepository;
        this.executionTraceService = executionTraceService;
        this.transactionTemplate = transactionTemplate;

    }

    /**
     * Ejecuta una transición manual en una única transacción. Si una acción falla, se deshace todo
     * (historial, estado y datos), se guarda la traza de lo ejecutado y se lanza ActionExecutionException.
     */
    public Instance executeTransition(Long instanceId, Long transitionId, Long userId) {
        try {
            return transactionTemplate.execute(status -> doExecuteTransition(instanceId, transitionId, userId));
        } catch (ActionExecutionException e) {
            executionTraceService.saveAll(e.getExecutions());
            throw e;
        }
    }

    private Instance doExecuteTransition(Long instanceId, Long transitionId, Long userId) {
        Instance instance = instanceRepository.findById(instanceId).orElseThrow(() -> new NotFoundException("Instancia no encontrada"));

        Transition transition = transitionRepository.findById(transitionId).orElseThrow(() -> new NotFoundException("Transicion no encontrada"));

        AppUser executor = appUserRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no encontrado"));

        // La instancia debe ser de la empresa de quien ejecuta
        if (executor.getCompany() == null || instance.getWorkflow().getCompany() == null
                || !executor.getCompany().getId().equals(instance.getWorkflow().getCompany().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Instancia no encontrada");
        }

        // Validar estado actual
        if (!instance.getState().getId().equals(transition.getSource_state().getId())) {
            throw new IllegalArgumentException("Transición no permitida desde el estado actual");
        }

        //Validar permisos con el rol de quien ejecuta (no del creador de la instancia)
        boolean hasPermission = executor.getRol() != null && permissionRepository.existsByTransition_IdAndRol_Id(
                transitionId, executor.getRol().getId());

        if (!hasPermission) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no tiene permisos de ejecucion");
        }

        //Evaluar condición
        Map<String, Object> dataMap;
        try {
            dataMap = instance.getData();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al interpretar los datos de la instancia: " + e.getMessage());
        }

        if (transition.getCondition() != null && !transition.getCondition().isBlank()) {
            logger.debug("DATA MAP: {}", dataMap);
            logger.debug("CONDICION: {}", transition.getCondition());
            boolean conditionOk = conditionCheck.evaluate(transition.getCondition(), dataMap);
            if (!conditionOk) {
                throw new IllegalArgumentException("No cumple la condición: " + transition.getCondition());
            }
        }

        List<ActionExecution> executions = new ArrayList<>();
        applyTransition(instance, transition, userId, executions);
        actionExecutionRepository.saveAll(executions);

        if (instance.getUser() != null && instance.getUser().getEmail() != null) {
            String subject = "Transición completada";
            String message = String.format(
                    "Tu proceso '%s' ha pasado de '%s' a '%s'.",
                    instance.getWorkflow().getName(),
                    transition.getSource_state().getName(),
                    instance.getState().getName()
            );
            mailService.sendEmail(instance.getUser().getEmail(), subject, message);
        }
        return instance;
    }

    /**
     * Registra el historial, cambia el estado y ejecuta las acciones de la transición en orden.
     * changedBy es null cuando la transición la ejecuta el sistema.
     */
    private void applyTransition(Instance instance, Transition transition, Long changedBy, List<ActionExecution> executions) {
        // Guardar historial
        Instance_History history = new Instance_History();
        history.setInstance(instance);
        history.setFrom_state(instance.getState());
        history.setTo_state(transition.getTarget_state());
        history.setTransition(transition);
        history.setChanged_by(changedBy);
        history.setTimestamp(LocalDateTime.now());
        historyRepository.save(history);

        // Actualizar estado
        instance.setState(transition.getTarget_state());

        List<TransitionAction> actions = transitionActionRepository.findByTransitionIdOrderByPositionAscIdAsc(transition.getId());
        for (TransitionAction ta : actions) {
            executions.add(runAction(instance, transition, ta.getAction(), executions));
        }
        instanceRepository.save(instance);
    }

    private ActionExecution runAction(Instance instance, Transition transition, Action action, List<ActionExecution> executions) {
        ActionExecution trace = new ActionExecution();
        trace.setInstance(instance);
        trace.setTransition(transition);
        trace.setAction(action);
        trace.setActionName(action.getName());
        trace.setActionType(action.getType());
        trace.setStartedAt(LocalDateTime.now());
        long start = System.nanoTime();
        ActionResult result;
        try {
            result = actionExecutorService.execute(action, instance.getData());
        } catch (RuntimeException e) {
            trace.setDurationMs((System.nanoTime() - start) / 1_000_000);
            trace.setStatus(ActionExecution.FAILED);
            trace.setError(e.getMessage());
            if (e instanceof ActionFailedException failed) {
                trace.setHttpStatus(failed.getHttpStatus());
                trace.setRequestSummary(failed.getRequestSummary());
            }
            logger.warn("Falla la acción '{}' de la transición {}: {}", action.getName(), transition.getId(), e.getMessage());
            List<ActionExecution> all = new ArrayList<>(executions);
            all.add(trace);
            throw new ActionExecutionException(action.getName(), e.getMessage(), all, e);
        }
        trace.setDurationMs((System.nanoTime() - start) / 1_000_000);
        trace.setStatus(ActionExecution.SUCCESS);
        if (result != null) {
            trace.setHttpStatus(result.httpStatus());
            trace.setRequestSummary(result.requestSummary());
            trace.setResponseSummary(traceResponse(result.output()));
            if (result.saveAs() != null) {
                Map<String, Object> data = instance.getData() != null ? new LinkedHashMap<>(instance.getData()) : new LinkedHashMap<>();
                data.put(result.saveAs(), result.output());
                instance.setData(data);
            }
        }
        return trace;
    }

    private Map<String, Object> traceResponse(Map<String, Object> output) {
        if (output == null) return null;
        try {
            String json = objectMapper.writeValueAsString(output);
            if (json != null && json.length() <= MAX_TRACE_RESPONSE_CHARS) return output;
        } catch (JsonProcessingException e) {
            logger.debug("No se puede serializar la respuesta para la traza", e);
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("status", output.get("status"));
        summary.put("truncated", true);
        return summary;
    }
}
