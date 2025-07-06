package com.marta.flowstate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marta.flowstate.model.Action;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActionExecutorService {

    private final MailService mailService;
    private final ObjectMapper objectMapper;

    public void execute(Action action, Map<String, Object> instanceData) {
        try {
            Map<String, Object> config = objectMapper.readValue(action.getConfig(), Map.class);
            String type = action.getType();

            switch (type.toUpperCase()) {
                case "EMAIL" -> handleEmail(config, instanceData);
                default -> throw new IllegalArgumentException("La accion " + type + "no esta implementada");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error ejecutando la accion", e);
        }
    }

    private void handleEmail(Map<String, Object> config, Map<String, Object> instanceData) {
        String to = (String) config.get("to");
        String subject = (String) config.get("subject");
        String body = (String) config.get("body");

        Pattern pattern = Pattern.compile("\\{\\{(\\w+)}}");
        Matcher matcher = pattern.matcher(body);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = instanceData.getOrDefault(key, "");
            matcher.appendReplacement(result, value.toString());
        }
        matcher.appendTail(result);

        mailService.sendEmail(to, subject, result.toString());
    }
}
