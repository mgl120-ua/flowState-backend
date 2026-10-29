package com.marta.flowstate.repository;

import com.marta.flowstate.model.TransitionAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransitionActionRepository extends JpaRepository<TransitionAction, Long> {
    List<TransitionAction> findByTransitionIdOrderByPositionAscIdAsc(Long transitionId);
    Optional<TransitionAction> findByTransitionIdAndActionId(Long transitionId, Long actionId);
    boolean existsByTransitionIdAndActionId(Long transitionId, Long actionId);
}
