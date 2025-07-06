package com.marta.flowstate.controller;

import com.marta.flowstate.model.Instance;
import com.marta.flowstate.model.Instance_History;
import com.marta.flowstate.dto.InstanceDTO;
import com.marta.flowstate.service.InstanceService;
import com.marta.flowstate.service.TransitionExecutionService;
import com.marta.flowstate.repository.InstanceRepository;
import com.marta.flowstate.security.SessionUserService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/flows/{flowId}/instances")
@SecurityRequirement(name = "bearerAuth")
public class InstanceController {

    private final InstanceService instanceService;
    private final TransitionExecutionService transitionExecutionService;
    private final InstanceRepository instanceRepository;
    private final SessionUserService sessionUserService;

    @Autowired
    public InstanceController(
            InstanceService instanceService,
            TransitionExecutionService transitionExecutionService,
            InstanceRepository instanceRepository,
            SessionUserService sessionUserService
    ) {
        this.instanceService = instanceService;
        this.transitionExecutionService = transitionExecutionService;
        this.instanceRepository = instanceRepository;
        this.sessionUserService = sessionUserService;
    }

    @GetMapping
    public List<Instance> getInstancesByWorkflowId(@PathVariable Long flowId) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        return instanceService.getInstancesByWorkflowId(flowId, companyId);
    }

    @PostMapping
    public Instance createInstance(@PathVariable Long flowId, @RequestBody @Valid InstanceDTO dto) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        return instanceService.createInstance(flowId, dto, companyId);
    }

    @GetMapping("/{instanceId}")
    public Instance getInstanceById(@PathVariable Long instanceId) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        Instance instance = instanceService.getInstanceById(instanceId);
        if (!instance.getWorkflow().getCompany().getId().equals(companyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a esta instancia");
        }
        return instance;
    }

    @DeleteMapping("/{instanceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInstance(@PathVariable Long instanceId) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        instanceService.deleteInstance(instanceId, companyId);
    }

    @GetMapping("/{instanceId}/history")
    public List<Instance_History> getInstanceHistory(@PathVariable Long instanceId) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        return instanceService.getInstanceHistory(instanceId, companyId);
    }

    @PutMapping("/{instanceId}")
    public Instance updateInstance(@PathVariable Long flowId, @PathVariable Long instanceId, @Valid @RequestBody InstanceDTO dto) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        return instanceService.updateInstance(instanceId, dto, flowId, companyId);
    }

    @PostMapping("/{instanceId}/transitions/{transitionId}/execute")
    public ResponseEntity<Void> executeTransition(@PathVariable Long flowId,@PathVariable Long instanceId,@PathVariable Long transitionId) {
        Long userId = sessionUserService.getCurrentUser().getId();
        transitionExecutionService.executeTransition(instanceId, transitionId, userId);
        return ResponseEntity.ok().build();
    }


    @PutMapping("/{instanceId}/data")
    public ResponseEntity<Instance> updateInstanceData(
            @PathVariable Long instanceId,
            @RequestBody Map<String, Object> newDataJson
    ) {
        Long companyId = sessionUserService.getCurrentCompanyId();
        Instance instance = instanceService.getInstanceById(instanceId);
        if (!instance.getWorkflow().getCompany().getId().equals(companyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos");
        }

        try {
            instance.setData(newDataJson);
            instanceRepository.save(instance);
            return ResponseEntity.ok(instance);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al actualizar los datos", e);
        }
    }

}
