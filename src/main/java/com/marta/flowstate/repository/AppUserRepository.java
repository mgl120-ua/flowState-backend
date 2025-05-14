package com.marta.flowstate.repository;

import com.marta.flowstate.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    boolean existsByEmail(String email);
    List<AppUser> findByCompanyId(Long companyId);
}
