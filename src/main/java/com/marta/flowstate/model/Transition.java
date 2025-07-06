package com.marta.flowstate.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import com.marta.flowstate.model.Transition_Permission;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

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
    @JsonBackReference
    private Workflow workflow;


    @OneToMany(mappedBy = "transition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transition_Permission> permissions = new ArrayList<>();
}