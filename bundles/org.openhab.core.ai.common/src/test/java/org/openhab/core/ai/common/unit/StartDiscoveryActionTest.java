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
import org.openhab.core.ai.common.actions.discovery.StartDiscoveryAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.thing.ThingRegistry;

/**
 * Unit tests for StartDiscoveryAction.
 * 
 * Tests cover:
 * - Action metadata (ID, name, description, category, version)
 * - Parameter validation (valid and invalid scenarios)
 * - Execution (discovery start scenarios)
 * - Async execution
 * - Error handling
 * - Schema generation
 * - Capabilities verification
 * - Lifecycle management
 */
@ExtendWith(MockitoExtension.class)
class StartDiscoveryActionTest {

    @Mock
    private AIActionContext mockContext;

    @Mock
    private ThingRegistry mockThingRegistry;

    private StartDiscoveryAction action;

    @BeforeEach
    void setUp() {
        action = new StartDiscoveryAction();

        // Setup mock context
        when(mockContext.getProtocol()).thenReturn("mcp");
        when(mockContext.getClientId()).thenReturn("test-client");
        when(mockContext.getSessionId()).thenReturn("test-session");

        // Initialize the action
        action.initialize(mockContext);
    }

    @Test
    void testGetActionId() {
        assertEquals("openhab.discovery.start", action.getActionId());
    }

