package com.marta.flowstate.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.io.Serializable;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transition")
public class Transition implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String action;
    private String condition;
    @ManyToOne
    @JoinColumn(name = "source_state_id")
    private State source_state;
    @ManyToOne
    @JoinColumn(name = "target_state_id")
    private State target_state;
    @ManyToOne
    @JoinColumn(name = "workflow_id")
    private Workflow workflow;
}