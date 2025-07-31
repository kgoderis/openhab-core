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
import org.openhab.core.ai.common.actions.addons.ListAddonsAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.osgi.framework.BundleContext;

/**
 * Unit tests for ListAddonsAction.
 * 
 * Tests cover:
 * - Action metadata (ID, name, description, category, version)
 * - Parameter validation (valid and invalid scenarios)
 * - Execution (addon listing scenarios)
 * - Async execution
 * - Error handling
 * - Schema generation
 * - Capabilities verification
 * - Lifecycle management
 */
@ExtendWith(MockitoExtension.class)
class ListAddonsActionTest {

    @Mock
    private AIActionContext mockContext;

    @Mock
    private BundleContext mockBundleContext;

    private ListAddonsAction action;

    @BeforeEach
    void setUp() {
        action = new ListAddonsAction();

        // Setup mock context
        when(mockContext.getProtocol()).thenReturn("mcp");
        when(mockContext.getClientId()).thenReturn("test-client");
        when(mockContext.getSessionId()).thenReturn("test-session");

        // Initialize the action
        action.initialize(mockContext);
    }

    @Test
    void testGetActionId() {
        assertEquals("openhab.addons.list", action.getActionId());
    }

    @Test
    void testGetActionName() {
        assertEquals("List Addons", action.getActionName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(action.getDescription());
        assertTrue(action.getDescription().contains("Lists and provides information about openHAB addons"));
    }

    @Test
    void testGetCategory() {
        assertEquals("addons", action.getCategory());
    }

    @Test
    void testGetVersion() {
        assertEquals("1.0.0", action.getVersion());
    }

    @Test
    void testValidateParametersWithValidBasicParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_addons");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidGetAddonInfo() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "get_addon_info");
        parameters.put("addonId", "org.openhab.binding.zwave");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidAddonSummary() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "addon_summary");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidListByType() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_by_type");
        parameters.put("addonType", "binding");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidAddonType() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_addons");
        parameters.put("addonType", "transformation");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidIncludeDetails() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_addons");
        parameters.put("includeDetails", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidIncludeSystemBundles() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_addons");
        parameters.put("includeSystemBundles", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidStateFilter() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_addons");
        parameters.put("stateFilter", "ACTIVE");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidComplexParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_by_type");
        parameters.put("addonType", "binding");
        parameters.put("addonId", "org.openhab.binding.zwave");
        parameters.put("includeDetails", true);
        parameters.put("includeSystemBundles", false);
        parameters.put("stateFilter", "ACTIVE");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithMissingAction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("addonType", "binding");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("action")));
    }

    @Test
    void testValidateParametersWithEmptyAction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("action")));
    }

    @Test
    void testValidateParametersWithInvalidAction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "invalid_action");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("action")));
    }

    @Test
    void testValidateParametersWithInvalidAddonType() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_addons");
        parameters.put("addonType", "invalid_type");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("addonType")));
    }

    @Test
    void testValidateParametersWithInvalidStateFilter() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_addons");
        parameters.put("stateFilter", "INVALID_STATE");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("stateFilter")));
    }

    @Test
    void testValidateParametersWithInvalidIncludeDetails() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_addons");
        parameters.put("includeDetails", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("includeDetails")));
    }

    @Test
    void testValidateParametersWithInvalidIncludeSystemBundles() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_addons");
        parameters.put("includeSystemBundles", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("includeSystemBundles")));
    }

    @Test
    void testExecuteBasicListAddons() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_addons");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("list_addons", data.get("action"));
        assertNotNull(data.get("addons"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteGetAddonInfo() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "get_addon_info");
        parameters.put("addonId", "org.openhab.binding.zwave");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("get_addon_info", data.get("action"));
        assertEquals("org.openhab.binding.zwave", data.get("addonId"));
        assertNotNull(data.get("addonInfo"));
    }

    @Test
    void testExecuteAddonSummary() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "addon_summary");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("addon_summary", data.get("action"));
        assertNotNull(data.get("summary"));
        assertNotNull(data.get("typeBreakdown"));
        assertNotNull(data.get("stateBreakdown"));
    }

    @Test
    void testExecuteListByType() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_by_type");
        parameters.put("addonType", "binding");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("list_by_type", data.get("action"));
        assertEquals("binding", data.get("addonType"));
        assertNotNull(data.get("addons"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteWithAddonTypeFilter() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_addons");
        parameters.put("addonType", "transformation");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("list_addons", data.get("action"));
        assertEquals("transformation", data.get("addonType"));
        assertNotNull(data.get("addons"));
    }

    @Test
    void testExecuteWithIncludeDetails() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_addons");
        parameters.put("includeDetails", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("list_addons", data.get("action"));
        assertEquals(true, data.get("includeDetails"));
        assertNotNull(data.get("addons"));
    }

    @Test
    void testExecuteWithIncludeSystemBundles() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_addons");
        parameters.put("includeSystemBundles", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("list_addons", data.get("action"));
        assertEquals(true, data.get("includeSystemBundles"));
        assertNotNull(data.get("addons"));
    }

    @Test
    void testExecuteWithStateFilter() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_addons");
        parameters.put("stateFilter", "ACTIVE");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("list_addons", data.get("action"));
        assertEquals("ACTIVE", data.get("stateFilter"));
        assertNotNull(data.get("addons"));
    }

    @Test
    void testExecuteWithComplexParameters() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_by_type");
        parameters.put("addonType", "binding");
        parameters.put("addonId", "org.openhab.binding.zwave");
        parameters.put("includeDetails", true);
        parameters.put("includeSystemBundles", false);
        parameters.put("stateFilter", "ACTIVE");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("list_by_type", data.get("action"));
        assertEquals("binding", data.get("addonType"));
        assertEquals("org.openhab.binding.zwave", data.get("addonId"));
        assertEquals(true, data.get("includeDetails"));
        assertEquals(false, data.get("includeSystemBundles"));
        assertEquals("ACTIVE", data.get("stateFilter"));
        assertNotNull(data.get("addons"));
    }

    @Test
    void testExecuteAsync() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "list_addons");

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
        parameters.put("action", "list_addons");
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
        parameters.put("action", "list_addons");
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
        assertTrue(properties.containsKey("action"));
        assertTrue(properties.containsKey("addonType"));
        assertTrue(properties.containsKey("addonId"));
        assertTrue(properties.containsKey("includeDetails"));
        assertTrue(properties.containsKey("includeSystemBundles"));
        assertTrue(properties.containsKey("stateFilter"));

        @SuppressWarnings("unchecked")
        java.util.List<String> required = (java.util.List<String>) schema.get("required");
        assertNotNull(required);
        assertTrue(required.contains("action"));
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
        assertTrue(properties.containsKey("addons"));
        assertTrue(properties.containsKey("totalCount"));
        assertTrue(properties.containsKey("addonInfo"));
        assertTrue(properties.containsKey("summary"));
        assertTrue(properties.containsKey("typeBreakdown"));
        assertTrue(properties.containsKey("stateBreakdown"));
        assertTrue(properties.containsKey("addonType"));
        assertTrue(properties.containsKey("addonId"));
        assertTrue(properties.containsKey("includeDetails"));
        assertTrue(properties.containsKey("includeSystemBundles"));
        assertTrue(properties.containsKey("stateFilter"));
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
        assertTrue(capabilities.containsKey("supportsOSGi"));
        assertTrue(capabilities.containsKey("supportsBundleAnalysis"));
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
