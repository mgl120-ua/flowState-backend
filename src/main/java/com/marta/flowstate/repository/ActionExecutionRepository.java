package com.marta.flowstate.repository;

import com.marta.flowstate.model.ActionExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionExecutionRepository extends JpaRepository<ActionExecution, Long> {
    List<ActionExecution> findByInstanceIdOrderByIdAsc(Long instanceId);
}
