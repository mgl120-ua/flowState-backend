package com.marta.flowstate.service;

import com.marta.flowstate.model.ActionExecution;
import com.marta.flowstate.repository.ActionExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExecutionTraceService {

    private final ActionExecutionRepository repository;

    public ExecutionTraceService(ActionExecutionRepository repository) {
        this.repository = repository;
    }

    //Transacción propia: se usa para guardar la traza de una ejecución que ha fallado y se ha deshecho
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAll(List<ActionExecution> executions) {
        repository.saveAll(executions);
    }

    public List<ActionExecution> findByInstance(Long instanceId) {
        return repository.findByInstanceIdOrderByIdAsc(instanceId);
    }
}
