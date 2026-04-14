package com.marta.flowstate.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ConditionCheck {

    private static final Logger logger = LoggerFactory.getLogger(ConditionCheck.class);
    private static final Pattern CONDITION_PATTERN = Pattern.compile("^\\s*([a-zA-Z0-9_.-]+)\\s*(>=|<=|!=|==|>|<)\\s*(\"[^\"]*\"|'[^']*'|[^\\s].*?)\\s*$");

    /**
     * Evalúa condiciones simples usando operadores controlados.
     *
     * Operadores soportados:
     * - Comparaciones numéricas: >, <, >=, <=
     * - Comparaciones de cadenas: ==, !=
     * - Operadores lógicos: AND, OR (case insensitive)
     * - Cadenas entre comillas simples o dobles
     *
     * Ejemplo: age >= 18 and status == 'active'
     */
    public boolean evaluate(String condition, Map<String, Object> dataMap) {
        if (condition == null || condition.isBlank()) return true;

        String[] orParts = condition.split("(?i)\\s+or\\s+");

        for (String orBlock : orParts) {
            boolean andResult = true;
            String[] andParts = orBlock.split("(?i)\\s+and\\s+");

            for (String part : andParts) {
                part = part.trim();
                if (part.isEmpty()) {
                    logger.warn("Parte de condición vacía encontrada en: {}", condition);
                    andResult = false;
                    break;
                }

                if (!evaluateExpression(part, dataMap)) {
                    andResult = false;
                    break;
                }
            }

            if (andResult) {
                return true;
            }
        }

        return false;
    }

    private boolean evaluateExpression(String expression, Map<String, Object> dataMap) {
        try {
            Matcher matcher = CONDITION_PATTERN.matcher(expression);
            if (!matcher.matches()) {
                logger.warn("Condición inválida o no soportada: {}", expression);
                return false;
            }

            String key = matcher.group(1);
            String operator = matcher.group(2);
            String rawValue = matcher.group(3).trim();
            String expectedValue = unquote(rawValue);
            Object actualValue = dataMap.get(key);

            if (actualValue == null) {
                logger.debug("Clave no encontrada en datos: {}", key);
                return false;
            }

            if (operator.equals("==") || operator.equals("!=")) {
                return compareString(actualValue, expectedValue, operator);
            }

            return compareNumber(actualValue, expectedValue, operator);
        } catch (Exception e) {
            logger.error("Error evaluando condición: {}", expression, e);
            return false;
        }
    }

    private String unquote(String value) {
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private boolean compareNumber(Object actualValue, String expectedValue, String operator) {
        double actual = parseNumber(actualValue);
        double expected = Double.parseDouble(expectedValue);

        return switch (operator) {
            case ">=" -> actual >= expected;
            case "<=" -> actual <= expected;
            case ">" -> actual > expected;
            case "<" -> actual < expected;
            default -> false;
        };
    }

    private double parseNumber(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            return Double.parseDouble(((String) value).trim());
        }
        throw new IllegalArgumentException("Valor no numérico: " + value);
    }

    private boolean compareString(Object actualValue, String expectedValue, String operator) {
        String actualString = actualValue.toString();

        return switch (operator) {
            case "==" -> expectedValue.equals(actualString);
            case "!=" -> !expectedValue.equals(actualString);
            default -> false;
        };
    }
}
