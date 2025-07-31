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
import org.openhab.core.ai.common.actions.monitoring.GetLogsAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;

/**
 * Unit tests for GetLogsAction.
 * 
 * Tests cover:
 * - Action metadata (ID, name, description, category, version)
 * - Parameter validation (valid and invalid scenarios)
 * - Execution (log retrieval scenarios)
 * - Async execution
 * - Error handling
 * - Schema generation
 * - Capabilities verification
 * - Lifecycle management
 */
@ExtendWith(MockitoExtension.class)
class GetLogsActionTest {

    @Mock
    private AIActionContext mockContext;

    private GetLogsAction action;

    @BeforeEach
    void setUp() {
        action = new GetLogsAction();

        // Setup mock context
        when(mockContext.getProtocol()).thenReturn("mcp");
        when(mockContext.getClientId()).thenReturn("test-client");
        when(mockContext.getSessionId()).thenReturn("test-session");

        // Initialize the action
        action.initialize(mockContext);
    }

    @Test
    void testGetActionId() {
        assertEquals("openhab.monitoring.get_logs", action.getActionId());
    }

    @Test
    void testGetActionName() {
        assertEquals("Get Logs", action.getActionName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(action.getDescription());
        assertTrue(action.getDescription().contains("Retrieves logs from openHAB log files"));
    }

    @Test
    void testGetCategory() {
        assertEquals("monitoring", action.getCategory());
    }

    @Test
    void testGetVersion() {
        assertEquals("1.0.0", action.getVersion());
    }

    @Test
    void testValidateParametersWithValidBasicParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("logFile", "openhab.log");
        parameters.put("maxLines", 100);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidAdvancedParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("logFile", "events.log");
        parameters.put("maxLines", 500);
        parameters.put("logLevel", "ERROR");
        parameters.put("searchPattern", ".*error.*");
        parameters.put("timePeriod", "24h");
        parameters.put("includeStackTrace", true);
        parameters.put("tail", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithInvalidLogLevel() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("logFile", "openhab.log");
        parameters.put("logLevel", "INVALID_LEVEL");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("logLevel")));
    }

    @Test
    void testValidateParametersWithInvalidMaxLines() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("logFile", "openhab.log");
        parameters.put("maxLines", -1);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("maxLines")));
    }

    @Test
    void testValidateParametersWithInvalidMaxLinesTooHigh() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("logFile", "openhab.log");
        parameters.put("maxLines", 15000);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("maxLines")));
    }

    @Test
    void testValidateParametersWithInvalidStartLine() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("logFile", "openhab.log");
        parameters.put("startLine", 0);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("startLine")));
    }

    @Test
    void testValidateParametersWithInvalidIncludeStackTrace() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("logFile", "openhab.log");
        parameters.put("includeStackTrace", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("includeStackTrace")));
    }

    @Test
    void testValidateParametersWithInvalidTail() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("logFile", "openhab.log");
        parameters.put("tail", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("tail")));
    }

    @Test
    void testExecuteBasicLogRetrieval() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("logFile", "openhab.log");
        parameters.put("maxLines", 100);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("openhab.log", data.get("logFile"));
        assertNotNull(data.get("logLines"));
        assertNotNull(data.get("totalLines"));
        assertNotNull(data.get("returnedLines"));
        assertNotNull(data.get("filters"));
    }

    @Test
    void testExecuteLogRetrievalWithFilters() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("logFile", "events.log");
        parameters.put("maxLines", 50);
        parameters.put("logLevel", "ERROR");
        parameters.put("searchPattern", ".*error.*");
        parameters.put("timePeriod", "1h");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("events.log", data.get("logFile"));
        assertNotNull(data.get("logLines"));
        assertNotNull(data.get("filters"));
    }

    @Test
    void testExecuteLogRetrievalWithTail() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("logFile", "openhab.log");
        parameters.put("maxLines", 200);
        parameters.put("tail", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("openhab.log", data.get("logFile"));
        assertNotNull(data.get("logLines"));
    }

    @Test
    void testExecuteLogRetrievalWithStartLine() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("logFile", "openhab.log");
        parameters.put("maxLines", 100);
        parameters.put("tail", false);
        parameters.put("startLine", 1000);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("openhab.log", data.get("logFile"));
        assertNotNull(data.get("logLines"));
    }

    @Test
    void testExecuteLogRetrievalWithStackTrace() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("logFile", "openhab.log");
        parameters.put("maxLines", 50);
        parameters.put("includeStackTrace", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("openhab.log", data.get("logFile"));
        assertNotNull(data.get("logLines"));
    }

    @Test
    void testExecuteAsync() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("logFile", "openhab.log");
        parameters.put("maxLines", 100);

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
        parameters.put("logFile", "openhab.log");
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
        parameters.put("logFile", "openhab.log");
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
        assertTrue(properties.containsKey("logFile"));
        assertTrue(properties.containsKey("maxLines"));
        assertTrue(properties.containsKey("logLevel"));
        assertTrue(properties.containsKey("searchPattern"));
        assertTrue(properties.containsKey("timePeriod"));
        assertTrue(properties.containsKey("includeStackTrace"));
        assertTrue(properties.containsKey("tail"));
        assertTrue(properties.containsKey("startLine"));
    }

    @Test
    void testGetReturnSchema() {
        Map<String, Object> schema = action.getReturnSchema();

        assertNotNull(schema);
        assertEquals("object", schema.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("logFile"));
        assertTrue(properties.containsKey("logLines"));
        assertTrue(properties.containsKey("totalLines"));
        assertTrue(properties.containsKey("returnedLines"));
        assertTrue(properties.containsKey("filters"));
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
        assertTrue(capabilities.containsKey("supportsFiltering"));
        assertTrue(capabilities.containsKey("supportsPagination"));
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
