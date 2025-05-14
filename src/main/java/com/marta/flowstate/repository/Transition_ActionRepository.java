package com.marta.flowstate.repository;

import com.marta.flowstate.model.TransitionAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface Transition_ActionRepository extends JpaRepository<TransitionAction, Long> {
    List<TransitionAction> findByTransitionId(Long transitionId);
}
