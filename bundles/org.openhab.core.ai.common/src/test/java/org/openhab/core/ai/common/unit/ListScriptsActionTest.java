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
import org.openhab.core.ai.common.actions.scripts.ListScriptsAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;

/**
 * Unit tests for ListScriptsAction.
 * 
 * Tests cover:
 * - Action metadata (ID, name, description, category, version)
 * - Parameter validation (valid and invalid scenarios)
 * - Execution (script listing scenarios)
 * - Async execution
 * - Error handling
 * - Schema generation
 * - Capabilities verification
 * - Lifecycle management
 */
@ExtendWith(MockitoExtension.class)
class ListScriptsActionTest {

    @Mock
    private AIActionContext mockContext;

    private ListScriptsAction action;

    @BeforeEach
    void setUp() {
        action = new ListScriptsAction();

        // Setup mock context
        when(mockContext.getProtocol()).thenReturn("mcp");
        when(mockContext.getClientId()).thenReturn("test-client");
        when(mockContext.getSessionId()).thenReturn("test-session");

        // Initialize the action
        action.initialize(mockContext);
    }

    @Test
    void testGetActionId() {
        assertEquals("openhab.scripts.list", action.getActionId());
    }

    @Test
    void testGetActionName() {
        assertEquals("List Scripts", action.getActionName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(action.getDescription());
        assertTrue(action.getDescription().contains("Lists openHAB Scripts"));
    }

    @Test
    void testGetCategory() {
        assertEquals("scripts", action.getCategory());
    }

    @Test
    void testGetVersion() {
        assertEquals("1.0.0", action.getVersion());
    }

    @Test
    void testValidateParametersWithValidBasicParameters() {
        Map<String, Object> parameters = new HashMap<>();

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidScriptType() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("scriptType", "js");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidNameContains() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("nameContains", "test");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidIncludeContent() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeContent", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidIncludeMetadata() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeMetadata", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidMaxContentSize() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("maxContentSize", 50000);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidSorting() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sortBy", "size");
        parameters.put("sortOrder", "desc");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidPagination() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("limit", 50);
        parameters.put("offset", 10);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidComplexParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("scriptType", "js");
        parameters.put("nameContains", "test");
        parameters.put("includeContent", true);
        parameters.put("includeMetadata", true);
        parameters.put("maxContentSize", 50000);
        parameters.put("sortBy", "modified");
        parameters.put("sortOrder", "desc");
        parameters.put("limit", 100);
        parameters.put("offset", 0);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithInvalidScriptType() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("scriptType", "invalid_type");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("scriptType")));
    }

    @Test
    void testValidateParametersWithInvalidSortBy() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sortBy", "invalid_field");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("sortBy")));
    }

    @Test
    void testValidateParametersWithInvalidSortOrder() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sortOrder", "invalid_order");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("sortOrder")));
    }

    @Test
    void testValidateParametersWithInvalidMaxContentSize() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("maxContentSize", 500);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("maxContentSize")));
    }

    @Test
    void testValidateParametersWithInvalidMaxContentSizeTooHigh() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("maxContentSize", 2000000);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("maxContentSize")));
    }

    @Test
    void testValidateParametersWithInvalidLimit() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("limit", 0);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("limit")));
    }

    @Test
    void testValidateParametersWithInvalidLimitTooHigh() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("limit", 1000);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("limit")));
    }

    @Test
    void testValidateParametersWithInvalidOffset() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("offset", -1);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("offset")));
    }

    @Test
    void testValidateParametersWithInvalidIncludeContent() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeContent", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("includeContent")));
    }

    @Test
    void testValidateParametersWithInvalidIncludeMetadata() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeMetadata", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("includeMetadata")));
    }

    @Test
    void testExecuteBasicScriptListing() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("scripts"));
        assertNotNull(data.get("totalCount"));
        assertNotNull(data.get("typeBreakdown"));
    }

    @Test
    void testExecuteScriptListingWithScriptTypeFilter() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("scriptType", "js");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("js", data.get("scriptType"));
        assertNotNull(data.get("scripts"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteScriptListingWithNameContains() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("nameContains", "test");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("test", data.get("nameContains"));
        assertNotNull(data.get("scripts"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteScriptListingWithIncludeContent() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeContent", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(true, data.get("includeContent"));
        assertNotNull(data.get("scripts"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteScriptListingWithIncludeMetadata() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeMetadata", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(true, data.get("includeMetadata"));
        assertNotNull(data.get("scripts"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteScriptListingWithMaxContentSize() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("maxContentSize", 50000);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(50000, data.get("maxContentSize"));
        assertNotNull(data.get("scripts"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteScriptListingWithSorting() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sortBy", "size");
        parameters.put("sortOrder", "desc");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("size", data.get("sortBy"));
        assertEquals("desc", data.get("sortOrder"));
        assertNotNull(data.get("scripts"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteScriptListingWithPagination() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("limit", 50);
        parameters.put("offset", 10);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(50, data.get("limit"));
        assertEquals(10, data.get("offset"));
        assertNotNull(data.get("scripts"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteScriptListingWithComplexParameters() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("scriptType", "js");
        parameters.put("nameContains", "test");
        parameters.put("includeContent", true);
        parameters.put("includeMetadata", true);
        parameters.put("maxContentSize", 50000);
        parameters.put("sortBy", "modified");
        parameters.put("sortOrder", "desc");
        parameters.put("limit", 100);
        parameters.put("offset", 0);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("js", data.get("scriptType"));
        assertEquals("test", data.get("nameContains"));
        assertEquals(true, data.get("includeContent"));
        assertEquals(true, data.get("includeMetadata"));
        assertEquals(50000, data.get("maxContentSize"));
        assertEquals("modified", data.get("sortBy"));
        assertEquals("desc", data.get("sortOrder"));
        assertEquals(100, data.get("limit"));
        assertEquals(0, data.get("offset"));
        assertNotNull(data.get("scripts"));
        assertNotNull(data.get("totalCount"));
        assertNotNull(data.get("typeBreakdown"));
    }

    @Test
    void testExecuteAsync() {
        Map<String, Object> parameters = new HashMap<>();

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
        assertTrue(properties.containsKey("scriptType"));
        assertTrue(properties.containsKey("nameContains"));
        assertTrue(properties.containsKey("includeContent"));
        assertTrue(properties.containsKey("includeMetadata"));
        assertTrue(properties.containsKey("maxContentSize"));
        assertTrue(properties.containsKey("sortBy"));
        assertTrue(properties.containsKey("sortOrder"));
        assertTrue(properties.containsKey("limit"));
        assertTrue(properties.containsKey("offset"));
    }

    @Test
    void testGetReturnSchema() {
        Map<String, Object> schema = action.getReturnSchema();

        assertNotNull(schema);
        assertEquals("object", schema.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("scripts"));
        assertTrue(properties.containsKey("totalCount"));
        assertTrue(properties.containsKey("typeBreakdown"));
        assertTrue(properties.containsKey("scriptType"));
        assertTrue(properties.containsKey("nameContains"));
        assertTrue(properties.containsKey("includeContent"));
        assertTrue(properties.containsKey("includeMetadata"));
        assertTrue(properties.containsKey("maxContentSize"));
        assertTrue(properties.containsKey("sortBy"));
        assertTrue(properties.containsKey("sortOrder"));
        assertTrue(properties.containsKey("limit"));
        assertTrue(properties.containsKey("offset"));
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
        assertTrue(capabilities.containsKey("supportsSorting"));
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
