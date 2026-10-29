package com.marta.flowstate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marta.flowstate.dto.ActionDTO;
import com.marta.flowstate.dto.ActionResponseDTO;
import com.marta.flowstate.dto.TransitionActionDTO;
import com.marta.flowstate.exception.NotFoundException;
import com.marta.flowstate.model.Action;
import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.model.Company;
import com.marta.flowstate.model.Transition;
import com.marta.flowstate.model.TransitionAction;
import com.marta.flowstate.model.Workflow;
import com.marta.flowstate.repository.ActionRepository;
import com.marta.flowstate.repository.TransitionActionRepository;
import com.marta.flowstate.repository.TransitionRepository;
import com.marta.flowstate.security.SessionUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionServiceTest {

    @Mock
    private ActionRepository actionRepository;
    @Mock
    private TransitionRepository transitionRepository;
    @Mock
    private TransitionActionRepository transitionActionRepository;
    @Mock
    private SessionUserService sessionUserService;

    private ActionService actionService;
    private Company company;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        ActionExecutorService executor = new ActionExecutorService(null, objectMapper);
        actionService = new ActionService(actionRepository, transitionRepository, transitionActionRepository,
                executor, sessionUserService, objectMapper);
        company = new Company(1L, "Acme");
        lenient().when(sessionUserService.getCurrentCompanyId()).thenReturn(1L);
    }

    private ActionDTO emailDto() {
        ActionDTO dto = new ActionDTO();
        dto.setName(" Aviso ");
        dto.setType("email");
        dto.setConfig(Map.of("to", "a@b.com", "subject", "Hola", "body", "Pedido {{id}}"));
        return dto;
    }

    private Transition transitionOfCompany(Long transitionId, Long flowId, Long companyId) {
        Workflow wf = new Workflow();
        wf.setId(flowId);
        wf.setCompany(new Company(companyId, "C" + companyId));
        Transition t = new Transition();
        t.setId(transitionId);
        t.setWorkflow(wf);
        return t;
    }

    @Test
    void createActionAssignsAdminCompanyAndNormalizesType() {
        AppUser admin = new AppUser();
        admin.setCompany(company);
        when(sessionUserService.getCurrentUser()).thenReturn(admin);
        when(actionRepository.save(any())).thenAnswer(inv -> {
            Action a = inv.getArgument(0);
            a.setId(5L);
            return a;
        });

        ActionResponseDTO result = actionService.createAction(emailDto());

        ArgumentCaptor<Action> saved = ArgumentCaptor.forClass(Action.class);
        verify(actionRepository).save(saved.capture());
        assertEquals(company, saved.getValue().getCompany());
        assertEquals("EMAIL", saved.getValue().getType());
        assertEquals("Aviso", saved.getValue().getName());
        assertEquals(5L, result.getId());
        assertEquals("a@b.com", result.getConfig().get("to"));
    }

    @Test
    void createActionRejectsIncompleteConfig() {
        ActionDTO dto = emailDto();
        dto.setConfig(Map.of("to", "a@b.com"));
        when(sessionUserService.getCurrentUser()).thenReturn(new AppUser());

        assertThrows(IllegalArgumentException.class, () -> actionService.createAction(dto));
        verify(actionRepository, never()).save(any());
    }

    @Test
    void createActionRejectsUnknownType() {
        ActionDTO dto = emailDto();
        dto.setType("FAX");
        when(sessionUserService.getCurrentUser()).thenReturn(new AppUser());

        assertThrows(IllegalArgumentException.class, () -> actionService.createAction(dto));
    }

    @Test
    void getActionOfOtherCompanyIsNotFound() {
        when(actionRepository.findByIdAndCompanyId(9L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> actionService.getAction(9L));
    }

    @Test
    void attachActionToTransitionOfOtherCompanyIsNotFound() {
        when(transitionRepository.findById(3L)).thenReturn(Optional.of(transitionOfCompany(3L, 7L, 2L)));
        TransitionActionDTO dto = new TransitionActionDTO();
        dto.setActionId(5L);

        assertThrows(NotFoundException.class, () -> actionService.attachAction(7L, 3L, dto));
        verify(transitionActionRepository, never()).save(any());
    }

    @Test
    void attachActionOfOtherCompanyIsNotFound() {
        when(transitionRepository.findById(3L)).thenReturn(Optional.of(transitionOfCompany(3L, 7L, 1L)));
        when(actionRepository.findByIdAndCompanyId(5L, 1L)).thenReturn(Optional.empty());
        TransitionActionDTO dto = new TransitionActionDTO();
        dto.setActionId(5L);

        assertThrows(NotFoundException.class, () -> actionService.attachAction(7L, 3L, dto));
    }

    @Test
    void attachActionTwiceIsConflict() {
        when(transitionRepository.findById(3L)).thenReturn(Optional.of(transitionOfCompany(3L, 7L, 1L)));
        Action action = new Action(5L, "Aviso", "EMAIL", "{}", company);
        when(actionRepository.findByIdAndCompanyId(5L, 1L)).thenReturn(Optional.of(action));
        when(transitionActionRepository.existsByTransitionIdAndActionId(3L, 5L)).thenReturn(true);
        TransitionActionDTO dto = new TransitionActionDTO();
        dto.setActionId(5L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> actionService.attachAction(7L, 3L, dto));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void attachActionSavesPosition() {
        Transition transition = transitionOfCompany(3L, 7L, 1L);
        when(transitionRepository.findById(3L)).thenReturn(Optional.of(transition));
        Action action = new Action(5L, "Aviso", "EMAIL", "{}", company);
        when(actionRepository.findByIdAndCompanyId(5L, 1L)).thenReturn(Optional.of(action));
        when(transitionActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        TransitionActionDTO dto = new TransitionActionDTO();
        dto.setActionId(5L);
        dto.setPosition(2);

        TransitionAction saved = actionService.attachAction(7L, 3L, dto);

        assertEquals(transition, saved.getTransition());
        assertEquals(action, saved.getAction());
        assertEquals(2, saved.getPosition());
    }
}
