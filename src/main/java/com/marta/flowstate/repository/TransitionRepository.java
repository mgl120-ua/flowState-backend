package com.marta.flowstate.repository;

import com.marta.flowstate.model.Transition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface TransitionRepository extends JpaRepository<Transition, Long> {
    List<Transition> findByWorkflowId(Long workflowId);
}
