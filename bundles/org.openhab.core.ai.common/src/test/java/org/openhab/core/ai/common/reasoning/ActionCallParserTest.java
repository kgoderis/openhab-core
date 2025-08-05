package org.openhab.core.ai.common.reasoning;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.api.llm.LLMResponse;

/**
 * Unit tests for ActionCallParser
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ActionCallParserTest {

    private ActionCallParser actionCallParser;

    @BeforeEach
    void setUp() {
        actionCallParser = new ActionCallParser();
    }

    @Test
    void testParseJsonActionCalls() {
        // Given
        String jsonContent = """
                {
                    "reasoning": "I need to get the current temperature",
                    "action_calls": [
                        {
                            "id": "temp-1",
                            "action": "openhab.items.get",
                            "arguments": {
                                "itemName": "Temperature_Sensor"
                            }
                        }
                    ]
                }
                """;
        LLMResponse response = createMockLLMResponse(jsonContent);

        // When
        List<AIActionContext> actionCalls = actionCallParser.parseActionCalls(response, "test-session");

        // Then
        assertNotNull(actionCalls);
        assertEquals(1, actionCalls.size());

        AIActionContext actionCall = actionCalls.get(0);
        assertEquals("temp-1", actionCall.getCorrelationId());
        assertEquals("test-session", actionCall.getSessionId());
        assertEquals("a2a", actionCall.getProtocol());
        assertEquals("reasoning-engine", actionCall.getClientId());

        Map<String, Object> protocolContext = actionCall.getProtocolContext();
        assertEquals("openhab.items.get", protocolContext.get("action"));

        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) protocolContext.get("arguments");
        assertEquals("Temperature_Sensor", arguments.get("itemName"));
    }

    @Test
    void testParseRegexActionCalls() {
        // Given
        String content = "I need to call action_call(openhab.items.get, {itemName: \"Temperature_Sensor\"}) to get the temperature";
        LLMResponse response = createMockLLMResponse(content);

        // When
        List<AIActionContext> actionCalls = actionCallParser.parseActionCalls(response, "test-session");

        // Then
        assertNotNull(actionCalls);
        assertEquals(1, actionCalls.size());

        AIActionContext actionCall = actionCalls.get(0);
        assertTrue(actionCall.getCorrelationId().startsWith("action-"));
        assertEquals("test-session", actionCall.getSessionId());
        assertEquals("a2a", actionCall.getProtocol());
        assertEquals("reasoning-engine", actionCall.getClientId());

        Map<String, Object> protocolContext = actionCall.getProtocolContext();
        assertEquals("openhab.items.get", protocolContext.get("action"));

        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) protocolContext.get("arguments");
        assertEquals("Temperature_Sensor", arguments.get("itemName"));
    }

    @Test
    void testParseEmbeddedJsonActionCalls() {
        // Given
        String content = "I need to get the temperature. Here's the action call: {\"action\": \"openhab.items.get\", \"arguments\": {\"itemName\": \"Temperature_Sensor\"}}";
        LLMResponse response = createMockLLMResponse(content);

        // When
        List<AIActionContext> actionCalls = actionCallParser.parseActionCalls(response, "test-session");

        // Then
        assertNotNull(actionCalls);
        assertEquals(1, actionCalls.size());

        AIActionContext actionCall = actionCalls.get(0);
        assertTrue(actionCall.getCorrelationId().startsWith("action-"));
        assertEquals("test-session", actionCall.getSessionId());

        Map<String, Object> protocolContext = actionCall.getProtocolContext();
        assertEquals("openhab.items.get", protocolContext.get("action"));

        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) protocolContext.get("arguments");
        assertEquals("Temperature_Sensor", arguments.get("itemName"));
    }

    @Test
    void testParseMultipleActionCalls() {
        // Given
        String jsonContent = """
                {
                    "action_calls": [
                        {
                            "id": "temp-1",
                            "action": "openhab.items.get",
                            "arguments": {"itemName": "Temperature_Sensor"}
                        },
                        {
                            "id": "humidity-1",
                            "action": "openhab.items.get",
                            "arguments": {"itemName": "Humidity_Sensor"}
                        }
                    ]
                }
                """;
        LLMResponse response = createMockLLMResponse(jsonContent);

        // When
        List<AIActionContext> actionCalls = actionCallParser.parseActionCalls(response, "test-session");

        // Then
        assertNotNull(actionCalls);
        assertEquals(2, actionCalls.size());

        AIActionContext tempCall = actionCalls.get(0);
        AIActionContext humidityCall = actionCalls.get(1);

        assertEquals("temp-1", tempCall.getCorrelationId());
        assertEquals("humidity-1", humidityCall.getCorrelationId());

        Map<String, Object> tempContext = tempCall.getProtocolContext();
        Map<String, Object> humidityContext = humidityCall.getProtocolContext();

        assertEquals("openhab.items.get", tempContext.get("action"));
        assertEquals("openhab.items.get", humidityContext.get("action"));
    }

    @Test
    void testParseNoActionCalls() {
        // Given
        String content = "This is just some reasoning text without any action calls";
        LLMResponse response = createMockLLMResponse(content);

        // When
        List<AIActionContext> actionCalls = actionCallParser.parseActionCalls(response, "test-session");

        // Then
        assertNotNull(actionCalls);
        assertTrue(actionCalls.isEmpty());
    }

    @Test
    void testParseInvalidJson() {
        // Given
        String content = "This is invalid JSON: { invalid json content";
        LLMResponse response = createMockLLMResponse(content);

        // When
        List<AIActionContext> actionCalls = actionCallParser.parseActionCalls(response, "test-session");

        // Then
        assertNotNull(actionCalls);
        assertTrue(actionCalls.isEmpty());
    }

    @Test
    void testParseNullResponse() {
        // Given
        LLMResponse response = createMockLLMResponse(null);

        // When
        List<AIActionContext> actionCalls = actionCallParser.parseActionCalls(response, "test-session");

        // Then
        assertNotNull(actionCalls);
        assertTrue(actionCalls.isEmpty());
    }

    @Test
    void testParseEmptyResponse() {
        // Given
        LLMResponse response = createMockLLMResponse("");

        // When
        List<AIActionContext> actionCalls = actionCallParser.parseActionCalls(response, "test-session");

        // Then
        assertNotNull(actionCalls);
        assertTrue(actionCalls.isEmpty());
    }

    @Test
    void testValidateActionCall() {
        // Given
        AIActionContext validActionCall = AIActionContext.builder().protocol("a2a").clientId("test-client")
                .sessionId("test-session").correlationId("test-correlation")
                .protocolContext(Map.of("action", "test.action")).build();

        // When
        boolean isValid = actionCallParser.validateActionCall(validActionCall, null);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateActionCallMissingCorrelationId() {
        // Given
        AIActionContext invalidActionCall = AIActionContext.builder().protocol("a2a").clientId("test-client")
                .sessionId("test-session").correlationId("").protocolContext(Map.of("action", "test.action")).build();

        // When
        boolean isValid = actionCallParser.validateActionCall(invalidActionCall, null);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateActionCallMissingAction() {
        // Given
        AIActionContext invalidActionCall = AIActionContext.builder().protocol("a2a").clientId("test-client")
                .sessionId("test-session").correlationId("test-correlation").protocolContext(Map.of()).build();

        // When
        boolean isValid = actionCallParser.validateActionCall(invalidActionCall, null);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateActionCallEmptyAction() {
        // Given
        AIActionContext invalidActionCall = AIActionContext.builder().protocol("a2a").clientId("test-client")
                .sessionId("test-session").correlationId("test-correlation").protocolContext(Map.of("action", ""))
                .build();

        // When
        boolean isValid = actionCallParser.validateActionCall(invalidActionCall, null);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testPerformanceMetrics() {
        // Given
        String content = "action_call(test.action, {param: \"value\"})";
        LLMResponse response = createMockLLMResponse(content);

        // When
        actionCallParser.parseActionCalls(response, "test-session");
        ActionCallParser.PerformanceMetrics metrics = actionCallParser.getPerformanceMetrics();

        // Then
        assertNotNull(metrics);
        assertEquals(1, metrics.getTotalParsingAttempts());
        assertEquals(1, metrics.getSuccessfulRegexParses());
        assertEquals(0, metrics.getSuccessfulJsonParses());
        assertEquals(0, metrics.getFailedParses());
        assertEquals(1, metrics.getTotalActionCalls());
    }

    @Test
    void testPerformanceMetricsMultipleCalls() {
        // Given
        String content1 = "action_call(test1.action, {param1: \"value1\"})";
        String content2 = "action_call(test2.action, {param2: \"value2\"})";
        LLMResponse response1 = createMockLLMResponse(content1);
        LLMResponse response2 = createMockLLMResponse(content2);

        // When
        actionCallParser.parseActionCalls(response1, "session-1");
        actionCallParser.parseActionCalls(response2, "session-2");
        ActionCallParser.PerformanceMetrics metrics = actionCallParser.getPerformanceMetrics();

        // Then
        assertNotNull(metrics);
        assertEquals(2, metrics.getTotalParsingAttempts());
        assertEquals(2, metrics.getSuccessfulRegexParses());
        assertEquals(0, metrics.getSuccessfulJsonParses());
        assertEquals(0, metrics.getFailedParses());
        assertEquals(2, metrics.getTotalActionCalls());
    }

    @Test
    void testPerformanceMetricsBuilder() {
        // Given
        ActionCallParser.PerformanceMetrics.Builder builder = ActionCallParser.PerformanceMetrics.builder();

        // When
        ActionCallParser.PerformanceMetrics metrics = builder.totalParsingAttempts(10).successfulJsonParses(5)
                .successfulRegexParses(3).failedParses(2).totalActionCalls(8).build();

        // Then
        assertNotNull(metrics);
        assertEquals(10, metrics.getTotalParsingAttempts());
        assertEquals(5, metrics.getSuccessfulJsonParses());
        assertEquals(3, metrics.getSuccessfulRegexParses());
        assertEquals(2, metrics.getFailedParses());
        assertEquals(8, metrics.getTotalActionCalls());
    }

    private LLMResponse createMockLLMResponse(String content) {
        return LLMResponse.builder().content(content != null ? content : "").modelName("test-model")
                .providerType("test-provider").promptTokens(50).completionTokens(50).totalTokens(100).cost(0.0)
                .responseTimeMs(100).build();
    }
}
