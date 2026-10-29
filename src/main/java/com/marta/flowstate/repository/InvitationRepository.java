package com.marta.flowstate.repository;

import com.marta.flowstate.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByCode(String code);
    List<Invitation> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    //Marca la invitación como usada solo si nadie la ha usado antes (evita usos simultáneos)
    @Modifying
    @Query("update Invitation i set i.usedAt = :now where i.id = :id and i.usedAt is null")
    int markAsUsed(@Param("id") Long id, @Param("now") LocalDateTime now);
}
