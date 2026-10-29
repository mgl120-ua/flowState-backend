package com.marta.flowstate.dto;

import com.marta.flowstate.model.TransitionAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransitionActionResponseDTO {
    private Long actionId;
    private String name;
    private String type;
    private Integer position;

    public static TransitionActionResponseDTO from(TransitionAction ta) {
        return new TransitionActionResponseDTO(
                ta.getAction().getId(),
                ta.getAction().getName(),
                ta.getAction().getType(),
                ta.getPosition()
        );
    }
}
