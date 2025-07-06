package com.marta.flowstate.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "state")
public class State implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private StateType type = StateType.NORMAL;
    @ManyToOne
    @JoinColumn(name = "workflow_id")
    @JsonBackReference
    private Workflow workflow;

}