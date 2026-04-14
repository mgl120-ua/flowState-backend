package com.marta.flowstate.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConditionCheckTest {

    private final ConditionCheck conditionCheck = new ConditionCheck();

    @Test
    void evaluateSupportsNumericComparisonsAndAndOr() {
        Map<String, Object> data = Map.of(
                "age", 20,
                "score", 95,
                "status", "active"
        );

        assertTrue(conditionCheck.evaluate("age >= 18 and score > 90", data));
        assertTrue(conditionCheck.evaluate("status == 'active' or age < 18", data));
        assertFalse(conditionCheck.evaluate("age < 18 and status == 'active'", data));
    }

    @Test
    void evaluateSupportsStringComparisonsWithQuotes() {
        Map<String, Object> data = Map.of(
                "status", "active",
                "role", "ADMIN"
        );

        assertTrue(conditionCheck.evaluate("status == 'active'", data));
        assertTrue(conditionCheck.evaluate("role == \"ADMIN\"", data));
        assertFalse(conditionCheck.evaluate("status != 'active'", data));
    }

    @Test
    void evaluateReturnsFalseForInvalidConditions() {
        Map<String, Object> data = Map.of("age", 20);

        assertFalse(conditionCheck.evaluate("age >> 18", data));
        assertFalse(conditionCheck.evaluate("unknown == 'x'", data));
    }
}
