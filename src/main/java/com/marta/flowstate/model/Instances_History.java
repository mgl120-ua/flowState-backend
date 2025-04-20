package com.marta.flowstate.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.io.Serializable;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Instances_History")
public class Instances_History implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime timestamp;
    @ManyToOne
    @JoinColumn(name = "instance_id")
    private Instances instances;
    @ManyToOne
    @JoinColumn(name = "from_state_id")
    private State  from_state;
    @ManyToOne
    @JoinColumn(name = "to_state_id")
    private State  to_state;
    @ManyToOne
    @JoinColumn(name = "transition_id")
    private Transition transition;
    private Integer changed_by;
}