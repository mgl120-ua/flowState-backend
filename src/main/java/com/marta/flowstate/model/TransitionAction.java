package com.marta.flowstate.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.io.Serializable;
import com.marta.flowstate.model.Action;
import com.marta.flowstate.model.Transition;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transition_action")
public class TransitionAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "action_id")
    private Action action;
    @ManyToOne
    @JoinColumn(name = "transition_id")
    private Transition transition;
}