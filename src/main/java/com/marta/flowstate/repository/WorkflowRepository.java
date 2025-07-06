package com.marta.flowstate.repository;

import com.marta.flowstate.model.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    List<Workflow> findByCompanyId(Long companyId);
}
