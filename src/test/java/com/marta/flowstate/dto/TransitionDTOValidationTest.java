package com.marta.flowstate.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TransitionDTOValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void transitionWithoutActionIsInvalid() {
        TransitionDTO dto = validDto();
        dto.setAction(" ");

        Set<ConstraintViolation<TransitionDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("action", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void transitionWithActionIsValid() {
        assertTrue(validator.validate(validDto()).isEmpty());
    }

    private TransitionDTO validDto() {
        TransitionDTO dto = new TransitionDTO();
        dto.setAction("Aprobar");
        dto.setSourceStateId(1L);
        dto.setTargetStateId(2L);
        return dto;
    }
}
