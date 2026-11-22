package com.marta.flowstate.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marta.flowstate.exception.ActionFailedException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FakeLlmClientTest {

    private final FakeLlmClient client = new FakeLlmClient(new ObjectMapper());

    private static LlmRequest request(String prompt, Map<String, Object> config) {
        return new LlmRequest("modelo", null, prompt, null, 100, Duration.ofSeconds(1), config);
    }

    @Test
    void firstMatchingRuleWinsAndDefaultIsUsedOtherwise() {
        Map<String, Object> config = Map.of(
                "fakeRules", List.of(
                        Map.of("contains", "caído", "response", Map.of("categoria", "urgente")),
                        Map.of("contains", "web", "response", Map.of("categoria", "normal"))),
                "fakeResponse", "sin regla");

        assertEquals("{\"categoria\":\"urgente\"}", client.complete(request("La web está caída... caído", config)).text());
        assertEquals("{\"categoria\":\"normal\"}", client.complete(request("La web va lenta", config)).text());
        assertEquals("sin regla", client.complete(request("Otra cosa", config)).text());
        assertEquals(3, client.calls());
    }

    @Test
    void estimatesTokensAndReportsStop() {
        LlmResponse response = client.complete(request("12345678", Map.of("fakeResponse", "1234")));

        assertEquals("modelo", response.model());
        assertEquals(2, response.inputTokens());
        assertEquals(1, response.outputTokens());
        assertEquals(LlmResponse.STOP, response.stopReason());
    }

    @Test
    void failsWithoutConfiguredResponse() {
        assertThrows(ActionFailedException.class, () -> client.complete(request("hola", Map.of())));
    }
}
