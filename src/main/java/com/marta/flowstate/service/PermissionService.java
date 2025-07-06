package com.marta.flowstate.service;
import com.marta.flowstate.repository.Transition_PermissionRepository;
import com.marta.flowstate.repository.TransitionRepository;
import com.marta.flowstate.repository.RolRepository;
import com.marta.flowstate.model.Transition;
import com.marta.flowstate.model.Rol;
import com.marta.flowstate.model.Transition_Permission;
import org.springframework.stereotype.Service;
@Service
public class PermissionService {

    private final Transition_PermissionRepository repository;
    private final TransitionRepository transitionRepository;
    private final RolRepository rolRepository;

    public PermissionService(Transition_PermissionRepository repository,
                             TransitionRepository transitionRepository,
                             RolRepository rolRepository) {
        this.repository = repository;
        this.transitionRepository = transitionRepository;
        this.rolRepository = rolRepository;
    }

    public void grantPermission(Long transitionId, Long rolId) {
        if (!repository.existsByTransition_IdAndRol_Id(transitionId, rolId)) {
            Transition transition = transitionRepository.findById(transitionId)
                    .orElseThrow(() -> new IllegalArgumentException("Transición no encontrada"));
            Rol rol = rolRepository.findById(rolId)
                    .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

            repository.save(new Transition_Permission(null, transition, rol));
        }
    }

    public void revokePermission(Long transitionId, Long rolId) {
        repository.findByTransition_IdAndRol_Id(transitionId, rolId)
                .ifPresent(repository::delete);
    }
}
