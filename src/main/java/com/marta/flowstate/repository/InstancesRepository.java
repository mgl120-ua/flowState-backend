package com.marta.flowstate.repository;

import com.marta.flowstate.model.Instances;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstancesRepository extends JpaRepository<Instances, Long> {
}
