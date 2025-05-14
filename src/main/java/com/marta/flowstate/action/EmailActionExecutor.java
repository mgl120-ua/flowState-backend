package com.marta.flowstate.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component("EMAIL")
public class EmailActionExecutor implements ActionExecutor {

    @Autowired
    private JavaMailSender mailSender;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(String configJson) {
        try {
            JsonNode config = objectMapper.readTree(configJson);
            String to = config.get("to").asText();
            String subject = config.get("subject").asText();
            String body = config.get("body").asText();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("no-reply@flowstate.com");
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Error enviando email:");
            e.printStackTrace();
        }
    }
}
