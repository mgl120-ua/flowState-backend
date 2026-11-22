package com.marta.flowstate.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marta.flowstate.service.llm.FakeLlmClient;
import com.marta.flowstate.service.llm.LlmClient;
import com.marta.flowstate.service.llm.LlmProperties;
import com.marta.flowstate.service.llm.OllamaLlmClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
@EnableConfigurationProperties(LlmProperties.class)
public class LlmConfig {

    @Bean
    public LlmClient llmClient(LlmProperties properties, ObjectMapper objectMapper) {
        return switch (properties.provider().toLowerCase(Locale.ROOT)) {
            case "ollama" -> new OllamaLlmClient(properties.ollamaUrl(), objectMapper);
            case "fake" -> new FakeLlmClient(objectMapper);
            default -> throw new IllegalStateException("flowstate.llm.provider no válido: " + properties.provider()
                    + " (usa ollama o fake)");
        };
    }
}
