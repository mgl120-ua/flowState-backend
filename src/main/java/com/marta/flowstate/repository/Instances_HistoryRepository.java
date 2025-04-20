package com.marta.flowstate.repository;

import com.marta.flowstate.model.Instances_History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Instances_HistoryRepository extends JpaRepository<Instances_History, Long> {
}
