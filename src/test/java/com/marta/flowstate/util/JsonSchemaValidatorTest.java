package com.marta.flowstate.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonSchemaValidatorTest {

    private static final Map<String, Object> TRIAJE = Map.of(
            "type", "object",
            "properties", Map.of(
                    "categoria", Map.of("type", "string", "enum", List.of("urgente", "normal")),
                    "confianza", Map.of("type", "number", "minimum", 0, "maximum", 1),
                    "etiquetas", Map.of("type", "array", "items", Map.of("type", "string"))),
            "required", List.of("categoria", "confianza"),
            "additionalProperties", false);

    @Test
    void acceptsValidObject() {
        Map<String, Object> value = Map.of("categoria", "urgente", "confianza", 0.9, "etiquetas", List.of("red"));

        assertTrue(JsonSchemaValidator.validate(value, TRIAJE).isEmpty());
    }

    @Test
    void reportsEveryProblemWithItsPath() {
        Map<String, Object> value = Map.of("categoria", "otra", "confianza", 1.5, "etiquetas", List.of(3), "extra", true);

        List<String> errors = JsonSchemaValidator.validate(value, TRIAJE);

        assertEquals(4, errors.size(), errors.toString());
        assertTrue(errors.stream().anyMatch(e -> e.startsWith("$.categoria")));
        assertTrue(errors.stream().anyMatch(e -> e.startsWith("$.confianza")));
        assertTrue(errors.stream().anyMatch(e -> e.startsWith("$.etiquetas[0]")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("'extra'")));
    }

    @Test
    void reportsMissingRequiredFieldAndWrongRootType() {
        assertEquals(List.of("$: falta el campo 'confianza'"),
                JsonSchemaValidator.validate(Map.of("categoria", "normal"), TRIAJE));
        assertEquals(1, JsonSchemaValidator.validate("texto", TRIAJE).size());
    }

    @Test
    void integerAcceptsWholeNumbersOnly() {
        Map<String, Object> schema = Map.of("type", "integer");

        assertTrue(JsonSchemaValidator.validate(3, schema).isEmpty());
        assertTrue(JsonSchemaValidator.validate(3.0, schema).isEmpty());
        assertFalse(JsonSchemaValidator.validate(3.5, schema).isEmpty());
    }

    @Test
    void checkSchemaRejectsUnsupportedShapes() {
        assertDoesNotThrow(() -> JsonSchemaValidator.checkSchema(TRIAJE));
        assertThrows(IllegalArgumentException.class, () -> JsonSchemaValidator.checkSchema("object"));
        assertThrows(IllegalArgumentException.class, () -> JsonSchemaValidator.checkSchema(Map.of("type", "fecha")));
        assertThrows(IllegalArgumentException.class, () -> JsonSchemaValidator.checkSchema(Map.of("required", "a")));
        assertThrows(IllegalArgumentException.class,
                () -> JsonSchemaValidator.checkSchema(Map.of("properties", Map.of("a", Map.of("type", 1)))));
    }
}
