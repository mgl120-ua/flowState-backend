package com.marta.flowstate.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ActionExecutorFactory {

    private final Map<String, ActionExecutor> executors = new HashMap<>();

    @Autowired
    public ActionExecutorFactory(List<ActionExecutor> executorList) {
        for (ActionExecutor executor : executorList) {
            String key = executor.getClass().getAnnotation(Component.class).value();
            executors.put(key.toUpperCase(), executor);
        }
    }

    public ActionExecutor getExecutor(String type) {
        return executors.get(type.toUpperCase());
    }
}
