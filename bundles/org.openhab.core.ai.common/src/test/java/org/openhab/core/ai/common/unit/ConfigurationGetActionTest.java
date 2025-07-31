package org.openhab.core.ai.common.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.actions.config.ConfigurationGetAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.ai.common.auth.AIAuthenticationContext;

@ExtendWith(MockitoExtension.class)
class ConfigurationGetActionTest {

    @Mock
    private AIActionContext mockContext;

    private ConfigurationGetAction action;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        action = new ConfigurationGetAction();
        // Only initialize the action, don't set up mock context here
        // Mock context will be set up individually in tests that need it
    }

    /**
     * Set up mock context for tests that need it.
     */
    private void setupMockContext() {
        // Configure the existing mock context with proper behavior using lenient stubbing
        lenient().when(mockContext.getProtocol()).thenReturn("test-protocol");
        lenient().when(mockContext.getClientId()).thenReturn("test-client");
        lenient().when(mockContext.getSessionId()).thenReturn("test-session");
        lenient().when(mockContext.getAuthContext()).thenReturn(new AIAuthenticationContext("test-user", "none",
                Map.of(), Set.of(), Instant.now(), null, "test-session"));
        lenient().when(mockContext.getProtocolContext()).thenReturn(new HashMap<>());
        lenient().when(mockContext.getExecutionStartTime()).thenReturn(System.currentTimeMillis());
        lenient().when(mockContext.getCorrelationId()).thenReturn("test-correlation");
        lenient().when(mockContext.getPriority()).thenReturn(Optional.of("normal"));

        action.initialize(mockContext);
    }

    @Test
    void testGetActionId() {
        assertEquals("openhab.config.get", action.getActionId());
    }

    @Test
    void testGetActionName() {
        assertEquals("Configuration Get", action.getActionName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(action.getDescription());
        assertTrue(action.getDescription().contains("configuration"));
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
    void testValidateParametersWithValidParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "items");
        parameters.put("includeContent", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithAllConfigType() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "all");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithInvalidConfigType() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "invalid_type");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("Invalid configType")));
    }

    @Test
    void testValidateParametersWithNullConfigType() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", null);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid()); // Should use default "all"
        assertNotNull(result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithEmptyParameters() {
        Map<String, Object> parameters = new HashMap<>();

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid()); // Should use defaults
        assertNotNull(result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithConfigName() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "items");
        parameters.put("configName", "test_items");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithIncludeContentFalse() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "items");
        parameters.put("includeContent", false);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithIncludeMetadataFalse() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "items");
        parameters.put("includeMetadata", false);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testExecuteWithItemsConfigType() throws AIActionException {
        // Setup
        setupMockContext();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "items");
        parameters.put("includeContent", true);
        parameters.put("includeMetadata", true);

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("items", data.get("configType"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("configurations"));
        assertNotNull(data.get("totalCount"));
        assertNotNull(data.get("confDirectory"));
    }

    @Test
    void testExecuteWithThingsConfigType() throws AIActionException {
        // Setup
        setupMockContext();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "things");
        parameters.put("includeContent", true);

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("things", data.get("configType"));
        assertNotNull(data.get("configurations"));
    }

    @Test
    void testExecuteWithRulesConfigType() throws AIActionException {
        // Setup
        setupMockContext();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "rules");
        parameters.put("includeContent", true);

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("rules", data.get("configType"));
        assertNotNull(data.get("configurations"));
    }

    @Test
    void testExecuteWithScriptsConfigType() throws AIActionException {
        // Setup
        setupMockContext();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "scripts");
        parameters.put("includeContent", true);

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("scripts", data.get("configType"));
        assertNotNull(data.get("configurations"));
    }

    @Test
    void testExecuteWithAllConfigType() throws AIActionException {
        // Setup
        setupMockContext();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "all");
        parameters.put("includeContent", true);

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("all", data.get("configType"));
        assertNotNull(data.get("configurations"));
    }

    @Test
    void testExecuteWithSpecificConfigName() throws AIActionException {
        // Setup
        setupMockContext();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "items");
        parameters.put("configName", "test_items");
        parameters.put("includeContent", true);

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("items", data.get("configType"));
        assertNotNull(data.get("configurations"));
    }

    @Test
    void testExecuteWithoutContent() throws AIActionException {
        // Setup
        setupMockContext();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "items");
        parameters.put("includeContent", false);

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("items", data.get("configType"));
        assertNotNull(data.get("configurations"));
    }

    @Test
    void testExecuteWithoutMetadata() throws AIActionException {
        // Setup
        setupMockContext();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "items");
        parameters.put("includeMetadata", false);

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("items", data.get("configType"));
        assertNotNull(data.get("configurations"));
    }

    @Test
    void testExecuteAsync() {
        // Setup
        setupMockContext();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "items");

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
    void testExecuteWithInvalidConfigType() {
        // Setup
        setupMockContext();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "invalid_type");

        // Execute and verify exception
        assertThrows(AIActionException.class, () -> {
            action.execute(parameters, mockContext);
        });
    }

    @Test
    void testExecuteWithInvalidParameters() {
        // Should throw exception for invalid parameters
        setupMockContext();
        Map<String, Object> invalidParams = Map.of("invalidParam", "invalidValue");
        assertThrows(AIActionException.class, () -> {
            action.execute(invalidParams, mockContext);
        });
    }

    @Test
    void testExecuteWithInvalidContext() {
        // Setup
        setupMockContext();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "items");
        AIActionContext invalidContext = AIActionContext.builder().build();

        // Should throw exception for invalid context
        assertThrows(AIActionException.class, () -> {
            action.execute(parameters, invalidContext);
        });
    }

    @Test
    void testExecuteAsyncWithInvalidParameters() {
        // Should throw exception for invalid parameters
        setupMockContext();
        Map<String, Object> invalidParams = Map.of("invalidParam", "invalidValue");
        CompletableFuture<AIActionResult> future = action.executeAsync(invalidParams, mockContext);
        assertThrows(CompletionException.class, () -> {
            future.join();
        });
    }

    @Test
    void testExecuteAsyncWithInvalidContext() {
        // Setup
        setupMockContext();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "items");
        AIActionContext invalidContext = AIActionContext.builder().build();

        // Should throw exception for invalid context
        CompletableFuture<AIActionResult> future = action.executeAsync(parameters, invalidContext);
        assertThrows(CompletionException.class, () -> {
            future.join();
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
        assertTrue(properties.containsKey("includeContent"));
        assertTrue(properties.containsKey("includeMetadata"));

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
        assertTrue(properties.containsKey("timestamp"));
        assertTrue(properties.containsKey("configType"));
        assertTrue(properties.containsKey("configurations"));
        assertTrue(properties.containsKey("totalCount"));
        assertTrue(properties.containsKey("confDirectory"));
        assertTrue(properties.containsKey("error"));
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
        assertTrue(capabilities.containsKey("supportsFileAccess"));
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
        setupMockContext();
        assertDoesNotThrow(() -> action.initialize(mockContext));
    }

    @Test
    void testExecuteWithServicesConfigType() throws AIActionException {
        // Setup
        setupMockContext();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "services");
        parameters.put("includeContent", true);

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("services", data.get("configType"));
        assertNotNull(data.get("configurations"));
    }

    @Test
    void testExecuteWithPersistenceConfigType() throws AIActionException {
        // Setup
        setupMockContext();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "persistence");
        parameters.put("includeContent", true);

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("persistence", data.get("configType"));
        assertNotNull(data.get("configurations"));
    }

    @Test
    void testExecuteWithTransformsConfigType() throws AIActionException {
        // Setup
        setupMockContext();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "transforms");
        parameters.put("includeContent", true);

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("transforms", data.get("configType"));
        assertNotNull(data.get("configurations"));
    }

    @Test
    void testExecuteWithSitemapsConfigType() throws AIActionException {
        // Setup
        setupMockContext();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "sitemaps");
        parameters.put("includeContent", true);

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("sitemaps", data.get("configType"));
        assertNotNull(data.get("configurations"));
    }
}
