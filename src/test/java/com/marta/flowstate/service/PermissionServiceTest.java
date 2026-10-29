package com.marta.flowstate.service;

import com.marta.flowstate.exception.NotFoundException;
import com.marta.flowstate.model.Company;
import com.marta.flowstate.model.Rol;
import com.marta.flowstate.model.Transition;
import com.marta.flowstate.model.Transition_Permission;
import com.marta.flowstate.model.Workflow;
import com.marta.flowstate.repository.RolRepository;
import com.marta.flowstate.repository.TransitionRepository;
import com.marta.flowstate.repository.Transition_PermissionRepository;
import com.marta.flowstate.security.SessionUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private Transition_PermissionRepository repository;
    @Mock
    private TransitionRepository transitionRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private SessionUserService sessionUserService;

    @InjectMocks
    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        when(sessionUserService.getCurrentCompanyId()).thenReturn(1L);
    }

    private Transition transitionOfCompany(Long companyId) {
        Workflow wf = new Workflow();
        wf.setCompany(new Company(companyId, "C" + companyId));
        Transition t = new Transition();
        t.setId(2L);
        t.setWorkflow(wf);
        return t;
    }

    @Test
    void grantSavesPermissionForOwnTransitionAndRole() {
        Transition transition = transitionOfCompany(1L);
        Rol rol = new Rol();
        rol.setId(3L);
        when(transitionRepository.findById(2L)).thenReturn(Optional.of(transition));
        when(rolRepository.findByCompanyIdAndId(1L, 3L)).thenReturn(Optional.of(rol));

        permissionService.grantPermission(2L, 3L);

        verify(repository).save(new Transition_Permission(null, transition, rol));
    }

    @Test
    void grantOnTransitionOfOtherCompanyIsNotFound() {
        when(transitionRepository.findById(2L)).thenReturn(Optional.of(transitionOfCompany(2L)));

        assertThrows(NotFoundException.class, () -> permissionService.grantPermission(2L, 3L));
        verify(repository, never()).save(any());
    }

    @Test
    void grantWithRoleOfOtherCompanyIsNotFound() {
        when(transitionRepository.findById(2L)).thenReturn(Optional.of(transitionOfCompany(1L)));
        when(rolRepository.findByCompanyIdAndId(1L, 9L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> permissionService.grantPermission(2L, 9L));
        verify(repository, never()).save(any());
    }

    @Test
    void revokeOnTransitionOfOtherCompanyIsNotFound() {
        when(transitionRepository.findById(2L)).thenReturn(Optional.of(transitionOfCompany(2L)));

        assertThrows(NotFoundException.class, () -> permissionService.revokePermission(2L, 3L));
        verify(repository, never()).delete(any());
    }
}
