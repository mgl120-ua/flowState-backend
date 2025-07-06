package com.marta.flowstate.repository;

import com.marta.flowstate.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    List<Rol> findByCompanyId(Long companyId);
    Optional<Rol> findByCompanyIdAndName(Long companyId, String name);
}
