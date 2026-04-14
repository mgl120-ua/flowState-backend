package com.marta.flowstate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marta.flowstate.model.Action;
import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.model.Instance;
import com.marta.flowstate.model.Instance_History;
import com.marta.flowstate.model.Rol;
import com.marta.flowstate.model.State;
import com.marta.flowstate.model.Transition;
import com.marta.flowstate.model.TransitionAction;
import com.marta.flowstate.model.Workflow;
import com.marta.flowstate.repository.InstanceRepository;
import com.marta.flowstate.repository.Instance_HistoryRepository;
import com.marta.flowstate.repository.TransitionActionRepository;
import com.marta.flowstate.repository.TransitionRepository;
import com.marta.flowstate.repository.Transition_PermissionRepository;
import com.marta.flowstate.util.ConditionCheck;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransitionExecutionServiceTest {

    @Mock
    private InstanceRepository instanceRepository;
    @Mock
    private TransitionRepository transitionRepository;
    @Mock
    private Transition_PermissionRepository permissionRepository;
    @Mock
    private Instance_HistoryRepository historyRepository;
    @Mock
    private ConditionCheck conditionCheck;
    @Mock
    private MailService mailService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ActionExecutorService actionExecutorService;
    @Mock
    private TransitionActionRepository transitionActionRepository;

    @InjectMocks
    private TransitionExecutionService transitionExecutionService;

    @Test
    void executeTransitionUpdatesStateAndSendsEmailWhenConditionPasses() {
        Long instanceId = 1L;
        Long transitionId = 2L;
        Long userId = 42L;

        State sourceState = new State();
        sourceState.setId(100L);
        sourceState.setName("Pending");

        State targetState = new State();
        targetState.setId(101L);
        targetState.setName("Approved");

        Workflow workflow = new Workflow();
        workflow.setId(10L);
        workflow.setName("Test workflow");

        AppUser user = new AppUser();
        user.setId(20L);
        Rol rol = new Rol();
        rol.setId(30L);
        rol.setName("USER");
        user.setRol(rol);
        user.setEmail("user@test.com");

        Instance instance = new Instance();
        instance.setId(instanceId);
        instance.setState(sourceState);
        instance.setWorkflow(workflow);
        instance.setUser(user);
        instance.setData(Map.of("amount", 500));
        instance.setDate(LocalDateTime.now());

        Transition transition = new Transition();
        transition.setId(transitionId);
        transition.setSource_state(sourceState);
        transition.setTarget_state(targetState);
        transition.setCondition("amount >= 100");

        Action action = new Action();
        action.setId(55L);
        action.setType("EMAIL");
        TransitionAction transitionAction = new TransitionAction();
        transitionAction.setId(66L);
        transitionAction.setAction(action);

        when(instanceRepository.findById(instanceId)).thenReturn(Optional.of(instance));
        when(transitionRepository.findById(transitionId)).thenReturn(Optional.of(transition));
        when(permissionRepository.existsByTransition_IdAndRol_Id(transitionId, rol.getId())).thenReturn(true);
        when(conditionCheck.evaluate(transition.getCondition(), instance.getData())).thenReturn(true);
        when(transitionActionRepository.findByTransitionId(transitionId)).thenReturn(List.of(transitionAction));
        when(instanceRepository.save(any())).thenReturn(instance);

        transitionExecutionService.executeTransition(instanceId, transitionId, userId);

        assertEquals(targetState, instance.getState());
        verify(historyRepository).save(any(Instance_History.class));
        verify(instanceRepository).save(instance);
        verify(actionExecutorService).execute(action, instance.getData());
        verify(mailService).sendEmail(eq(user.getEmail()), anyString(), anyString());
    }

    @Test
    void executeTransitionThrowsWhenConditionFails() {
        Long instanceId = 1L;
        Long transitionId = 2L;
        Long userId = 42L;

        State sourceState = new State();
        sourceState.setId(100L);
        sourceState.setName("Pending");

        State targetState = new State();
        targetState.setId(101L);
        targetState.setName("Approved");

        Workflow workflow = new Workflow();
        workflow.setId(10L);
        workflow.setName("Test workflow");

        AppUser user = new AppUser();
        user.setId(20L);
        Rol rol = new Rol();
        rol.setId(30L);
        rol.setName("USER");
        user.setRol(rol);
        user.setEmail("user@test.com");

        Instance instance = new Instance();
        instance.setId(instanceId);
        instance.setState(sourceState);
        instance.setWorkflow(workflow);
        instance.setUser(user);
        instance.setData(Map.of("amount", 50));
        instance.setDate(LocalDateTime.now());

        Transition transition = new Transition();
        transition.setId(transitionId);
        transition.setSource_state(sourceState);
        transition.setTarget_state(targetState);
        transition.setCondition("amount >= 100");

        when(instanceRepository.findById(instanceId)).thenReturn(Optional.of(instance));
        when(transitionRepository.findById(transitionId)).thenReturn(Optional.of(transition));
        when(permissionRepository.existsByTransition_IdAndRol_Id(transitionId, rol.getId())).thenReturn(true);
        when(conditionCheck.evaluate(transition.getCondition(), instance.getData())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transitionExecutionService.executeTransition(instanceId, transitionId, userId));

        assertEquals("No cumple la condición: " + transition.getCondition(), exception.getMessage());
        verify(historyRepository, never()).save(any());
        verify(actionExecutorService, never()).execute(any(), any());
        verify(mailService, never()).sendEmail(anyString(), anyString(), anyString());
    }
}
