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
import org.openhab.core.ai.common.actions.config.ConfigurationSetAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;

/**
 * Unit tests for ConfigurationSetAction.
 * 
 * Tests cover:
 * - Action metadata (ID, name, description, category, version)
 * - Parameter validation (valid and invalid scenarios)
 * - Execution (configuration setting scenarios)
 * - Async execution
 * - Error handling
 * - Schema generation
 * - Capabilities verification
 * - Lifecycle management
 */
@ExtendWith(MockitoExtension.class)
class ConfigurationSetActionTest {

    @Mock
    private AIActionContext mockContext;

    private ConfigurationSetAction action;

    @BeforeEach
    void setUp() {
        action = new ConfigurationSetAction();

        // Setup mock context
        when(mockContext.getProtocol()).thenReturn("mcp");
        when(mockContext.getClientId()).thenReturn("test-client");
        when(mockContext.getSessionId()).thenReturn("test-session");

        // Initialize the action
        action.initialize(mockContext);
    }

    @Test
    void testGetActionId() {
        assertEquals("openhab.config.set", action.getActionId());
    }

    @Test
    void testGetActionName() {
        assertEquals("Configuration Set", action.getActionName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(action.getDescription());
        assertTrue(action.getDescription().contains("Sets/updates openHAB configuration"));
    }

    @Test
    void testGetCategory() {
        assertEquals("config", action.getCategory());
    }

    @Test
    void testGetVersion() {
        assertEquals("1.0.0", action.getVersion());
    }

    @Test
    void testValidateParametersWithValidItemsConfig() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "items");
        parameters.put("configName", "test.items");
        parameters.put("content", "Switch TestSwitch { channel=\"binding:thing:channel\" }");
        parameters.put("createBackup", true);
        parameters.put("validateSyntax", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidRulesConfig() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "rules");
        parameters.put("configName", "test.rules");
        parameters.put("content",
                "rule \"Test Rule\" when Item TestSwitch changed then logInfo(\"test\", \"Item changed\") end");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidThingsConfig() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "things");
        parameters.put("configName", "test.things");
        parameters.put("content", "Thing binding:thing:test [ param1=\"value1\" ]");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithMissingConfigType() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configName", "test.items");
        parameters.put("content", "Switch TestSwitch { channel=\"binding:thing:channel\" }");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("configType")));
    }

    @Test
    void testValidateParametersWithMissingConfigName() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "items");
        parameters.put("content", "Switch TestSwitch { channel=\"binding:thing:channel\" }");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("configName")));
    }

    @Test
    void testValidateParametersWithMissingContent() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "items");
        parameters.put("configName", "test.items");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("content")));
    }

    @Test
    void testValidateParametersWithInvalidConfigType() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "invalid_type");
        parameters.put("configName", "test.items");
        parameters.put("content", "Switch TestSwitch { channel=\"binding:thing:channel\" }");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("Invalid config type")));
    }

    @Test
    void testValidateParametersWithEmptyConfigName() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "items");
        parameters.put("configName", "");
        parameters.put("content", "Switch TestSwitch { channel=\"binding:thing:channel\" }");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("configName")));
    }

    @Test
    void testValidateParametersWithEmptyContent() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "items");
        parameters.put("configName", "test.items");
        parameters.put("content", "");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("content")));
    }

    @Test
    void testValidateParametersWithInvalidCreateBackup() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "items");
        parameters.put("configName", "test.items");
        parameters.put("content", "Switch TestSwitch { channel=\"binding:thing:channel\" }");
        parameters.put("createBackup", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("createBackup")));
    }

    @Test
    void testValidateParametersWithInvalidValidateSyntax() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "items");
        parameters.put("configName", "test.items");
        parameters.put("content", "Switch TestSwitch { channel=\"binding:thing:channel\" }");
        parameters.put("validateSyntax", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("validateSyntax")));
    }

    @Test
    void testExecuteItemsConfiguration() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "items");
        parameters.put("configName", "test.items");
        parameters.put("content", "Switch TestSwitch { channel=\"binding:thing:channel\" }");
        parameters.put("createBackup", true);
        parameters.put("validateSyntax", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("items", data.get("configType"));
        assertEquals("test.items", data.get("configName"));
        assertTrue((Boolean) data.get("success"));
        assertNotNull(data.get("message"));
        assertNotNull(data.get("timestamp"));
    }

    @Test
    void testExecuteRulesConfiguration() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "rules");
        parameters.put("configName", "test.rules");
        parameters.put("content",
                "rule \"Test Rule\" when Item TestSwitch changed then logInfo(\"test\", \"Item changed\") end");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("rules", data.get("configType"));
        assertEquals("test.rules", data.get("configName"));
        assertTrue((Boolean) data.get("success"));
    }

    @Test
    void testExecuteThingsConfiguration() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "things");
        parameters.put("configName", "test.things");
        parameters.put("content", "Thing binding:thing:test [ param1=\"value1\" ]");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("things", data.get("configType"));
        assertEquals("test.things", data.get("configName"));
        assertTrue((Boolean) data.get("success"));
    }

    @Test
    void testExecuteAsync() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "items");
        parameters.put("configName", "test.items");
        parameters.put("content", "Switch TestSwitch { channel=\"binding:thing:channel\" }");

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
        parameters.put("configType", "items");
        parameters.put("configName", "test.items");
        parameters.put("content", "Switch TestSwitch { channel=\"binding:thing:channel\" }");
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
        parameters.put("configType", "items");
        parameters.put("configName", "test.items");
        parameters.put("content", "Switch TestSwitch { channel=\"binding:thing:channel\" }");
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
        assertTrue(properties.containsKey("configType"));
        assertTrue(properties.containsKey("configName"));
        assertTrue(properties.containsKey("content"));
        assertTrue(properties.containsKey("createBackup"));
        assertTrue(properties.containsKey("validateSyntax"));

        @SuppressWarnings("unchecked")
        java.util.List<String> required = (java.util.List<String>) schema.get("required");
        assertNotNull(required);
        assertTrue(required.contains("configType"));
        assertTrue(required.contains("configName"));
        assertTrue(required.contains("content"));
    }

    @Test
    void testGetReturnSchema() {
        Map<String, Object> schema = action.getReturnSchema();

        assertNotNull(schema);
        assertEquals("object", schema.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("timestamp"));
        assertTrue(properties.containsKey("configType"));
        assertTrue(properties.containsKey("configName"));
        assertTrue(properties.containsKey("success"));
        assertTrue(properties.containsKey("message"));
        assertTrue(properties.containsKey("fileExisted"));
        assertTrue(properties.containsKey("directoryCreated"));
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
        assertTrue(capabilities.containsKey("supportsBackup"));
        assertTrue(capabilities.containsKey("supportsSyntaxValidation"));
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
