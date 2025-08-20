/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.reasoning;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.action.api.ActionKeys;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.openhab.core.ai.common.metrics.ModelPerformanceMetrics;
import org.openhab.core.ai.common.response.ModelResponse;
import org.openhab.core.ai.model.ModelResponseActionParser;

/**
 * Unit tests for ActionCallParser
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ActionCallParserTest {

    private ModelResponseActionParser actionCallParser;

    @BeforeEach
    void setUp() {
        actionCallParser = new ModelResponseActionParser();
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
        ModelResponse response = createMockModelResponse(jsonContent);

        // When
        List<ExecutionContext> actionCalls = actionCallParser.parseActionCalls(response, "test-session");

        // Then
        assertNotNull(actionCalls);
        assertEquals(1, actionCalls.size());

        ExecutionContext actionCall = actionCalls.get(0);
        assertEquals("temp-1", actionCall.getCorrelationId());
        assertEquals("test-session", actionCall.getSessionId());
        assertEquals("a2a", actionCall.getProtocol());
        assertEquals("reasoning-engine", actionCall.getClientId());

        String actionName = actionCall.getValue(ActionKeys.ACTION_NAME.getKey(), String.class);
        assertEquals("openhab.items.get", actionName);

        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) protocolContext.get("arguments");
        assertEquals("Temperature_Sensor", arguments.get("itemName"));
    }

    @Test
    void testParseRegexActionCalls() {
        // Given
        String content = "I need to call action_call(openhab.items.get, {itemName: \"Temperature_Sensor\"}) to get the temperature";
        ModelResponse response = createMockModelResponse(content);

        // When
        List<ExecutionContext> actionCalls = actionCallParser.parseActionCalls(response, "test-session");

        // Then
        assertNotNull(actionCalls);
        assertEquals(1, actionCalls.size());

        ExecutionContext actionCall = actionCalls.get(0);
        assertTrue(actionCall.getCorrelationId().startsWith("action-"));
        assertEquals("test-session", actionCall.getSessionId());
        assertEquals("a2a", actionCall.getProtocol());
        assertEquals("reasoning-engine", actionCall.getClientId());

        String actionName = actionCall.getValue(ActionKeys.ACTION_NAME.getKey(), String.class);
        assertEquals("openhab.items.get", actionName);

        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = actionCall.getValue(ActionKeys.ARGUMENTS.getKey(), Map.class);
        assertEquals("Temperature_Sensor", arguments.get("itemName"));
    }

    @Test
    void testParseEmbeddedJsonActionCalls() {
        // Given
        String content = "I need to get the temperature. Here's the action call: {\"action\": \"openhab.items.get\", \"arguments\": {\"itemName\": \"Temperature_Sensor\"}}";
        ModelResponse response = createMockModelResponse(content);

        // When
        List<ExecutionContext> actionCalls = actionCallParser.parseActionCalls(response, "test-session");

        // Then
        assertNotNull(actionCalls);
        assertEquals(1, actionCalls.size());

        ExecutionContext actionCall = actionCalls.get(0);
        assertTrue(actionCall.getCorrelationId().startsWith("action-"));
        assertEquals("test-session", actionCall.getSessionId());

        String actionName = actionCall.getValue(ActionKeys.ACTION_NAME.getKey(), String.class);
        assertEquals("openhab.items.get", actionName);

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
        ModelResponse response = createMockModelResponse(jsonContent);

        // When
        List<ExecutionContext> actionCalls = actionCallParser.parseActionCalls(response, "test-session");

        // Then
        assertNotNull(actionCalls);
        assertEquals(2, actionCalls.size());

        ExecutionContext tempCall = actionCalls.get(0);
        ExecutionContext humidityCall = actionCalls.get(1);

        assertEquals("temp-1", tempCall.getCorrelationId());
        assertEquals("humidity-1", humidityCall.getCorrelationId());

        String tempAction = tempCall.getValue(ActionKeys.ACTION_NAME.getKey(), String.class);
        String humidityAction = humidityCall.getValue(ActionKeys.ACTION_NAME.getKey(), String.class);

        assertEquals("openhab.items.get", tempAction);
        assertEquals("openhab.items.get", humidityAction);
    }

    @Test
    void testParseNoActionCalls() {
        // Given
        String content = "This is just some reasoning text without any action calls";
        ModelResponse response = createMockModelResponse(content);

        // When
        List<ExecutionContext> actionCalls = actionCallParser.parseActionCalls(response, "test-session");

        // Then
        assertNotNull(actionCalls);
        assertTrue(actionCalls.isEmpty());
    }

    @Test
    void testParseInvalidJson() {
        // Given
        String content = "This is invalid JSON: { invalid json content";
        ModelResponse response = createMockModelResponse(content);

        // When
        List<ExecutionContext> actionCalls = actionCallParser.parseActionCalls(response, "test-session");

        // Then
        assertNotNull(actionCalls);
        assertTrue(actionCalls.isEmpty());
    }

    @Test
    void testParseNullResponse() {
        // Given
        ModelResponse response = createMockModelResponse(null);

        // When
        List<ExecutionContext> actionCalls = actionCallParser.parseActionCalls(response, "test-session");

        // Then
        assertNotNull(actionCalls);
        assertTrue(actionCalls.isEmpty());
    }

    @Test
    void testParseEmptyResponse() {
        // Given
        ModelResponse response = createMockModelResponse("");

        // When
        List<ExecutionContext> actionCalls = actionCallParser.parseActionCalls(response, "test-session");

        // Then
        assertNotNull(actionCalls);
        assertTrue(actionCalls.isEmpty());
    }

    @Test
    void testValidateActionCall() {
        // Given
        ExecutionContext validActionCall = ExecutionContext.builder().protocol("a2a").clientId("test-client")
                .sessionId("test-session").correlationId("test-correlation").withActionName("test.action").build();

        // When
        boolean isValid = actionCallParser.validateActionCall(validActionCall, null);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateActionCallMissingCorrelationId() {
        // Given
        ExecutionContext invalidActionCall = ExecutionContext.builder().withProtocol("a2a").withClientId("test-client")
                .withSessionId("test-session").withCorrelationId("").withValues(Map.of("action", "test.action"))
                .build();

        // When
        boolean isValid = actionCallParser.validateActionCall(invalidActionCall, null);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateActionCallMissingAction() {
        // Given
        ExecutionContext invalidActionCall = ExecutionContext.builder().withProtocol("a2a").withClientId("test-client")
                .withSessionId("test-session").withCorrelationId("test-correlation").withValues(Map.of()).build();

        // When
        boolean isValid = actionCallParser.validateActionCall(invalidActionCall, null);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateActionCallEmptyAction() {
        // Given
        ExecutionContext invalidActionCall = ExecutionContext.builder().withProtocol("a2a").withClientId("test-client")
                .withSessionId("test-session").withCorrelationId("test-correlation").withValues(Map.of("action", ""))
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
        ModelResponse response = createMockModelResponse(content);

        // When
        actionCallParser.parseActionCalls(response, "test-session");
        org.openhab.core.ai.common.metrics.ModelPerformanceMetrics metrics = actionCallParser.getPerformanceMetrics();

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
        ModelResponse response1 = createMockModelResponse(content1);
        ModelResponse response2 = createMockModelResponse(content2);

        // When
        actionCallParser.parseActionCalls(response1, "session-1");
        actionCallParser.parseActionCalls(response2, "session-2");
        org.openhab.core.ai.common.metrics.ModelPerformanceMetrics metrics = actionCallParser.getPerformanceMetrics();

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
        // Given - Test unified metrics construction
        ModelPerformanceMetrics metrics = new ModelPerformanceMetrics(10, 5, 3, 2, 8, 0, 0.0, null);

        // Then
        assertNotNull(metrics);
        assertEquals(10, metrics.getTotalParsingAttempts());
        assertEquals(5, metrics.getSuccessfulJsonParses());
        assertEquals(3, metrics.getSuccessfulRegexParses());
        assertEquals(2, metrics.getFailedParses());
        assertEquals(8, metrics.getTotalActionCalls());
    }

    private ModelResponse createMockModelResponse(String content) {
        return ModelResponse.builder().withContent(content != null ? content : "").withModelName("test-model")
                .withProviderType("test-provider").withPromptTokens(50).withCompletionTokens(50).withTotalTokens(100)
                .withCost(0.0).withResponseTimeMs(100).build();
    }
}
