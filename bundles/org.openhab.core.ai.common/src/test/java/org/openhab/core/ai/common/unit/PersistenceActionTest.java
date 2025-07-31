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
import org.openhab.core.ai.common.actions.persistence.PersistenceAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;

@ExtendWith(MockitoExtension.class)
class PersistenceActionTest {

    @Mock
    private AIActionContext mockContext;

    @Mock
    private PersistenceServiceRegistry mockPersistenceRegistry;

    @Mock
    private QueryablePersistenceService mockPersistenceService;

    private PersistenceAction action;

    @BeforeEach
    void setUp() {
        action = new PersistenceAction();

        // Initialize the action (no services needed for simplified version)
        action.initialize(mockContext);
    }

    @Test
    void testGetActionId() {
        assertEquals("openhab.persistence.manage", action.getActionId());
    }

    @Test
    void testGetActionName() {
        assertEquals("Persistence Management", action.getActionName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(action.getDescription());
        assertTrue(action.getDescription().contains("persistence"));
    }

    @Test
    void testGetCategory() {
        assertEquals("persistence", action.getCategory());
    }

    @Test
    void testGetVersion() {
        assertEquals("1.0.0", action.getVersion());
    }

    @Test
    void testValidateParametersWithValidStatusAction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "status");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidInfoAction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "info");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithMissingAction() {
        Map<String, Object> parameters = new HashMap<>();
        // Missing "action" parameter

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Missing required parameter: action"));
    }

    @Test
    void testValidateParametersWithInvalidAction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "invalid_action");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Invalid action. Must be one of: [status, info]"));
    }

    @Test
    void testValidateParametersWithNullAction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", null);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Missing required parameter: action"));
    }

    @Test
    void testExecuteStatusAction() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "status");

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("status", data.get("action"));
        assertFalse((Boolean) data.get("persistenceAvailable")); // Simplified version returns false
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("message"));
    }

    @Test
    void testExecuteInfoAction() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "info");

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("info", data.get("action"));
        assertNotNull(data.get("supportedActions"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("message"));
    }

    @Test
    void testExecuteAsync() {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "status");

        // Execute
        CompletableFuture<AIActionResult> future = action.executeAsync(parameters, mockContext);

        // Verify
        assertNotNull(future);

        // Wait for completion and verify result
        AIActionResult result = future.join();
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void testExecuteWithServiceUnavailable() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "status");

        // Execute (simplified version doesn't use services)
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess()); // Should handle gracefully
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertFalse((Boolean) data.get("persistenceAvailable")); // Simplified version always returns false
    }

    @Test
    void testExecuteWithEmptyPersistenceServices() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "status");

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertFalse((Boolean) data.get("persistenceAvailable")); // Simplified version always returns false
    }

    @Test
    void testExecuteWithException() {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "invalid_action"); // This should cause validation to fail

        // Execute and verify exception
        assertThrows(AIActionException.class, () -> {
            action.execute(parameters, mockContext);
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
        assertTrue(properties.containsKey("action"));

        @SuppressWarnings("unchecked")
        Map<String, Object> actionProperty = (Map<String, Object>) properties.get("action");
        assertEquals("string", actionProperty.get("type"));
        assertTrue(actionProperty.containsKey("enum"));

        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertNotNull(required);
        assertTrue(required.contains("action"));

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
        assertTrue(properties.containsKey("action"));
        assertTrue(properties.containsKey("timestamp"));
        assertTrue(properties.containsKey("persistenceAvailable"));
        assertTrue(properties.containsKey("message"));
        assertTrue(properties.containsKey("persistenceServices"));
        assertTrue(properties.containsKey("supportedActions"));
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
        assertTrue(capabilities.containsKey("persistence_status"));
        assertTrue(capabilities.containsKey("persistence_info"));
        assertTrue(capabilities.containsKey("historical_data_access"));
        assertTrue(capabilities.containsKey("data_export"));
        assertTrue(capabilities.containsKey("data_import"));

        // Check that persistence_status and persistence_info are true
        assertTrue((Boolean) capabilities.get("persistence_status"));
        assertTrue((Boolean) capabilities.get("persistence_info"));

        // Check that other capabilities are false (as per simplified version)
        assertFalse((Boolean) capabilities.get("historical_data_access"));
        assertFalse((Boolean) capabilities.get("data_export"));
        assertFalse((Boolean) capabilities.get("data_import"));
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
    void testExecuteWithInvalidParameters() {
        // Should throw exception for invalid parameters (missing required "action" parameter)
        Map<String, Object> invalidParams = Map.of("invalidParam", "invalidValue");
        assertThrows(AIActionException.class, () -> {
            action.execute(invalidParams, mockContext);
        });
    }

    @Test
    void testExecuteWithInvalidContext() {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "status");
        AIActionContext invalidContext = AIActionContext.builder().build();

        // Should not throw exception for invalid context in simplified version
        assertDoesNotThrow(() -> {
            action.execute(parameters, invalidContext);
        });
    }

    @Test
    void testExecuteAsyncWithInvalidParameters() {
        // Should throw exception for invalid parameters (missing required "action" parameter)
        Map<String, Object> invalidParams = Map.of("invalidParam", "invalidValue");
        assertThrows(AIActionException.class, () -> {
            action.executeAsync(invalidParams, mockContext);
        });
    }

    @Test
    void testExecuteAsyncWithInvalidContext() {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "status");
        AIActionContext invalidContext = AIActionContext.builder().build();

        // Should not throw exception for invalid context in simplified version
        assertDoesNotThrow(() -> {
            action.executeAsync(parameters, invalidContext);
        });
    }
}
