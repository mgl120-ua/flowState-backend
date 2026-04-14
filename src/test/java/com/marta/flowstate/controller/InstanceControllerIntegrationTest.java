package com.marta.flowstate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marta.flowstate.model.AppUser;
import com.marta.flowstate.model.Instance;
import com.marta.flowstate.model.State;
import com.marta.flowstate.model.Workflow;
import com.marta.flowstate.service.InstanceService;
import com.marta.flowstate.service.TransitionExecutionService;
import com.marta.flowstate.security.SessionUserService;
import com.marta.flowstate.dto.InstanceDTO;
import com.marta.flowstate.repository.InstanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(InstanceController.class)
@AutoConfigureMockMvc(addFilters = false)
class InstanceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InstanceService instanceService;

    @MockBean
    private TransitionExecutionService transitionExecutionService;

    @MockBean
    private InstanceRepository instanceRepository;

    @MockBean
    private SessionUserService sessionUserService;

    @MockBean
    private com.marta.flowstate.util.JwtUtil jwtUtil;

    @MockBean
    private com.marta.flowstate.repository.AppUserRepository appUserRepository;

    @Test
    void createInstanceReturnsCreatedInstance() throws Exception {
        InstanceDTO instanceDTO = new InstanceDTO();
        instanceDTO.setData(Map.of("field", "value"));

        State state = new State();
        state.setId(1L);
        state.setName("Initial");

        Workflow workflow = new Workflow();
        workflow.setId(5L);
        workflow.setName("Workflow Test");

        Instance savedInstance = new Instance();
        savedInstance.setId(10L);
        savedInstance.setState(state);
        savedInstance.setWorkflow(workflow);
        savedInstance.setData(Map.of("field", "value"));
        savedInstance.setDate(LocalDateTime.of(2026, 4, 14, 12, 0));

        when(sessionUserService.getCurrentCompanyId()).thenReturn(1L);
        when(instanceService.createInstance(anyLong(), any(InstanceDTO.class), anyLong())).thenReturn(savedInstance);

        mockMvc.perform(post("/flows/5/instances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(instanceDTO)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(savedInstance)));
    }

    @Test
    void getInstanceByIdReturnsInstanceWhenCompanyMatches() throws Exception {
        State state = new State();
        state.setId(1L);
        state.setName("Initial");

        Workflow workflow = new Workflow();
        workflow.setId(5L);
        workflow.setName("Workflow Test");
        workflow.setCompany(new com.marta.flowstate.model.Company());
        workflow.getCompany().setId(1L);

        Instance instance = new Instance();
        instance.setId(10L);
        instance.setState(state);
        instance.setWorkflow(workflow);
        instance.setData(Map.of("field", "value"));
        instance.setDate(LocalDateTime.of(2026, 4, 14, 12, 0));

        when(sessionUserService.getCurrentCompanyId()).thenReturn(1L);
        when(instanceService.getInstanceById(10L)).thenReturn(instance);

        mockMvc.perform(get("/flows/5/instances/10"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(instance)));
    }

    @Test
    void executeTransitionReturnsOkAndCallsService() throws Exception {
        AppUser currentUser = new AppUser();
        currentUser.setId(100L);

        when(sessionUserService.getCurrentUser()).thenReturn(currentUser);

        mockMvc.perform(post("/flows/5/instances/10/transitions/20/execute"))
                .andExpect(status().isOk());

        verify(transitionExecutionService).executeTransition(10L, 20L, 100L);
    }
}