    @Test
    void testGetActionName() {
        assertEquals("Start Discovery", action.getActionName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(action.getDescription());
        assertTrue(action.getDescription().contains("Starts device discovery processes"));
    }

    @Test
    void testGetCategory() {
        assertEquals("discovery", action.getCategory());
    }

    @Test
    void testGetVersion() {
        assertEquals("1.0.0", action.getVersion());
    }

    @Test
    void testValidateParametersWithValidBasicParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidProtocol() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("protocol", "UPnP");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidDeviceType() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("deviceType", "switch");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidTimeout() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("timeout", 600);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidScanNetwork() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("scanNetwork", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidScanPorts() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("scanPorts", java.util.List.of(80, 443, 8080));

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidFilters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("filters", Map.of("manufacturer", "Philips"));

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidAutoApprove() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("autoApprove", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidBackground() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("background", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidComplexParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("protocol", "UPnP");
        parameters.put("deviceType", "switch");
        parameters.put("timeout", 600);
        parameters.put("scanNetwork", true);
        parameters.put("scanPorts", java.util.List.of(80, 443));
        parameters.put("filters", Map.of("manufacturer", "Philips"));
        parameters.put("autoApprove", false);
        parameters.put("background", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithMissingBindingId() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("protocol", "UPnP");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("bindingId")));
    }

    @Test
    void testValidateParametersWithEmptyBindingId() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("bindingId")));
    }

    @Test
    void testValidateParametersWithInvalidTimeout() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("timeout", 20);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("timeout")));
    }

    @Test
    void testValidateParametersWithInvalidTimeoutTooHigh() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("timeout", 4000);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("timeout")));
    }

    @Test
    void testValidateParametersWithInvalidScanNetwork() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("scanNetwork", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("scanNetwork")));
    }

    @Test
    void testValidateParametersWithInvalidAutoApprove() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("autoApprove", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("autoApprove")));
    }

    @Test
    void testValidateParametersWithInvalidBackground() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("background", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("background")));
    }

    @Test
    void testValidateParametersWithInvalidScanPorts() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("scanPorts", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("scanPorts")));
    }

    @Test
    void testExecuteBasicDiscovery() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("zwave", data.get("bindingId"));
        assertNotNull(data.get("discoveryId"));
        assertNotNull(data.get("status"));
        assertNotNull(data.get("startTime"));
        assertNotNull(data.get("estimatedDuration"));
        assertNotNull(data.get("estimatedDevices"));
    }

    @Test
    void testExecuteDiscoveryWithProtocol() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("protocol", "UPnP");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("zwave", data.get("bindingId"));
        assertEquals("UPnP", data.get("protocol"));
        assertNotNull(data.get("discoveryId"));
    }

    @Test
    void testExecuteDiscoveryWithDeviceType() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("deviceType", "switch");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("zwave", data.get("bindingId"));
        assertEquals("switch", data.get("deviceType"));
        assertNotNull(data.get("discoveryId"));
    }

    @Test
    void testExecuteDiscoveryWithTimeout() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("timeout", 600);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("zwave", data.get("bindingId"));
        assertEquals(600, data.get("timeout"));
        assertNotNull(data.get("discoveryId"));
    }

    @Test
    void testExecuteDiscoveryWithScanNetwork() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("scanNetwork", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("zwave", data.get("bindingId"));
        assertEquals(true, data.get("scanNetwork"));
        assertNotNull(data.get("discoveryId"));
    }

    @Test
    void testExecuteDiscoveryWithScanPorts() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("scanPorts", java.util.List.of(80, 443));

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("zwave", data.get("bindingId"));
        assertNotNull(data.get("scanPorts"));
        assertNotNull(data.get("discoveryId"));
    }

    @Test
    void testExecuteDiscoveryWithFilters() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("filters", Map.of("manufacturer", "Philips"));

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("zwave", data.get("bindingId"));
        assertNotNull(data.get("filters"));
        assertNotNull(data.get("discoveryId"));
    }

    @Test
    void testExecuteDiscoveryWithAutoApprove() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("autoApprove", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("zwave", data.get("bindingId"));
        assertEquals(true, data.get("autoApprove"));
        assertNotNull(data.get("discoveryId"));
    }

    @Test
    void testExecuteDiscoveryWithBackground() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("background", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("zwave", data.get("bindingId"));
        assertEquals(true, data.get("background"));
        assertNotNull(data.get("discoveryId"));
    }

    @Test
    void testExecuteDiscoveryWithComplexParameters() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");
        parameters.put("protocol", "UPnP");
        parameters.put("deviceType", "switch");
        parameters.put("timeout", 600);
        parameters.put("scanNetwork", true);
        parameters.put("scanPorts", java.util.List.of(80, 443));
        parameters.put("filters", Map.of("manufacturer", "Philips"));
        parameters.put("autoApprove", false);
        parameters.put("background", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("zwave", data.get("bindingId"));
        assertEquals("UPnP", data.get("protocol"));
        assertEquals("switch", data.get("deviceType"));
        assertEquals(600, data.get("timeout"));
        assertEquals(true, data.get("scanNetwork"));
        assertEquals(false, data.get("autoApprove"));
        assertEquals(true, data.get("background"));
        assertNotNull(data.get("discoveryId"));
    }

    @Test
    void testExecuteAsync() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bindingId", "zwave");

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
        parameters.put("bindingId", "zwave");
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
        parameters.put("bindingId", "zwave");
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
        assertTrue(properties.containsKey("bindingId"));
        assertTrue(properties.containsKey("protocol"));
        assertTrue(properties.containsKey("deviceType"));
        assertTrue(properties.containsKey("timeout"));
        assertTrue(properties.containsKey("scanNetwork"));
        assertTrue(properties.containsKey("scanPorts"));
        assertTrue(properties.containsKey("filters"));
        assertTrue(properties.containsKey("autoApprove"));
        assertTrue(properties.containsKey("background"));

        @SuppressWarnings("unchecked")
        java.util.List<String> required = (java.util.List<String>) schema.get("required");
        assertNotNull(required);
        assertTrue(required.contains("bindingId"));
    }

    @Test
    void testGetReturnSchema() {
        Map<String, Object> schema = action.getReturnSchema();

        assertNotNull(schema);
        assertEquals("object", schema.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("bindingId"));
        assertTrue(properties.containsKey("discoveryId"));
        assertTrue(properties.containsKey("status"));
        assertTrue(properties.containsKey("startTime"));
        assertTrue(properties.containsKey("estimatedDuration"));
        assertTrue(properties.containsKey("estimatedDevices"));
        assertTrue(properties.containsKey("protocol"));
        assertTrue(properties.containsKey("deviceType"));
        assertTrue(properties.containsKey("timeout"));
        assertTrue(properties.containsKey("scanNetwork"));
        assertTrue(properties.containsKey("scanPorts"));
        assertTrue(properties.containsKey("filters"));
        assertTrue(properties.containsKey("autoApprove"));
        assertTrue(properties.containsKey("background"));
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
        assertTrue(capabilities.containsKey("supportsBackground"));
        assertTrue(capabilities.containsKey("supportsAutoApprove"));
        assertTrue(capabilities.containsKey("supportsNetworkScanning"));
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
