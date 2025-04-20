package com.marta.flowstate.repository;

import com.marta.flowstate.model.Transition_Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Transition_ActionRepository extends JpaRepository<Transition_Action, Long> {
}
