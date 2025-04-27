package com.marta.flowstate.repository;

import com.marta.flowstate.model.Instance_History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface Instance_HistoryRepository extends JpaRepository<Instance_History, Long> {
    List<Instance_History> findByInstanceId(Long instanceId);
}
