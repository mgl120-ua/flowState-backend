package com.marta.flowstate.repository;

import com.marta.flowstate.model.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.marta.flowstate.model.StateType;
import java.util.List;
import java.util.Optional;


@Repository
public interface StateRepository extends JpaRepository<State, Long> {
    List<State> findByWorkflowId(Long workflowId);
    boolean existsByWorkflowIdAndType(Long workflowId, StateType type);
    List<State> findByWorkflowCompanyId(Long companyId);
    Optional<State> findByIdAndWorkflowCompanyId(Long id, Long companyId);
    Optional<State> findFirstByWorkflowIdAndType(Long workflowId, StateType type);
}
