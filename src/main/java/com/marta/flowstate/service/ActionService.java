package com.marta.flowstate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marta.flowstate.dto.ActionDTO;
import com.marta.flowstate.dto.ActionResponseDTO;
import com.marta.flowstate.dto.TransitionActionDTO;
import com.marta.flowstate.exception.NotFoundException;
import com.marta.flowstate.model.Action;
import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.model.Transition;
import com.marta.flowstate.model.TransitionAction;
import com.marta.flowstate.repository.ActionRepository;
import com.marta.flowstate.repository.TransitionActionRepository;
import com.marta.flowstate.repository.TransitionRepository;
import com.marta.flowstate.security.SessionUserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Gestión de las acciones de una empresa y de su asociación a transiciones.
 * Todas las operaciones se limitan a la empresa del usuario en sesión.
 */
@Service
public class ActionService {

    private final ActionRepository actionRepository;
    private final TransitionRepository transitionRepository;
    private final TransitionActionRepository transitionActionRepository;
    private final ActionExecutorService actionExecutorService;
    private final SessionUserService sessionUserService;
    private final ObjectMapper objectMapper;

    public ActionService(ActionRepository actionRepository,
                         TransitionRepository transitionRepository,
                         TransitionActionRepository transitionActionRepository,
                         ActionExecutorService actionExecutorService,
                         SessionUserService sessionUserService,
                         ObjectMapper objectMapper) {
        this.actionRepository = actionRepository;
        this.transitionRepository = transitionRepository;
        this.transitionActionRepository = transitionActionRepository;
        this.actionExecutorService = actionExecutorService;
        this.sessionUserService = sessionUserService;
        this.objectMapper = objectMapper;
    }

    public List<ActionResponseDTO> getActions() {
        return actionRepository.findByCompanyIdOrderByIdAsc(sessionUserService.getCurrentCompanyId())
                .stream().map(this::toResponse).toList();
    }

    public ActionResponseDTO getAction(Long id) {
        return toResponse(findOwnAction(id));
    }

    public ActionResponseDTO createAction(ActionDTO dto) {
        AppUser admin = sessionUserService.getCurrentUser();
        Action action = new Action();
        action.setCompany(admin.getCompany());
        apply(action, dto);
        return toResponse(actionRepository.save(action));
    }

    public ActionResponseDTO updateAction(Long id, ActionDTO dto) {
        Action action = findOwnAction(id);
        apply(action, dto);
        return toResponse(actionRepository.save(action));
    }

    public void deleteAction(Long id) {
        actionRepository.delete(findOwnAction(id));
    }

    public List<TransitionAction> getTransitionActions(Long flowId, Long transitionId) {
        Transition transition = findOwnTransition(flowId, transitionId);
        return transitionActionRepository.findByTransitionIdOrderByPositionAscIdAsc(transition.getId());
    }

    public TransitionAction attachAction(Long flowId, Long transitionId, TransitionActionDTO dto) {
        Transition transition = findOwnTransition(flowId, transitionId);
        Action action = findOwnAction(dto.getActionId());
        if (transitionActionRepository.existsByTransitionIdAndActionId(transition.getId(), action.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La acción ya está asociada a esta transición");
        }
        TransitionAction ta = new TransitionAction();
        ta.setTransition(transition);
        ta.setAction(action);
        ta.setPosition(dto.getPosition() != null ? dto.getPosition() : 0);
        return transitionActionRepository.save(ta);
    }

    public void detachAction(Long flowId, Long transitionId, Long actionId) {
        Transition transition = findOwnTransition(flowId, transitionId);
        TransitionAction ta = transitionActionRepository.findByTransitionIdAndActionId(transition.getId(), actionId)
                .orElseThrow(() -> new NotFoundException("La acción " + actionId + " no está asociada a esta transición"));
        transitionActionRepository.delete(ta);
    }

    private void apply(Action action, ActionDTO dto) {
        String type = dto.getType().trim().toUpperCase(Locale.ROOT);
        actionExecutorService.validateConfig(type, dto.getConfig());
        action.setName(dto.getName().trim());
        action.setType(type);
        try {
            action.setConfig(objectMapper.writeValueAsString(dto.getConfig()));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("La configuración de la acción no es un JSON válido");
        }
    }

    private Action findOwnAction(Long id) {
        return actionRepository.findByIdAndCompanyId(id, sessionUserService.getCurrentCompanyId())
                .orElseThrow(() -> new NotFoundException("Acción " + id + " no encontrada"));
    }

    private Transition findOwnTransition(Long flowId, Long transitionId) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        return transitionRepository.findById(transitionId)
                .filter(t -> t.getWorkflow().getId().equals(flowId))
                .filter(t -> t.getWorkflow().getCompany().getId().equals(companyId))
                .orElseThrow(() -> new NotFoundException("Transición " + transitionId + " no encontrada en el flujo " + flowId));
    }

    private ActionResponseDTO toResponse(Action action) {
        Map<String, Object> config = null;
        if (action.getConfig() != null) {
            try {
                config = objectMapper.readValue(action.getConfig(), new TypeReference<Map<String, Object>>() {});
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Configuración guardada inválida en la acción " + action.getId(), e);
            }
        }
        return new ActionResponseDTO(action.getId(), action.getName(), action.getType(), config);
    }
}
