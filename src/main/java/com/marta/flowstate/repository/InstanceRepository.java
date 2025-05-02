package com.marta.flowstate.repository;

import com.marta.flowstate.model.Instance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InstanceRepository extends JpaRepository<Instance, Long> {
    List<Instance> findByWorkflowId(Long workflowId);
}
