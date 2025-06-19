package com.marta.flowstate.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConditionCheck {

    private final ObjectMapper objectMapper;
    private final ExpressionParser parser;

    public ConditionCheck() {
        this.objectMapper = new ObjectMapper();
        this.parser = new SpelExpressionParser();
    }

    public boolean evaluate(String condition, String dataJson) {
        try {
            if (condition == null || condition.isBlank()) {
                return true;
            }

            Map<String, Object> dataMap = objectMapper.readValue(dataJson, new TypeReference<>() {});
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariables(dataMap);
            Boolean result = parser.parseExpression(condition).getValue(context, Boolean.class);

            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            return false;
        }
    }
}
