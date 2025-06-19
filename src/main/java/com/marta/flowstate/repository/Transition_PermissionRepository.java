package com.marta.flowstate.repository;

import com.marta.flowstate.model.Transition_Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Transition_PermissionRepository extends JpaRepository<Transition_Permission, Long> {

    boolean existsByTransitionIdAndRolId(Long transitionId, Long rolId);
}
