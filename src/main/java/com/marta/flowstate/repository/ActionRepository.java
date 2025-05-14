package com.marta.flowstate.repository;

import com.marta.flowstate.model.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActionRepository extends JpaRepository<Action, Long> {
}
