package com.marta.flowstate.repository;

import com.marta.flowstate.model.Transition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface TransitionRepository extends JpaRepository<Transition, Long> {
    List<Transition> findByWorkflowId(Long workflowId);

    @Query("select t from Transition t where t.source_state.id = :stateId and t.automatic = true order by t.id")
    List<Transition> findAutomaticFromState(@Param("stateId") Long stateId);
}
