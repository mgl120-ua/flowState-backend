package com.marta.flowstate.service;
import com.marta.flowstate.exception.NotFoundException;
import com.marta.flowstate.repository.Transition_PermissionRepository;
import com.marta.flowstate.repository.TransitionRepository;
import com.marta.flowstate.repository.RolRepository;
import com.marta.flowstate.model.Transition;
import com.marta.flowstate.model.Rol;
import com.marta.flowstate.model.Transition_Permission;
import com.marta.flowstate.security.SessionUserService;
import org.springframework.stereotype.Service;
@Service
public class PermissionService {

    private final Transition_PermissionRepository repository;
    private final TransitionRepository transitionRepository;
    private final RolRepository rolRepository;
    private final SessionUserService sessionUserService;

    public PermissionService(Transition_PermissionRepository repository,
                             TransitionRepository transitionRepository,
                             RolRepository rolRepository,
                             SessionUserService sessionUserService) {
        this.repository = repository;
        this.transitionRepository = transitionRepository;
        this.rolRepository = rolRepository;
        this.sessionUserService = sessionUserService;
    }

    public void grantPermission(Long transitionId, Long rolId) {
        //La transición y el rol deben ser de la empresa del admin
        Long companyId = sessionUserService.getCurrentCompanyId();
        Transition transition = findOwnTransition(transitionId, companyId);
        Rol rol = rolRepository.findByCompanyIdAndId(companyId, rolId)
                .orElseThrow(() -> new NotFoundException("Rol " + rolId + " no encontrado"));

        if (!repository.existsByTransition_IdAndRol_Id(transitionId, rolId)) {
            repository.save(new Transition_Permission(null, transition, rol));
        }
    }

    public void revokePermission(Long transitionId, Long rolId) {
        findOwnTransition(transitionId, sessionUserService.getCurrentCompanyId());
        repository.findByTransition_IdAndRol_Id(transitionId, rolId)
                .ifPresent(repository::delete);
    }

    private Transition findOwnTransition(Long transitionId, Long companyId) {
        return transitionRepository.findById(transitionId)
                .filter(t -> t.getWorkflow().getCompany().getId().equals(companyId))
                .orElseThrow(() -> new NotFoundException("Transición " + transitionId + " no encontrada"));
    }
}
