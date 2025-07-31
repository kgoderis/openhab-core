package org.openhab.core.ai.common.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.actions.events.SendEventAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.events.EventPublisher;

@ExtendWith(MockitoExtension.class)
class SendEventActionTest {

    @Mock
    private AIActionContext mockContext;

    @Mock
    private EventPublisher mockEventPublisher;

    private SendEventAction action;

    @BeforeEach
    void setUp() {
        action = new SendEventAction();
        action.initialize(mockContext);
    }

    @Test
    void testGetActionId() {
        assertEquals("openhab.events.send", action.getActionId());
    }

    @Test
    void testGetActionName() {
        assertEquals("Send Event", action.getActionName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(action.getDescription());
        assertTrue(action.getDescription().contains("EventBus"));
    }

    @Test
    void testGetCategory() {
        assertEquals("events", action.getCategory());
    }

    @Test
    void testGetVersion() {
        assertEquals("1.0.0", action.getVersion());
    }

    @Test
    void testValidateParametersWithValidParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("topic", "openhab/items/Light/command");
        parameters.put("payload", Map.of("command", "ON"));

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithAllOptionalParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("topic", "openhab/items/Light/command");
        parameters.put("payload", Map.of("command", "ON"));
        parameters.put("eventType", "CustomEvent");
        parameters.put("source", "ai-action");
        parameters.put("priority", "high");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithMissingTopic() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("payload", Map.of("command", "ON"));
        // Missing "topic" parameter

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("topic")));
    }

    @Test
    void testValidateParametersWithMissingPayload() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("topic", "openhab/items/Light/command");
        // Missing "payload" parameter

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("payload")));
    }

    @Test
    void testValidateParametersWithInvalidPriority() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("topic", "openhab/items/Light/command");
        parameters.put("payload", Map.of("command", "ON"));
        parameters.put("priority", "invalid_priority");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("priority")));
    }

    @Test
    void testValidateParametersWithNullTopic() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("topic", null);
        parameters.put("payload", Map.of("command", "ON"));

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("topic")));
    }

    @Test
    void testValidateParametersWithNullPayload() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("topic", "openhab/items/Light/command");
        parameters.put("payload", null);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("payload")));
    }

    @Test
    void testValidateParametersWithEmptyTopic() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("topic", "");
        parameters.put("payload", Map.of("command", "ON"));

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("topic")));
    }

    @Test
    void testValidateParametersWithValidPriorityValues() {
        String[] validPriorities = { "low", "normal", "high" };

        for (String priority : validPriorities) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("topic", "openhab/items/Light/command");
            parameters.put("payload", Map.of("command", "ON"));
            parameters.put("priority", priority);

            AIActionValidationResult result = action.validateParameters(parameters);

            assertTrue(result.isValid(), "Priority '" + priority + "' should be valid");
        }
    }

    @Test
    void testExecuteWithBasicParameters() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("topic", "openhab/items/Light/command");
        parameters.put("payload", Map.of("command", "ON"));

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("openhab/items/Light/command", data.get("topic"));
        assertNotNull(data.get("eventId"));
        assertNotNull(data.get("status"));
        assertNotNull(data.get("timestamp"));
    }

    @Test
    void testExecuteWithAllParameters() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("topic", "openhab/items/Light/command");
        parameters.put("payload", Map.of("command", "ON", "value", 100));
        parameters.put("eventType", "CustomEvent");
        parameters.put("source", "ai-action");
        parameters.put("priority", "high");

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("openhab/items/Light/command", data.get("topic"));
        assertEquals("CustomEvent", data.get("eventType"));
        assertEquals("ai-action", data.get("source"));
        assertEquals("high", data.get("priority"));
        assertNotNull(data.get("eventId"));
        assertNotNull(data.get("status"));
        assertNotNull(data.get("timestamp"));
    }

    @Test
    void testExecuteWithComplexPayload() throws AIActionException {
        // Setup
        Map<String, Object> complexPayload = new HashMap<>();
        complexPayload.put("command", "ON");
        complexPayload.put("value", 100);
        complexPayload.put("metadata", Map.of("source", "ai", "timestamp", "2024-01-01T00:00:00Z"));
        complexPayload.put("parameters", List.of("param1", "param2"));

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("topic", "openhab/items/Light/command");
        parameters.put("payload", complexPayload);

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("openhab/items/Light/command", data.get("topic"));
        assertNotNull(data.get("eventId"));
        assertNotNull(data.get("status"));
    }

    @Test
    void testExecuteWithDifferentTopics() throws AIActionException {
        String[] topics = { "openhab/items/Light/command", "openhab/items/Temperature/state",
                "openhab/things/zwave:device:node/status", "openhab/rules/MyRule/triggered",
                "openhab/automation/MyAutomation/executed" };

        for (String topic : topics) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("topic", topic);
            parameters.put("payload", Map.of("value", "test"));

            AIActionResult result = action.execute(parameters, mockContext);

            assertTrue(result.isSuccess());
            assertNotNull(result.getData());

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertEquals(topic, data.get("topic"));
        }
    }

    @Test
    void testExecuteAsync() {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("topic", "openhab/items/Light/command");
        parameters.put("payload", Map.of("command", "ON"));

        // Execute
        CompletableFuture<AIActionResult> future = action.executeAsync(parameters, mockContext);

        // Verify
        assertNotNull(future);
        assertTrue(future.isDone());

        AIActionResult result = future.join();
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void testExecuteWithInvalidPriority() {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("topic", "openhab/items/Light/command");
        parameters.put("payload", Map.of("command", "ON"));
        parameters.put("priority", "invalid_priority");

        // Execute and verify exception
        assertThrows(AIActionException.class, () -> {
            action.execute(parameters, mockContext);
        });
    }

    @Test
    void testExecuteWithInvalidParameters() {
        // Should throw exception for invalid parameters
        Map<String, Object> invalidParams = Map.of("invalidParam", "invalidValue");
        assertThrows(AIActionException.class, () -> {
            action.execute(invalidParams, mockContext);
        });
    }

    @Test
    void testExecuteWithInvalidContext() {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("topic", "openhab/items/Light/command");
        parameters.put("payload", Map.of("command", "ON"));
        AIActionContext invalidContext = AIActionContext.builder().build();

        // Should throw exception for invalid context
        assertThrows(AIActionException.class, () -> {
            action.execute(parameters, invalidContext);
        });
    }

    @Test
    void testExecuteAsyncWithInvalidParameters() {
        // Should throw exception for invalid parameters
        Map<String, Object> invalidParams = Map.of("invalidParam", "invalidValue");
        assertThrows(AIActionException.class, () -> {
            action.executeAsync(invalidParams, mockContext);
        });
    }

    @Test
    void testExecuteAsyncWithInvalidContext() {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("topic", "openhab/items/Light/command");
        parameters.put("payload", Map.of("command", "ON"));
        AIActionContext invalidContext = AIActionContext.builder().build();

        // Should throw exception for invalid context
        assertThrows(AIActionException.class, () -> {
            action.executeAsync(parameters, invalidContext);
        });
    }

    @Test
    void testGetParameterSchema() {
        Map<String, Object> schema = action.getParameterSchema();

        assertNotNull(schema);
        assertEquals("object", schema.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("topic"));
        assertTrue(properties.containsKey("payload"));
        assertTrue(properties.containsKey("eventType"));
        assertTrue(properties.containsKey("source"));
        assertTrue(properties.containsKey("priority"));

        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertNotNull(required);
        assertTrue(required.contains("topic"));
        assertTrue(required.contains("payload"));

        assertEquals(false, schema.get("additionalProperties"));
    }

    @Test
    void testGetReturnSchema() {
        Map<String, Object> schema = action.getReturnSchema();

        assertNotNull(schema);
        assertEquals("object", schema.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("eventId"));
        assertTrue(properties.containsKey("topic"));
        assertTrue(properties.containsKey("status"));
        assertTrue(properties.containsKey("timestamp"));
    }

    @Test
    void testGetMetadata() {
        var metadata = action.getMetadata();

        assertNotNull(metadata);
        assertEquals("1.0.0", metadata.getVersion());
        assertNotNull(metadata.getDescription());
        assertNotNull(metadata.getTags());
    }

    @Test
    void testGetCapabilities() {
        Map<String, Object> capabilities = action.getCapabilities();

        assertNotNull(capabilities);
        assertTrue(capabilities.containsKey("supportsAsync"));
        assertTrue(capabilities.containsKey("supportsValidation"));
        assertTrue(capabilities.containsKey("supportsEventPublishing"));
    }

    @Test
    void testIsReady() {
        assertTrue(action.isReady());
    }

    @Test
    void testCleanup() {
        // Should not throw any exception
        assertDoesNotThrow(() -> action.cleanup());
    }

    @Test
    void testInitialize() {
        // Should not throw any exception
        assertDoesNotThrow(() -> action.initialize(mockContext));
    }

    @Test
    void testExecuteWithDefaultValues() throws AIActionException {
        // Setup - only required parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("topic", "openhab/items/Light/command");
        parameters.put("payload", Map.of("command", "ON"));

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("openhab/items/Light/command", data.get("topic"));
        assertEquals("CustomEvent", data.get("eventType")); // Default value
        assertEquals("ai-action", data.get("source")); // Default value
        assertEquals("normal", data.get("priority")); // Default value
    }

    @Test
    void testExecuteWithItemCommandTopic() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("topic", "openhab/items/LivingRoom_Light/command");
        parameters.put("payload", Map.of("command", "ON"));

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("openhab/items/LivingRoom_Light/command", data.get("topic"));
        assertEquals("ON", ((Map<String, Object>) data.get("payload")).get("command"));
    }

    @Test
    void testExecuteWithThingStatusTopic() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("topic", "openhab/things/zwave:device:node/status");
        parameters.put("payload", Map.of("status", "ONLINE", "detail", "Thing is online"));

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("openhab/things/zwave:device:node/status", data.get("topic"));
        assertEquals("ONLINE", ((Map<String, Object>) data.get("payload")).get("status"));
    }
}
