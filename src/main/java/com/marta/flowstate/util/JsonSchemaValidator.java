package com.marta.flowstate.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validador de un subconjunto de JSON Schema, suficiente para la salida estructurada de un LLM:
 * type, properties, required, additionalProperties, items, enum, minimum y maximum.
 * Las palabras clave que no conoce se ignoran.
 */
public final class JsonSchemaValidator {

    private static final Set<String> TYPES = Set.of("object", "array", "string", "number", "integer", "boolean", "null");

    private JsonSchemaValidator() {
    }

    /**
     * Comprueba que el esquema tiene una forma que este validador entiende.
     * Lanza IllegalArgumentException con el motivo si no.
     */
    public static void checkSchema(Object schema) {
        checkSchema(schema, "$");
    }

    /**
     * Valida un valor (mapas, listas, textos, números... tal como los deja Jackson) contra el esquema.
     * Devuelve los errores encontrados; vacía si el valor es válido.
     */
    public static List<String> validate(Object value, Map<String, Object> schema) {
        List<String> errors = new ArrayList<>();
        validate(value, schema, "$", errors);
        return errors;
    }

    private static void checkSchema(Object schema, String path) {
        if (!(schema instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("El esquema en " + path + " debe ser un objeto");
        }
        Object type = map.get("type");
        if (type != null) {
            List<?> types = type instanceof List<?> list ? list : List.of(type);
            for (Object t : types) {
                if (!(t instanceof String s) || !TYPES.contains(s)) {
                    throw new IllegalArgumentException("Tipo no soportado en " + path + ": " + t);
                }
            }
        }
        if (map.containsKey("properties")) {
            if (!(map.get("properties") instanceof Map<?, ?> properties)) {
                throw new IllegalArgumentException("'properties' en " + path + " debe ser un objeto");
            }
            for (Map.Entry<?, ?> entry : properties.entrySet()) {
                checkSchema(entry.getValue(), path + "." + entry.getKey());
            }
        }
        if (map.containsKey("required")) {
            if (!(map.get("required") instanceof List<?> required) || !required.stream().allMatch(String.class::isInstance)) {
                throw new IllegalArgumentException("'required' en " + path + " debe ser una lista de nombres");
            }
        }
        Object additional = map.get("additionalProperties");
        if (additional != null && !(additional instanceof Boolean)) {
            checkSchema(additional, path + ".additionalProperties");
        }
        if (map.containsKey("items")) {
            checkSchema(map.get("items"), path + "[]");
        }
        if (map.containsKey("enum") && !(map.get("enum") instanceof List<?> values && !values.isEmpty())) {
            throw new IllegalArgumentException("'enum' en " + path + " debe ser una lista no vacía");
        }
        for (String key : List.of("minimum", "maximum")) {
            if (map.containsKey(key) && !(map.get(key) instanceof Number)) {
                throw new IllegalArgumentException("'" + key + "' en " + path + " debe ser un número");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void validate(Object value, Map<String, Object> schema, String path, List<String> errors) {
        Object type = schema.get("type");
        if (type != null) {
            List<?> types = type instanceof List<?> list ? list : List.of(type);
            if (types.stream().noneMatch(t -> hasType(value, (String) t))) {
                errors.add(path + ": se esperaba " + String.join(" o ", (List<String>) types) + " y llegó " + typeName(value));
                return;
            }
        }
        if (schema.get("enum") instanceof List<?> values && values.stream().noneMatch(v -> sameValue(v, value))) {
            errors.add(path + ": valor " + value + " no está entre " + values);
        }
        if (value instanceof Number number) {
            if (schema.get("minimum") instanceof Number min && compare(number, min) < 0) {
                errors.add(path + ": " + number + " es menor que " + min);
            }
            if (schema.get("maximum") instanceof Number max && compare(number, max) > 0) {
                errors.add(path + ": " + number + " es mayor que " + max);
            }
        }
        if (value instanceof Map<?, ?> object) {
            Map<String, Object> properties = schema.get("properties") instanceof Map<?, ?> p
                    ? (Map<String, Object>) p : Map.of();
            if (schema.get("required") instanceof List<?> required) {
                for (Object name : required) {
                    if (!object.containsKey(name)) errors.add(path + ": falta el campo '" + name + "'");
                }
            }
            Object additional = schema.get("additionalProperties");
            for (Map.Entry<?, ?> entry : object.entrySet()) {
                String key = String.valueOf(entry.getKey());
                String childPath = path + "." + key;
                if (properties.get(key) instanceof Map<?, ?> child) {
                    validate(entry.getValue(), (Map<String, Object>) child, childPath, errors);
                } else if (Boolean.FALSE.equals(additional)) {
                    errors.add(path + ": campo no permitido '" + key + "'");
                } else if (additional instanceof Map<?, ?> child) {
                    validate(entry.getValue(), (Map<String, Object>) child, childPath, errors);
                }
            }
        }
        if (value instanceof List<?> list && schema.get("items") instanceof Map<?, ?> items) {
            for (int i = 0; i < list.size(); i++) {
                validate(list.get(i), (Map<String, Object>) items, path + "[" + i + "]", errors);
            }
        }
    }

    private static boolean hasType(Object value, String type) {
        return switch (type) {
            case "object" -> value instanceof Map<?, ?>;
            case "array" -> value instanceof List<?>;
            case "string" -> value instanceof String;
            case "number" -> value instanceof Number;
            case "integer" -> value instanceof Number n && isIntegral(n);
            case "boolean" -> value instanceof Boolean;
            case "null" -> value == null;
            default -> false;
        };
    }

    private static boolean isIntegral(Number n) {
        try {
            return new BigDecimal(n.toString()).stripTrailingZeros().scale() <= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean sameValue(Object expected, Object actual) {
        if (expected instanceof Number a && actual instanceof Number b) return compare(a, b) == 0;
        return expected == null ? actual == null : expected.equals(actual);
    }

    private static int compare(Number a, Number b) {
        return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString()));
    }

    private static String typeName(Object value) {
        if (value == null) return "null";
        if (value instanceof Map<?, ?>) return "object";
        if (value instanceof List<?>) return "array";
        if (value instanceof String) return "string";
        if (value instanceof Boolean) return "boolean";
        if (value instanceof Number) return "number";
        return value.getClass().getSimpleName();
    }
}
