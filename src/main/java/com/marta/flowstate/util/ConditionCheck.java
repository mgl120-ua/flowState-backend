package com.marta.flowstate.util;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConditionCheck {

    public boolean evaluate(String condition, Map<String, Object> dataMap) {
        if (condition == null || condition.isBlank()) return true;

        String[] orParts = condition.split("(?i)\\s+or\\s+");

        for (String orBlock : orParts) {
            boolean andResult = true;
            String[] andParts = orBlock.split("(?i)\\s+and\\s+");

            for (String part : andParts) {
                part = part.trim();

                try {
                    if (part.contains(">=")) {
                        andResult &= compareNumber(part, dataMap, ">=");
                    } else if (part.contains("<=")) {
                        andResult &= compareNumber(part, dataMap, "<=");
                    } else if (part.contains("!=")) {
                        andResult &= compareString(part, dataMap, "!=");
                    } else if (part.contains("==")) {
                        andResult &= compareString(part, dataMap, "==");
                    } else if (part.contains(">")) {
                        andResult &= compareNumber(part, dataMap, ">");
                    } else if (part.contains("<")) {
                        andResult &= compareNumber(part, dataMap, "<");
                    } else {
                        System.out.println("Operador no soportado: " + part);
                        andResult = false;
                    }
                } catch (Exception e) {
                    System.out.println("Error evaluando parte: " + part);
                    e.printStackTrace();
                    andResult = false;
                }

                if (!andResult) break;
            }

            if (andResult) return true;
        }

        return false;
    }

    private boolean compareNumber(String part, Map<String, Object> data, String operator) {
        String[] tokens = part.split(operator);
        String key = tokens[0].trim();
        double expected = Double.parseDouble(tokens[1].trim());
        Object value = data.get(key);

        if (!(value instanceof Number)) return false;
        double actual = ((Number) value).doubleValue();

        return switch (operator) {
            case ">=" -> actual >= expected;
            case "<=" -> actual <= expected;
            case ">" -> actual > expected;
            case "<" -> actual < expected;
            default -> false;
        };
    }

    private boolean compareString(String part, Map<String, Object> data, String operator) {
        String[] tokens = part.split(operator);
        String key = tokens[0].trim();
        String expected = tokens[1].trim().replace("\"", "").replace("'", "");
        Object value = data.get(key);

        if (value == null) return false;

        return switch (operator) {
            case "==" -> expected.equals(value.toString());
            case "!=" -> !expected.equals(value.toString());
            default -> false;
        };
    }
}
