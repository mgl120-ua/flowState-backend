package com.marta.flowstate.model;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Traza de una ejecución de una acción: qué se hizo, cuánto tardó y cómo terminó.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "action_execution")
public class ActionExecution {

    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "instance_id", nullable = false)
    private Instance instance;
    @ManyToOne
    @JoinColumn(name = "transition_id")
    private Transition transition;
    @ManyToOne
    @JoinColumn(name = "action_id")
    private Action action;
    @Column(name = "action_name")
    private String actionName;
    @Column(name = "action_type", nullable = false)
    private String actionType;
    @Column(nullable = false)
    private String status;
    @Column(name = "http_status")
    private Integer httpStatus;
    @Column(name = "duration_ms", nullable = false)
    private Long durationMs;
    private String error;
    @Column(name = "request_summary")
    private String requestSummary;
    @Type(JsonType.class)
    @Column(name = "response_summary", columnDefinition = "jsonb")
    private Map<String, Object> responseSummary;
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
}
