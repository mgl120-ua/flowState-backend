package com.marta.flowstate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marta.flowstate.exception.ActionFailedException;
import com.marta.flowstate.model.Company;
import com.marta.flowstate.service.llm.LlmClient;
import com.marta.flowstate.service.llm.LlmProperties;
import com.marta.flowstate.service.llm.LlmRequest;
import com.marta.flowstate.service.llm.LlmResponse;
import com.marta.flowstate.service.llm.LlmUsage;
import com.marta.flowstate.util.JsonSchemaValidator;
import com.marta.flowstate.util.TemplateRenderer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Acción LLM: envía un prompt con plantillas {{clave}} a un modelo de lenguaje y guarda la respuesta.
 *
 * Configuración: {"model", "system", "prompt", "outputSchema", "maxTokens", "timeoutMs", "saveAs"}.
 * Con outputSchema la respuesta debe ser un JSON que lo cumpla. Si hay saveAs, se guarda
 * {"status": "ok", "body": respuesta} en los datos de la instancia, igual que la acción HTTP.
 * Antes de llamar al modelo se comprueba el límite mensual de tokens de la empresa de la acción.
 */
@Component
public class LlmActionHandler {

    static final int DEFAULT_MAX_TOKENS = 1024;
    static final int MAX_MAX_TOKENS = 8192;
    static final int DEFAULT_TIMEOUT_MS = 120_000;
    static final int MAX_TIMEOUT_MS = 300_000;
    //El prompt se guarda en la traza para auditar, recortado porque puede llevar datos de la instancia
    static final int MAX_TRACE_PROMPT_CHARS = 8 * 1024;
    private static final BigDecimal MILLION = BigDecimal.valueOf(1_000_000);

    private final LlmClient llmClient;
    private final LlmProperties properties;
    private final LlmUsageService llmUsageService;
    private final TemplateRenderer templateRenderer;
    private final ObjectMapper objectMapper;

    public LlmActionHandler(LlmClient llmClient, LlmProperties properties, LlmUsageService llmUsageService,
                            TemplateRenderer templateRenderer, ObjectMapper objectMapper) {
        this.llmClient = llmClient;
        this.properties = properties;
        this.llmUsageService = llmUsageService;
        this.templateRenderer = templateRenderer;
        this.objectMapper = objectMapper;
    }

    public void validateConfig(Map<String, Object> config) {
        if (!(config.get("prompt") instanceof String prompt) || prompt.isBlank()) {
            throw new IllegalArgumentException("La configuración necesita el campo 'prompt' como texto");
        }
        optionalText(config, "model");
        optionalText(config, "system");
        if (config.get("outputSchema") != null) {
            JsonSchemaValidator.checkSchema(config.get("outputSchema"));
        }
        maxTokens(config);
        timeoutMs(config);
        Object saveAs = config.get("saveAs");
        if (saveAs != null && !(saveAs instanceof String s && HttpActionHandler.SAVE_AS.matcher(s).matches())) {
            throw new IllegalArgumentException("El campo 'saveAs' debe ser un nombre de clave (letras, números y _)");
        }
    }

    @SuppressWarnings("unchecked")
    public ActionResult execute(Map<String, Object> config, Map<String, Object> data, Company company) {
        String model = config.get("model") instanceof String m && !m.isBlank() ? m : properties.defaultModel();
        String system = config.get("system") instanceof String s ? templateRenderer.render(s, data) : null;
        String prompt = templateRenderer.render((String) config.get("prompt"), data);
        Map<String, Object> schema = (Map<String, Object>) config.get("outputSchema");
        int maxTokens = maxTokens(config);
        String summary = "LLM " + model;

        LlmRequest request = new LlmRequest(model, system, prompt, schema, maxTokens,
                Duration.ofMillis(timeoutMs(config)), config);
        LlmResponse response;
        try {
            llmUsageService.checkMonthlyLimit(company);
            response = llmClient.complete(request);
        } catch (ActionFailedException e) {
            throw new ActionFailedException(e.getMessage(), summary, usage(model, prompt, null));
        }
        LlmUsage usage = usage(model, prompt, response);

        if (LlmResponse.LENGTH.equals(response.stopReason())) {
            throw new ActionFailedException("La respuesta del modelo se cortó al llegar a maxTokens (" + maxTokens + ")", summary, usage);
        }
        Object body = schema == null ? response.text() : parseAndValidate(response.text(), schema, summary, usage);

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("status", "ok");
        output.put("body", body);
        return new ActionResult(null, summary, (String) config.get("saveAs"), output, usage);
    }

    private Object parseAndValidate(String text, Map<String, Object> schema, String summary, LlmUsage usage) {
        Object value;
        try {
            value = objectMapper.readValue(text, Object.class);
        } catch (IOException e) {
            throw new ActionFailedException("La respuesta del modelo no es un JSON válido", summary, usage);
        }
        List<String> errors = JsonSchemaValidator.validate(value, schema);
        if (!errors.isEmpty()) {
            throw new ActionFailedException("La respuesta del modelo no cumple el esquema: " + String.join("; ", errors), summary, usage);
        }
        return value;
    }

    private LlmUsage usage(String requestedModel, String prompt, LlmResponse response) {
        String tracedPrompt = prompt.length() > MAX_TRACE_PROMPT_CHARS ? prompt.substring(0, MAX_TRACE_PROMPT_CHARS) : prompt;
        if (response == null) {
            return new LlmUsage(requestedModel, null, null, null, tracedPrompt, null);
        }
        String model = response.model() != null ? response.model() : requestedModel;
        return new LlmUsage(model, response.inputTokens(), response.outputTokens(),
                cost(model, response.inputTokens(), response.outputTokens()), tracedPrompt, response.stopReason());
    }

    //USD según la tabla de precios; un modelo que no está en la tabla es local y cuesta 0
    BigDecimal cost(String model, Integer inputTokens, Integer outputTokens) {
        LlmProperties.Price price = properties.prices().get(model);
        if (price == null) return BigDecimal.ZERO.setScale(6);
        BigDecimal input = price.input() == null ? BigDecimal.ZERO
                : price.input().multiply(BigDecimal.valueOf(inputTokens == null ? 0 : inputTokens));
        BigDecimal output = price.output() == null ? BigDecimal.ZERO
                : price.output().multiply(BigDecimal.valueOf(outputTokens == null ? 0 : outputTokens));
        return input.add(output).divide(MILLION, 6, RoundingMode.HALF_UP);
    }

    private static void optionalText(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value != null && !(value instanceof String s && !s.isBlank())) {
            throw new IllegalArgumentException("El campo '" + key + "' debe ser un texto no vacío");
        }
    }

    private static int maxTokens(Map<String, Object> config) {
        Object value = config.get("maxTokens");
        if (value == null) return DEFAULT_MAX_TOKENS;
        if (!(value instanceof Number n) || n.intValue() < 1 || n.intValue() > MAX_MAX_TOKENS) {
            throw new IllegalArgumentException("'maxTokens' debe estar entre 1 y " + MAX_MAX_TOKENS);
        }
        return n.intValue();
    }

    private static int timeoutMs(Map<String, Object> config) {
        Object value = config.get("timeoutMs");
        if (value == null) return DEFAULT_TIMEOUT_MS;
        if (!(value instanceof Number n) || n.intValue() < 1 || n.intValue() > MAX_TIMEOUT_MS) {
            throw new IllegalArgumentException("'timeoutMs' debe estar entre 1 y " + MAX_TIMEOUT_MS);
        }
        return n.intValue();
    }
}
