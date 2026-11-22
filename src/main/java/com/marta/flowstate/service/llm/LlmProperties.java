package com.marta.flowstate.service.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Configuración de flowstate.llm.*
 *
 * @param provider      ollama (modelo local) o fake (respuestas fijas, para tests)
 * @param defaultModel  modelo si la acción no indica uno
 * @param ollamaUrl     URL base de Ollama
 * @param prices        USD por millón de tokens por modelo; un modelo que no está aquí es local y cuesta 0
 */
@ConfigurationProperties(prefix = "flowstate.llm")
public record LlmProperties(String provider, String defaultModel, String ollamaUrl, Map<String, Price> prices) {

    public LlmProperties {
        if (provider == null || provider.isBlank()) provider = "ollama";
        if (defaultModel == null || defaultModel.isBlank()) defaultModel = "qwen3.5:4b";
        if (ollamaUrl == null || ollamaUrl.isBlank()) ollamaUrl = "http://localhost:11434";
        prices = prices == null ? Map.of() : Map.copyOf(prices);
    }

    public record Price(BigDecimal input, BigDecimal output) {
    }
}
