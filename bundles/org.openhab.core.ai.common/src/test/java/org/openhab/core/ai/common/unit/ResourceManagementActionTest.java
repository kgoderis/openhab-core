package org.openhab.core.ai.common.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.actions.resources.ResourceManagementAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;

/**
 * Unit tests for ResourceManagementAction.
 * 
 * Tests cover:
 * - Action metadata (ID, name, description, category, version)
 * - Parameter validation (valid and invalid scenarios)
 * - Execution (resource management scenarios)
 * - Async execution
 * - Error handling
 * - Schema generation
 * - Capabilities verification
 * - Lifecycle management
 */
@ExtendWith(MockitoExtension.class)
class ResourceManagementActionTest {

    @Mock
    private AIActionContext mockContext;

    private ResourceManagementAction action;

    @BeforeEach
    void setUp() {
        action = new ResourceManagementAction();

        // Setup mock context
        when(mockContext.getProtocol()).thenReturn("mcp");
        when(mockContext.getClientId()).thenReturn("test-client");
        when(mockContext.getSessionId()).thenReturn("test-session");

        // Initialize the action
        action.initialize(mockContext);
    }

    @Test
    void testGetActionId() {
        assertEquals("openhab.resources.manage", action.getActionId());
    }

    @Test
    void testGetActionName() {
        assertEquals("Resource Management", action.getActionName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(action.getDescription());
        assertTrue(action.getDescription().contains("Manage resources in the openHAB system"));
    }

    @Test
    void testGetCategory() {
        assertEquals("resources", action.getCategory());
    }

    @Test
    void testGetVersion() {
        assertEquals("1.0.0", action.getVersion());
    }

    @Test
    void testValidateParametersWithValidListOperation() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "list");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidGetOperation() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "get");
        parameters.put("resourceId", "test-resource");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidCreateOperation() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "create");
        parameters.put("resourceType", "item");
        parameters.put("resourceData", Map.of("name", "test-item"));

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidUpdateOperation() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "update");
        parameters.put("resourceId", "test-resource");
        parameters.put("resourceData", Map.of("name", "updated-item"));

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidDeleteOperation() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "delete");
        parameters.put("resourceId", "test-resource");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidResourceType() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "list");
        parameters.put("resourceType", "thing");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidFilter() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "list");
        parameters.put("filter", Map.of("type", "item"));

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidComplexParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "create");
        parameters.put("resourceType", "item");
        parameters.put("resourceId", "test-item");
        parameters.put("resourceData", Map.of("name", "test-item", "type", "String"));
        parameters.put("filter", Map.of("category", "test"));

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithMissingOperation() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("resourceType", "item");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("operation")));
    }

    @Test
    void testValidateParametersWithEmptyOperation() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("operation")));
    }

    @Test
    void testValidateParametersWithInvalidOperation() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "invalid_operation");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("operation")));
    }

    @Test
    void testValidateParametersWithMissingResourceIdForGet() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "get");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("resourceId")));
    }

    @Test
    void testValidateParametersWithMissingResourceIdForUpdate() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "update");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("resourceId")));
    }

    @Test
    void testValidateParametersWithMissingResourceIdForDelete() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "delete");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("resourceId")));
    }

    @Test
    void testValidateParametersWithMissingResourceDataForCreate() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "create");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("resourceData")));
    }

    @Test
    void testValidateParametersWithMissingResourceDataForUpdate() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "update");
        parameters.put("resourceId", "test-resource");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("resourceData")));
    }

    @Test
    void testValidateParametersWithInvalidResourceData() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "create");
        parameters.put("resourceData", "invalid_data");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("resourceData")));
    }

    @Test
    void testValidateParametersWithInvalidFilter() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "list");
        parameters.put("filter", "invalid_filter");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("filter")));
    }

    @Test
    void testExecuteListOperation() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "list");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("list", data.get("operation"));
        assertNotNull(data.get("resources"));
        assertNotNull(data.get("count"));
    }

    @Test
    void testExecuteGetOperation() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "get");
        parameters.put("resourceId", "test-resource");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("get", data.get("operation"));
        assertEquals("test-resource", data.get("resourceId"));
        assertNotNull(data.get("resourceData"));
    }

    @Test
    void testExecuteCreateOperation() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "create");
        parameters.put("resourceType", "item");
        parameters.put("resourceData", Map.of("name", "test-item"));

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("create", data.get("operation"));
        assertEquals("item", data.get("resourceType"));
        assertNotNull(data.get("resourceData"));
        assertNotNull(data.get("message"));
    }

    @Test
    void testExecuteUpdateOperation() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "update");
        parameters.put("resourceId", "test-resource");
        parameters.put("resourceData", Map.of("name", "updated-item"));

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("update", data.get("operation"));
        assertEquals("test-resource", data.get("resourceId"));
        assertNotNull(data.get("resourceData"));
        assertNotNull(data.get("message"));
    }

    @Test
    void testExecuteDeleteOperation() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "delete");
        parameters.put("resourceId", "test-resource");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("delete", data.get("operation"));
        assertEquals("test-resource", data.get("resourceId"));
        assertNotNull(data.get("message"));
    }

    @Test
    void testExecuteListOperationWithResourceType() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "list");
        parameters.put("resourceType", "thing");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("list", data.get("operation"));
        assertEquals("thing", data.get("resourceType"));
        assertNotNull(data.get("resources"));
        assertNotNull(data.get("count"));
    }

    @Test
    void testExecuteListOperationWithFilter() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "list");
        parameters.put("filter", Map.of("type", "item"));

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("list", data.get("operation"));
        assertNotNull(data.get("filter"));
        assertNotNull(data.get("resources"));
        assertNotNull(data.get("count"));
    }

    @Test
    void testExecuteComplexOperation() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "create");
        parameters.put("resourceType", "item");
        parameters.put("resourceId", "test-item");
        parameters.put("resourceData", Map.of("name", "test-item", "type", "String"));
        parameters.put("filter", Map.of("category", "test"));

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("create", data.get("operation"));
        assertEquals("item", data.get("resourceType"));
        assertEquals("test-item", data.get("resourceId"));
        assertNotNull(data.get("resourceData"));
        assertNotNull(data.get("filter"));
        assertNotNull(data.get("message"));
    }

    @Test
    void testExecuteAsync() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "list");

        CompletableFuture<AIActionResult> future = action.executeAsync(parameters, mockContext);

        assertNotNull(future);
        assertTrue(future.isDone());

        AIActionResult result = future.join();
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    void testExecuteWithInvalidParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("invalidParam", "invalidValue");

        assertThrows(AIActionException.class, () -> {
            action.execute(parameters, mockContext);
        });
    }

    @Test
    void testExecuteWithInvalidContext() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "list");
        AIActionContext invalidContext = AIActionContext.builder().build();

        assertThrows(AIActionException.class, () -> {
            action.execute(parameters, invalidContext);
        });
    }

    @Test
    void testExecuteAsyncWithInvalidParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("invalidParam", "invalidValue");

        assertThrows(AIActionException.class, () -> {
            action.executeAsync(parameters, mockContext);
        });
    }

    @Test
    void testExecuteAsyncWithInvalidContext() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("operation", "list");
        AIActionContext invalidContext = AIActionContext.builder().build();

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
        assertTrue(properties.containsKey("operation"));
        assertTrue(properties.containsKey("resourceType"));
        assertTrue(properties.containsKey("resourceId"));
        assertTrue(properties.containsKey("resourceData"));
        assertTrue(properties.containsKey("filter"));

        @SuppressWarnings("unchecked")
        java.util.List<String> required = (java.util.List<String>) schema.get("required");
        assertNotNull(required);
        assertTrue(required.contains("operation"));
    }

    @Test
    void testGetReturnSchema() {
        Map<String, Object> schema = action.getReturnSchema();

        assertNotNull(schema);
        assertEquals("object", schema.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("operation"));
        assertTrue(properties.containsKey("resourceType"));
        assertTrue(properties.containsKey("resourceId"));
        assertTrue(properties.containsKey("resourceData"));
        assertTrue(properties.containsKey("resources"));
        assertTrue(properties.containsKey("count"));
        assertTrue(properties.containsKey("filter"));
        assertTrue(properties.containsKey("message"));
        assertTrue(properties.containsKey("error"));
    }

    @Test
    void testGetMetadata() {
        AIActionMetadata metadata = action.getMetadata();

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
        assertTrue(capabilities.containsKey("supportsCRUD"));
        assertTrue(capabilities.containsKey("supportsFiltering"));
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
}
