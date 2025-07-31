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
import org.openhab.core.ai.common.actions.network.TestConnectivityAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;

/**
 * Unit tests for TestConnectivityAction.
 * 
 * Tests cover:
 * - Action metadata (ID, name, description, category, version)
 * - Parameter validation (valid and invalid scenarios)
 * - Execution (connectivity testing scenarios)
 * - Async execution
 * - Error handling
 * - Schema generation
 * - Capabilities verification
 * - Lifecycle management
 */
@ExtendWith(MockitoExtension.class)
class TestConnectivityActionTest {

    @Mock
    private AIActionContext mockContext;

    private TestConnectivityAction action;

    @BeforeEach
    void setUp() {
        action = new TestConnectivityAction();

        // Setup mock context
        when(mockContext.getProtocol()).thenReturn("mcp");
        when(mockContext.getClientId()).thenReturn("test-client");
        when(mockContext.getSessionId()).thenReturn("test-session");

        // Initialize the action
        action.initialize(mockContext);
    }

    @Test
    void testGetActionId() {
        assertEquals("test_connectivity", action.getActionId());
    }

    @Test
    void testGetActionName() {
        assertEquals("Test Connectivity", action.getActionName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(action.getDescription());
        assertTrue(action.getDescription().contains("Tests network connectivity"));
    }

    @Test
    void testGetCategory() {
        assertEquals("network", action.getCategory());
    }

    @Test
    void testGetVersion() {
        assertEquals("1.0.0", action.getVersion());
    }

    @Test
    void testValidateParametersWithValidBasicParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("hosts", java.util.List.of("8.8.8.8", "google.com"));
        parameters.put("ports", java.util.List.of(80, 443));

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidAdvancedParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("hosts", java.util.List.of("8.8.8.8", "1.1.1.1", "google.com"));
        parameters.put("ports", java.util.List.of(80, 443, 8080));
        parameters.put("timeout", 10000);
        parameters.put("includeLatency", true);
        parameters.put("includeDNS", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithEmptyHosts() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("hosts", java.util.List.of());

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("hosts")));
    }

    @Test
    void testValidateParametersWithInvalidHosts() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("hosts", "invalid_host");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("hosts")));
    }

    @Test
    void testValidateParametersWithInvalidPorts() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("hosts", java.util.List.of("8.8.8.8"));
        parameters.put("ports", "invalid_ports");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("ports")));
    }

    @Test
    void testValidateParametersWithInvalidPortNumbers() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("hosts", java.util.List.of("8.8.8.8"));
        parameters.put("ports", java.util.List.of(80, 70000));

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("ports")));
    }

    @Test
    void testValidateParametersWithInvalidTimeout() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("hosts", java.util.List.of("8.8.8.8"));
        parameters.put("timeout", -1);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("timeout")));
    }

    @Test
    void testValidateParametersWithInvalidIncludeLatency() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("hosts", java.util.List.of("8.8.8.8"));
        parameters.put("includeLatency", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("includeLatency")));
    }

    @Test
    void testValidateParametersWithInvalidIncludeDNS() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("hosts", java.util.List.of("8.8.8.8"));
        parameters.put("includeDNS", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("includeDNS")));
    }

    @Test
    void testExecuteBasicConnectivityTest() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("hosts", java.util.List.of("8.8.8.8", "google.com"));
        parameters.put("ports", java.util.List.of(80, 443));

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("overallStatus"));
        assertNotNull(data.get("hostTests"));
        assertNotNull(data.get("portTests"));
        assertNotNull(data.get("summary"));
        assertNotNull(data.get("timestamp"));
    }

    @Test
    void testExecuteConnectivityTestWithLatency() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("hosts", java.util.List.of("8.8.8.8"));
        parameters.put("includeLatency", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("overallStatus"));
        assertNotNull(data.get("hostTests"));
    }

    @Test
    void testExecuteConnectivityTestWithDNS() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("hosts", java.util.List.of("google.com"));
        parameters.put("includeDNS", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("overallStatus"));
        assertNotNull(data.get("dnsTests"));
    }

    @Test
    void testExecuteConnectivityTestWithCustomTimeout() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("hosts", java.util.List.of("8.8.8.8"));
        parameters.put("timeout", 10000);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("overallStatus"));
        assertNotNull(data.get("hostTests"));
    }

    @Test
    void testExecuteConnectivityTestWithAllOptions() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("hosts", java.util.List.of("8.8.8.8", "1.1.1.1", "google.com"));
        parameters.put("ports", java.util.List.of(80, 443, 8080));
        parameters.put("timeout", 5000);
        parameters.put("includeLatency", true);
        parameters.put("includeDNS", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("overallStatus"));
        assertNotNull(data.get("hostTests"));
        assertNotNull(data.get("portTests"));
        assertNotNull(data.get("dnsTests"));
        assertNotNull(data.get("summary"));
    }

    @Test
    void testExecuteAsync() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("hosts", java.util.List.of("8.8.8.8"));

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
        parameters.put("hosts", java.util.List.of("8.8.8.8"));
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
        parameters.put("hosts", java.util.List.of("8.8.8.8"));
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
        assertTrue(properties.containsKey("hosts"));
        assertTrue(properties.containsKey("ports"));
        assertTrue(properties.containsKey("timeout"));
        assertTrue(properties.containsKey("includeLatency"));
        assertTrue(properties.containsKey("includeDNS"));
    }

    @Test
    void testGetReturnSchema() {
        Map<String, Object> schema = action.getReturnSchema();

        assertNotNull(schema);
        assertEquals("object", schema.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("overallStatus"));
        assertTrue(properties.containsKey("hostTests"));
        assertTrue(properties.containsKey("portTests"));
        assertTrue(properties.containsKey("dnsTests"));
        assertTrue(properties.containsKey("summary"));
        assertTrue(properties.containsKey("timestamp"));
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
        assertTrue(capabilities.containsKey("supportsLatency"));
        assertTrue(capabilities.containsKey("supportsDNS"));
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
