package com.marta.flowstate.repository;

import com.marta.flowstate.model.ActionExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActionExecutionRepository extends JpaRepository<ActionExecution, Long> {
    List<ActionExecution> findByInstanceIdOrderByIdAsc(Long instanceId);

    //Tokens LLM consumidos por la empresa desde una fecha (las trazas LLM son las que tienen modelo)
    @Query("""
            select coalesce(sum(coalesce(e.inputTokens, 0) + coalesce(e.outputTokens, 0)), 0)
            from ActionExecution e
            where e.instance.workflow.company.id = :companyId and e.model is not null and e.startedAt >= :since
            """)
    long sumLlmTokensSince(@Param("companyId") Long companyId, @Param("since") LocalDateTime since);

    //Filas: modelo, llamadas, tokens de entrada, tokens de salida, coste
    @Query("""
            select e.model, count(e), sum(e.inputTokens), sum(e.outputTokens), sum(e.costUsd)
            from ActionExecution e
            where e.instance.workflow.company.id = :companyId and e.model is not null
              and e.startedAt >= :from and e.startedAt < :to
            group by e.model
            order by e.model
            """)
    List<Object[]> llmUsageByModel(@Param("companyId") Long companyId, @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to);

    //Filas: id del flujo, nombre, llamadas, tokens de entrada, tokens de salida, coste
    @Query("""
            select e.instance.workflow.id, e.instance.workflow.name, count(e), sum(e.inputTokens),
                   sum(e.outputTokens), sum(e.costUsd)
            from ActionExecution e
            where e.instance.workflow.company.id = :companyId and e.model is not null
              and e.startedAt >= :from and e.startedAt < :to
            group by e.instance.workflow.id, e.instance.workflow.name
            order by e.instance.workflow.name
            """)
    List<Object[]> llmUsageByFlow(@Param("companyId") Long companyId, @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);
}
