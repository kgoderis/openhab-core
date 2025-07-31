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
import org.openhab.core.ai.common.actions.filesystem.ReadFileAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;

/**
 * Unit tests for ReadFileAction.
 * 
 * Tests cover:
 * - Action metadata (ID, name, description, category, version)
 * - Parameter validation (valid and invalid scenarios)
 * - Execution (file reading scenarios)
 * - Async execution
 * - Error handling
 * - Schema generation
 * - Capabilities verification
 * - Lifecycle management
 */
@ExtendWith(MockitoExtension.class)
class ReadFileActionTest {

    @Mock
    private AIActionContext mockContext;

    private ReadFileAction action;

    @BeforeEach
    void setUp() {
        action = new ReadFileAction();

        // Setup mock context
        when(mockContext.getProtocol()).thenReturn("mcp");
        when(mockContext.getClientId()).thenReturn("test-client");
        when(mockContext.getSessionId()).thenReturn("test-session");

        // Initialize the action
        action.initialize(mockContext);
    }

    @Test
    void testGetActionId() {
        assertEquals("read_file", action.getActionId());
    }

    @Test
    void testGetActionName() {
        assertEquals("Read File", action.getActionName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(action.getDescription());
        assertTrue(action.getDescription().contains("Reads file contents within the openHAB root folder"));
    }

    @Test
    void testGetCategory() {
        assertEquals("filesystem", action.getCategory());
    }

    @Test
    void testGetVersion() {
        assertEquals("1.0.0", action.getVersion());
    }

    @Test
    void testValidateParametersWithValidBasicParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("path", "conf/test.txt");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidAdvancedParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("path", "conf/test.txt");
        parameters.put("encoding", "UTF-8");
        parameters.put("maxSize", 1048576);
        parameters.put("includeMetadata", true);
        parameters.put("lineNumbers", true);
        parameters.put("startLine", 1);
        parameters.put("endLine", 100);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithMissingPath() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("encoding", "UTF-8");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("path")));
    }

    @Test
    void testValidateParametersWithEmptyPath() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("path", "");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("path")));
    }

    @Test
    void testValidateParametersWithInvalidEncoding() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("path", "conf/test.txt");
        parameters.put("encoding", "INVALID_ENCODING");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("encoding")));
    }

    @Test
    void testValidateParametersWithInvalidMaxSize() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("path", "conf/test.txt");
        parameters.put("maxSize", -1);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("maxSize")));
    }

    @Test
    void testValidateParametersWithInvalidMaxSizeTooHigh() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("path", "conf/test.txt");
        parameters.put("maxSize", 104857601);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("maxSize")));
    }

    @Test
    void testValidateParametersWithInvalidStartLine() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("path", "conf/test.txt");
        parameters.put("startLine", 0);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("startLine")));
    }

    @Test
    void testValidateParametersWithInvalidEndLine() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("path", "conf/test.txt");
        parameters.put("endLine", 0);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("endLine")));
    }

    @Test
    void testValidateParametersWithInvalidIncludeMetadata() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("path", "conf/test.txt");
        parameters.put("includeMetadata", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("includeMetadata")));
    }

    @Test
    void testValidateParametersWithInvalidLineNumbers() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("path", "conf/test.txt");
        parameters.put("lineNumbers", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("lineNumbers")));
    }

    @Test
    void testExecuteBasicFileRead() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("path", "conf/test.txt");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("conf/test.txt", data.get("path"));
        assertNotNull(data.get("content"));
        assertNotNull(data.get("encoding"));
        assertNotNull(data.get("size"));
    }

    @Test
    void testExecuteFileReadWithEncoding() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("path", "conf/test.txt");
        parameters.put("encoding", "UTF-8");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("conf/test.txt", data.get("path"));
        assertEquals("UTF-8", data.get("encoding"));
    }

    @Test
    void testExecuteFileReadWithMetadata() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("path", "conf/test.txt");
        parameters.put("includeMetadata", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("conf/test.txt", data.get("path"));
        assertNotNull(data.get("metadata"));
    }

    @Test
    void testExecuteFileReadWithLineNumbers() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("path", "conf/test.txt");
        parameters.put("lineNumbers", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("conf/test.txt", data.get("path"));
        assertNotNull(data.get("content"));
    }

    @Test
    void testExecuteFileReadWithLineRange() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("path", "conf/test.txt");
        parameters.put("startLine", 1);
        parameters.put("endLine", 10);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("conf/test.txt", data.get("path"));
        assertNotNull(data.get("content"));
    }

    @Test
    void testExecuteAsync() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("path", "conf/test.txt");

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
        parameters.put("path", "conf/test.txt");
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
        parameters.put("path", "conf/test.txt");
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
        assertTrue(properties.containsKey("path"));
        assertTrue(properties.containsKey("encoding"));
        assertTrue(properties.containsKey("maxSize"));
        assertTrue(properties.containsKey("includeMetadata"));
        assertTrue(properties.containsKey("lineNumbers"));
        assertTrue(properties.containsKey("startLine"));
        assertTrue(properties.containsKey("endLine"));

        @SuppressWarnings("unchecked")
        java.util.List<String> required = (java.util.List<String>) schema.get("required");
        assertNotNull(required);
        assertTrue(required.contains("path"));
    }

    @Test
    void testGetReturnSchema() {
        Map<String, Object> schema = action.getReturnSchema();

        assertNotNull(schema);
        assertEquals("object", schema.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("path"));
        assertTrue(properties.containsKey("content"));
        assertTrue(properties.containsKey("encoding"));
        assertTrue(properties.containsKey("size"));
        assertTrue(properties.containsKey("metadata"));
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
        assertTrue(capabilities.containsKey("supportsEncoding"));
        assertTrue(capabilities.containsKey("supportsMetadata"));
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
