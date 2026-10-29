package com.marta.flowstate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marta.flowstate.exception.ActionExecutionException;
import com.marta.flowstate.exception.ActionFailedException;
import com.marta.flowstate.model.Action;
import com.marta.flowstate.model.ActionExecution;
import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.model.Company;
import com.marta.flowstate.model.Instance;
import com.marta.flowstate.model.Instance_History;
import com.marta.flowstate.model.Rol;
import com.marta.flowstate.model.State;
import com.marta.flowstate.model.StateType;
import com.marta.flowstate.model.Transition;
import com.marta.flowstate.model.TransitionAction;
import com.marta.flowstate.model.Workflow;
import com.marta.flowstate.repository.ActionExecutionRepository;
import com.marta.flowstate.repository.AppUserRepository;
import com.marta.flowstate.repository.InstanceRepository;
import com.marta.flowstate.repository.Instance_HistoryRepository;
import com.marta.flowstate.repository.TransitionActionRepository;
import com.marta.flowstate.repository.TransitionRepository;
import com.marta.flowstate.repository.Transition_PermissionRepository;
import com.marta.flowstate.util.ConditionCheck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

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
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private ActionExecutionRepository actionExecutionRepository;
    @Mock
    private ExecutionTraceService executionTraceService;
    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private TransitionExecutionService transitionExecutionService;

    @BeforeEach
    void runTransactionCallbackDirectly() {
        lenient().when(transactionTemplate.execute(any())).thenAnswer(inv ->
                inv.<TransactionCallback<?>>getArgument(0).doInTransaction(null));
    }

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
        workflow.setCompany(company(1L));

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
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(executor(userId, 1L, rol)));
        when(permissionRepository.existsByTransition_IdAndRol_Id(transitionId, rol.getId())).thenReturn(true);
        when(conditionCheck.evaluate(transition.getCondition(), instance.getData())).thenReturn(true);
        when(transitionActionRepository.findByTransitionIdOrderByPositionAscIdAsc(transitionId)).thenReturn(List.of(transitionAction));
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
        workflow.setCompany(company(1L));

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
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(executor(userId, 1L, rol)));
        when(permissionRepository.existsByTransition_IdAndRol_Id(transitionId, rol.getId())).thenReturn(true);
        when(conditionCheck.evaluate(transition.getCondition(), instance.getData())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transitionExecutionService.executeTransition(instanceId, transitionId, userId));

        assertEquals("No cumple la condición: " + transition.getCondition(), exception.getMessage());
        verify(historyRepository, never()).save(any());
        verify(actionExecutorService, never()).execute(any(), any());
        verify(mailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void executeTransitionUsesExecutorRoleNotCreatorRole() {
        Long instanceId = 1L;
        Long transitionId = 2L;
        Long userId = 42L;

        Rol creatorRol = new Rol();
        creatorRol.setId(30L);
        Rol executorRol = new Rol();
        executorRol.setId(31L);

        Instance instance = instanceWithCreatorRol(instanceId, creatorRol);
        Transition transition = transitionFrom(transitionId, instance.getState());

        when(instanceRepository.findById(instanceId)).thenReturn(Optional.of(instance));
        when(transitionRepository.findById(transitionId)).thenReturn(Optional.of(transition));
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(executor(userId, 1L, executorRol)));
        when(permissionRepository.existsByTransition_IdAndRol_Id(transitionId, executorRol.getId())).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transitionExecutionService.executeTransition(instanceId, transitionId, userId));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(permissionRepository, never()).existsByTransition_IdAndRol_Id(transitionId, creatorRol.getId());
        verify(historyRepository, never()).save(any());
        verify(instanceRepository, never()).save(any());
    }

    @Test
    void executeTransitionIsForbiddenWhenExecutorHasNoRole() {
        Long instanceId = 1L;
        Long transitionId = 2L;
        Long userId = 42L;

        Rol creatorRol = new Rol();
        creatorRol.setId(30L);
        Instance instance = instanceWithCreatorRol(instanceId, creatorRol);
        Transition transition = transitionFrom(transitionId, instance.getState());

        when(instanceRepository.findById(instanceId)).thenReturn(Optional.of(instance));
        when(transitionRepository.findById(transitionId)).thenReturn(Optional.of(transition));
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(executor(userId, 1L, null)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transitionExecutionService.executeTransition(instanceId, transitionId, userId));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void executeTransitionOnInstanceOfOtherCompanyReturnsNotFound() {
        Long instanceId = 1L;
        Long transitionId = 2L;
        Long userId = 42L;

        Rol rol = new Rol();
        rol.setId(30L);
        Instance instance = instanceWithCreatorRol(instanceId, rol);
        Transition transition = transitionFrom(transitionId, instance.getState());

        when(instanceRepository.findById(instanceId)).thenReturn(Optional.of(instance));
        when(transitionRepository.findById(transitionId)).thenReturn(Optional.of(transition));
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(executor(userId, 2L, rol)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transitionExecutionService.executeTransition(instanceId, transitionId, userId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(permissionRepository, never()).existsByTransition_IdAndRol_Id(any(), any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void executeTransitionSavesActionOutputInInstanceDataAndTrace() {
        Rol rol = new Rol();
        rol.setId(30L);
        Instance instance = instanceWithCreatorRol(1L, rol);
        Transition transition = transitionFrom(2L, instance.getState());
        Action action = new Action();
        action.setId(55L);
        action.setName("Validar");
        action.setType("HTTP");
        TransitionAction ta = new TransitionAction();
        ta.setAction(action);

        when(instanceRepository.findById(1L)).thenReturn(Optional.of(instance));
        when(transitionRepository.findById(2L)).thenReturn(Optional.of(transition));
        when(appUserRepository.findById(42L)).thenReturn(Optional.of(executor(42L, 1L, rol)));
        when(permissionRepository.existsByTransition_IdAndRol_Id(2L, 30L)).thenReturn(true);
        when(transitionActionRepository.findByTransitionIdOrderByPositionAscIdAsc(2L)).thenReturn(List.of(ta));
        Map<String, Object> output = Map.of("status", 200, "body", Map.of("ok", true));
        when(actionExecutorService.execute(eq(action), any())).thenReturn(new ActionResult(200, "GET https://api", "validacion", output));

        Instance result = transitionExecutionService.executeTransition(1L, 2L, 42L);

        assertEquals(output, result.getData().get("validacion"));
        assertEquals(500, result.getData().get("amount"));
        ArgumentCaptor<List<ActionExecution>> traces = ArgumentCaptor.forClass(List.class);
        verify(actionExecutionRepository).saveAll(traces.capture());
        ActionExecution trace = traces.getValue().get(0);
        assertEquals(ActionExecution.SUCCESS, trace.getStatus());
        assertEquals(200, trace.getHttpStatus());
        assertEquals("GET https://api", trace.getRequestSummary());
        verify(executionTraceService, never()).saveAll(any());
    }

    @Test
    void executeTransitionWhenActionFailsThrowsAndKeepsFailedTrace() {
        Rol rol = new Rol();
        rol.setId(30L);
        Instance instance = instanceWithCreatorRol(1L, rol);
        Transition transition = transitionFrom(2L, instance.getState());
        Action action = new Action();
        action.setId(55L);
        action.setName("Validar");
        action.setType("HTTP");
        TransitionAction ta = new TransitionAction();
        ta.setAction(action);

        when(instanceRepository.findById(1L)).thenReturn(Optional.of(instance));
        when(transitionRepository.findById(2L)).thenReturn(Optional.of(transition));
        when(appUserRepository.findById(42L)).thenReturn(Optional.of(executor(42L, 1L, rol)));
        when(permissionRepository.existsByTransition_IdAndRol_Id(2L, 30L)).thenReturn(true);
        when(transitionActionRepository.findByTransitionIdOrderByPositionAscIdAsc(2L)).thenReturn(List.of(ta));
        when(actionExecutorService.execute(eq(action), any()))
                .thenThrow(new ActionFailedException("La API respondió 503", 503, "GET https://api"));

        ActionExecutionException ex = assertThrows(ActionExecutionException.class,
                () -> transitionExecutionService.executeTransition(1L, 2L, 42L));

        assertEquals("Validar", ex.getActionName());
        ArgumentCaptor<List<ActionExecution>> traces = ArgumentCaptor.forClass(List.class);
        verify(executionTraceService).saveAll(traces.capture());
        ActionExecution trace = traces.getValue().get(0);
        assertEquals(ActionExecution.FAILED, trace.getStatus());
        assertEquals(503, trace.getHttpStatus());
        assertEquals("La API respondió 503", trace.getError());
        verify(actionExecutionRepository, never()).saveAll(any());
        verify(mailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    private State state(Long id, StateType type) {
        State state = new State();
        state.setId(id);
        state.setName("S" + id);
        state.setType(type);
        return state;
    }

    private Transition automatic(Long id, State from, State to, String condition) {
        Transition t = new Transition();
        t.setId(id);
        t.setSource_state(from);
        t.setTarget_state(to);
        t.setCondition(condition);
        t.setAutomatic(true);
        return t;
    }

    private Instance prepareManualTransition(State from, State to) {
        Rol rol = new Rol();
        rol.setId(30L);
        Instance instance = instanceWithCreatorRol(1L, rol);
        instance.setState(from);
        Transition manual = new Transition();
        manual.setId(2L);
        manual.setSource_state(from);
        manual.setTarget_state(to);
        when(instanceRepository.findById(1L)).thenReturn(Optional.of(instance));
        when(transitionRepository.findById(2L)).thenReturn(Optional.of(manual));
        when(appUserRepository.findById(42L)).thenReturn(Optional.of(executor(42L, 1L, rol)));
        when(permissionRepository.existsByTransition_IdAndRol_Id(2L, 30L)).thenReturn(true);
        return instance;
    }

    @Test
    void automaticTransitionsRunAfterManualOneUsingFirstMatchingCondition() {
        State nuevo = state(100L, StateType.INITIAL);
        State validando = state(101L, StateType.NORMAL);
        State aprobado = state(102L, StateType.FINAL);
        State rechazado = state(103L, StateType.FINAL);
        Instance instance = prepareManualTransition(nuevo, validando);
        Transition toAprobado = automatic(10L, validando, aprobado, "amount >= 1000");
        Transition toRechazado = automatic(11L, validando, rechazado, "true");
        when(transitionRepository.findAutomaticFromState(101L)).thenReturn(List.of(toAprobado, toRechazado));
        when(conditionCheck.evaluate(eq("amount >= 1000"), any())).thenReturn(false);
        when(conditionCheck.evaluate(eq("true"), any())).thenReturn(true);

        Instance result = transitionExecutionService.executeTransition(1L, 2L, 42L);

        assertEquals(rechazado, result.getState());
        ArgumentCaptor<Instance_History> history = ArgumentCaptor.forClass(Instance_History.class);
        verify(historyRepository, times(2)).save(history.capture());
        assertEquals(42L, history.getAllValues().get(0).getChanged_by());
        assertNull(history.getAllValues().get(1).getChanged_by(), "las automáticas las ejecuta el sistema");
        assertEquals(toRechazado, history.getAllValues().get(1).getTransition());
        verify(transitionRepository, never()).findAutomaticFromState(102L);
        verify(transitionRepository, never()).findAutomaticFromState(103L);
    }

    @Test
    void automaticCycleStopsAtMaximumSteps() {
        State a = state(100L, StateType.NORMAL);
        State b = state(101L, StateType.NORMAL);
        prepareManualTransition(a, b);
        when(transitionRepository.findAutomaticFromState(101L)).thenReturn(List.of(automatic(10L, b, a, null)));
        when(transitionRepository.findAutomaticFromState(100L)).thenReturn(List.of(automatic(11L, a, b, null)));
        when(conditionCheck.evaluate(isNull(), any())).thenReturn(true);

        transitionExecutionService.executeTransition(1L, 2L, 42L);

        verify(historyRepository, times(1 + TransitionExecutionService.MAX_AUTOMATIC_STEPS)).save(any());
    }

    @Test
    void runAutomaticTransitionsFromInitialStateOnCreation() {
        State nuevo = state(100L, StateType.INITIAL);
        State listo = state(101L, StateType.NORMAL);
        Instance instance = instanceWithCreatorRol(1L, new Rol());
        instance.setState(nuevo);
        when(instanceRepository.findById(1L)).thenReturn(Optional.of(instance));
        when(transitionRepository.findAutomaticFromState(100L)).thenReturn(List.of(automatic(10L, nuevo, listo, "amount > 100")));
        when(conditionCheck.evaluate(eq("amount > 100"), any())).thenReturn(true);

        Instance result = transitionExecutionService.runAutomaticTransitions(1L);

        assertEquals(listo, result.getState());
        verify(historyRepository).save(any());
        verify(mailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void withoutAutomaticTransitionsInstanceStaysInState() {
        State nuevo = state(100L, StateType.INITIAL);
        Instance instance = instanceWithCreatorRol(1L, new Rol());
        instance.setState(nuevo);
        when(instanceRepository.findById(1L)).thenReturn(Optional.of(instance));

        Instance result = transitionExecutionService.runAutomaticTransitions(1L);

        assertEquals(nuevo, result.getState());
        verify(historyRepository, never()).save(any());
    }

    private Company company(Long id) {
        Company company = new Company();
        company.setId(id);
        return company;
    }

    private AppUser executor(Long id, Long companyId, Rol rol) {
        AppUser executor = new AppUser();
        executor.setId(id);
        executor.setCompany(company(companyId));
        executor.setRol(rol);
        return executor;
    }

    private Instance instanceWithCreatorRol(Long instanceId, Rol creatorRol) {
        State sourceState = new State();
        sourceState.setId(100L);

        Workflow workflow = new Workflow();
        workflow.setId(10L);
        workflow.setCompany(company(1L));

        AppUser creator = new AppUser();
        creator.setId(20L);
        creator.setRol(creatorRol);

        Instance instance = new Instance();
        instance.setId(instanceId);
        instance.setState(sourceState);
        instance.setWorkflow(workflow);
        instance.setUser(creator);
        instance.setData(Map.of("amount", 500));
        return instance;
    }

    private Transition transitionFrom(Long transitionId, State sourceState) {
        State targetState = new State();
        targetState.setId(101L);

        Transition transition = new Transition();
        transition.setId(transitionId);
        transition.setSource_state(sourceState);
        transition.setTarget_state(targetState);
        return transition;
    }
}
