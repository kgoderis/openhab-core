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
import org.openhab.core.ai.common.actions.events.SubscribeEventsAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;

@ExtendWith(MockitoExtension.class)
class SubscribeEventsActionTest {

    @Mock
    private AIActionContext mockContext;

    private SubscribeEventsAction action;

    @BeforeEach
    void setUp() {
        action = new SubscribeEventsAction();
        action.initialize(mockContext);
    }

    @Test
    void testGetActionId() {
        assertEquals("openhab.events.subscribe", action.getActionId());
    }

    @Test
    void testGetActionName() {
        assertEquals("Subscribe Events", action.getActionName());
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
        parameters.put("eventTypes", List.of("ItemStateChangedEvent", "ThingStatusInfoChangedEvent"));
        parameters.put("clientId", "test-client-123");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithFilters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("eventTypes", List.of("ItemStateChangedEvent"));
        parameters.put("clientId", "test-client-123");
        parameters.put("filters", Map.of("itemName", "LivingRoom_Light", "source", "ai-action"));

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithMissingEventTypes() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("clientId", "test-client-123");
        // Missing "eventTypes" parameter

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("eventTypes")));
    }

    @Test
    void testValidateParametersWithMissingClientId() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("eventTypes", List.of("ItemStateChangedEvent"));
        // Missing "clientId" parameter

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("clientId")));
    }

    @Test
    void testValidateParametersWithNullEventTypes() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("eventTypes", null);
        parameters.put("clientId", "test-client-123");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("eventTypes")));
    }

    @Test
    void testValidateParametersWithNullClientId() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("eventTypes", List.of("ItemStateChangedEvent"));
        parameters.put("clientId", null);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("clientId")));
    }

    @Test
    void testValidateParametersWithEmptyEventTypes() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("eventTypes", List.of());
        parameters.put("clientId", "test-client-123");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("eventTypes")));
    }

    @Test
    void testValidateParametersWithEmptyClientId() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("eventTypes", List.of("ItemStateChangedEvent"));
        parameters.put("clientId", "");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("clientId")));
    }

    @Test
    void testValidateParametersWithSingleEventType() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("eventTypes", List.of("ItemStateChangedEvent"));
        parameters.put("clientId", "test-client-123");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithMultipleEventTypes() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("eventTypes",
                List.of("ItemStateChangedEvent", "ThingStatusInfoChangedEvent", "RuleStatusInfoEvent"));
        parameters.put("clientId", "test-client-123");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testExecuteWithBasicParameters() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("eventTypes", List.of("ItemStateChangedEvent"));
        parameters.put("clientId", "test-client-123");

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("subscriptionId"));
        assertNotNull(data.get("sseUrl"));
        assertNotNull(data.get("eventTypes"));
        assertNotNull(data.get("status"));
        assertNotNull(data.get("timestamp"));
    }

    @Test
    void testExecuteWithFilters() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("eventTypes", List.of("ItemStateChangedEvent"));
        parameters.put("clientId", "test-client-123");
        parameters.put("filters", Map.of("itemName", "LivingRoom_Light", "source", "ai-action"));

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("subscriptionId"));
        assertNotNull(data.get("sseUrl"));
        assertNotNull(data.get("eventTypes"));
        assertNotNull(data.get("filters"));
        assertNotNull(data.get("status"));
        assertNotNull(data.get("timestamp"));
    }

    @Test
    void testExecuteWithMultipleEventTypes() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("eventTypes",
                List.of("ItemStateChangedEvent", "ThingStatusInfoChangedEvent", "RuleStatusInfoEvent"));
        parameters.put("clientId", "test-client-123");

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("subscriptionId"));
        assertNotNull(data.get("sseUrl"));
        assertNotNull(data.get("eventTypes"));
        assertNotNull(data.get("status"));
        assertNotNull(data.get("timestamp"));

        @SuppressWarnings("unchecked")
        List<String> eventTypes = (List<String>) data.get("eventTypes");
        assertEquals(3, eventTypes.size());
    }

    @Test
    void testExecuteWithComplexFilters() throws AIActionException {
        // Setup
        Map<String, Object> complexFilters = new HashMap<>();
        complexFilters.put("itemName", "LivingRoom_Light");
        complexFilters.put("thingUID", "zwave:device:node");
        complexFilters.put("topic", "openhab/items/*/state");
        complexFilters.put("source", "ai-action");
        complexFilters.put("metadata", Map.of("priority", "high", "category", "lighting"));

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("eventTypes", List.of("ItemStateChangedEvent"));
        parameters.put("clientId", "test-client-123");
        parameters.put("filters", complexFilters);

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("subscriptionId"));
        assertNotNull(data.get("sseUrl"));
        assertNotNull(data.get("eventTypes"));
        assertNotNull(data.get("filters"));
        assertNotNull(data.get("status"));
        assertNotNull(data.get("timestamp"));
    }

    @Test
    void testExecuteWithDifferentEventTypes() throws AIActionException {
        String[] eventTypes = { "ItemStateChangedEvent", "ThingStatusInfoChangedEvent", "RuleStatusInfoEvent",
                "ItemCommandEvent", "ItemAddedEvent", "ItemRemovedEvent", "ThingAddedEvent", "ThingRemovedEvent" };

        for (String eventType : eventTypes) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("eventTypes", List.of(eventType));
            parameters.put("clientId", "test-client-" + eventType);

            AIActionResult result = action.execute(parameters, mockContext);

            assertTrue(result.isSuccess());
            assertNotNull(result.getData());

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertNotNull(data.get("subscriptionId"));
            assertNotNull(data.get("sseUrl"));
            assertNotNull(data.get("eventTypes"));
            assertNotNull(data.get("status"));
        }
    }

    @Test
    void testExecuteAsync() {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("eventTypes", List.of("ItemStateChangedEvent"));
        parameters.put("clientId", "test-client-123");

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
    void testExecuteWithMissingClientId() {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("eventTypes", List.of("ItemStateChangedEvent"));
        // Missing "clientId" parameter

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
        parameters.put("eventTypes", List.of("ItemStateChangedEvent"));
        parameters.put("clientId", "test-client-123");
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
        parameters.put("eventTypes", List.of("ItemStateChangedEvent"));
        parameters.put("clientId", "test-client-123");
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
        assertTrue(properties.containsKey("eventTypes"));
        assertTrue(properties.containsKey("filters"));
        assertTrue(properties.containsKey("clientId"));

        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertNotNull(required);
        assertTrue(required.contains("eventTypes"));
        assertTrue(required.contains("clientId"));

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
        assertTrue(properties.containsKey("subscriptionId"));
        assertTrue(properties.containsKey("sseUrl"));
        assertTrue(properties.containsKey("eventTypes"));
        assertTrue(properties.containsKey("filters"));
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
        assertTrue(capabilities.containsKey("supportsEventSubscription"));
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
    void testExecuteWithItemStateChangedEvent() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("eventTypes", List.of("ItemStateChangedEvent"));
        parameters.put("clientId", "item-state-client");

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("ItemStateChangedEvent", ((List<String>) data.get("eventTypes")).get(0));
        assertEquals("item-state-client", data.get("clientId"));
    }

    @Test
    void testExecuteWithThingStatusEvent() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("eventTypes", List.of("ThingStatusInfoChangedEvent"));
        parameters.put("clientId", "thing-status-client");

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("ThingStatusInfoChangedEvent", ((List<String>) data.get("eventTypes")).get(0));
        assertEquals("thing-status-client", data.get("clientId"));
    }

    @Test
    void testExecuteWithRuleStatusEvent() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("eventTypes", List.of("RuleStatusInfoEvent"));
        parameters.put("clientId", "rule-status-client");

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("RuleStatusInfoEvent", ((List<String>) data.get("eventTypes")).get(0));
        assertEquals("rule-status-client", data.get("clientId"));
    }
}
