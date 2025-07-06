package com.marta.flowstate.repository;

import com.marta.flowstate.model.Transition_Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface Transition_PermissionRepository extends JpaRepository<Transition_Permission, Long> {
    boolean existsByTransition_IdAndRol_Id(Long transitionId, Long rolId);
    Optional<Transition_Permission> findByTransition_IdAndRol_Id(Long transitionId, Long rolId);
}
