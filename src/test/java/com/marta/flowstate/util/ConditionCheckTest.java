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

    @Test
    void evaluateSupportsBooleanLiterals() {
        Map<String, Object> data = Map.of("age", 20);

        assertTrue(conditionCheck.evaluate("true", data));
        assertTrue(conditionCheck.evaluate("TRUE", data));
        assertFalse(conditionCheck.evaluate("false", data));
        assertTrue(conditionCheck.evaluate("false or age >= 18", data));
        assertFalse(conditionCheck.evaluate("true and age < 18", data));
    }

    @Test
    void evaluateReadsNestedValuesWithDottedPaths() {
        Map<String, Object> data = Map.of(
                "validacion", Map.of("status", 200, "body", Map.of("ok", true, "score", 87)),
                "legacy.key", "x"
        );

        assertTrue(conditionCheck.evaluate("validacion.body.ok == true", data));
        assertTrue(conditionCheck.evaluate("validacion.status >= 200 and validacion.body.score > 80", data));
        assertFalse(conditionCheck.evaluate("validacion.body.missing == true", data));
        assertTrue(conditionCheck.evaluate("legacy.key == 'x'", data));
    }
}
