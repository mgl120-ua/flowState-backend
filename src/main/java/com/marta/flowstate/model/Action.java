package com.marta.flowstate.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "action")
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; //email,webhook,etc.
    @Column(columnDefinition = "jsonb")
    private String config; //configuración específica
}
