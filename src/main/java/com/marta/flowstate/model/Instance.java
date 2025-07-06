package com.marta.flowstate.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import jakarta.persistence.Convert;
import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "instance")
public class Instance implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Type(JsonType.class)
    @Column(name = "data", columnDefinition = "json")
    private Map<String, Object> data;

    @Column(name = "date", nullable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "state_id", nullable = false)
    private State state;

    @ManyToOne
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;
}