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
import org.openhab.core.ai.common.actions.rules.ListRulesAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.automation.RuleManager;
import org.openhab.core.automation.RuleRegistry;

/**
 * Unit tests for ListRulesAction.
 * 
 * Tests cover:
 * - Action metadata (ID, name, description, category, version)
 * - Parameter validation (valid and invalid scenarios)
 * - Execution (rule listing scenarios)
 * - Async execution
 * - Error handling
 * - Schema generation
 * - Capabilities verification
 * - Lifecycle management
 */
@ExtendWith(MockitoExtension.class)
class ListRulesActionTest {

    @Mock
    private AIActionContext mockContext;

    @Mock
    private RuleRegistry mockRuleRegistry;

    @Mock
    private RuleManager mockRuleManager;

    private ListRulesAction action;

    @BeforeEach
    void setUp() {
        action = new ListRulesAction();

        // Setup mock context
        when(mockContext.getProtocol()).thenReturn("mcp");
        when(mockContext.getClientId()).thenReturn("test-client");
        when(mockContext.getSessionId()).thenReturn("test-session");

        // Initialize the action
        action.initialize(mockContext);
    }

    @Test
    void testGetActionId() {
        assertEquals("openhab.rules.list", action.getActionId());
    }

    @Test
    void testGetActionName() {
        assertEquals("List Rules", action.getActionName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(action.getDescription());
        assertTrue(action.getDescription().contains("Lists openHAB Rules"));
    }

    @Test
    void testGetCategory() {
        assertEquals("rules", action.getCategory());
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
    void testValidateParametersWithValidStatus() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", "ENABLED");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidTag() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tag", "automation");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidType() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "core");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidIncludeTriggers() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeTriggers", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidIncludeConditions() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeConditions", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidIncludeActions() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeActions", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidIncludeConfiguration() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeConfiguration", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidSorting() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sortBy", "status");
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
        parameters.put("status", "ENABLED");
        parameters.put("tag", "automation");
        parameters.put("type", "core");
        parameters.put("includeTriggers", true);
        parameters.put("includeConditions", true);
        parameters.put("includeActions", true);
        parameters.put("includeConfiguration", true);
        parameters.put("sortBy", "name");
        parameters.put("sortOrder", "asc");
        parameters.put("limit", 100);
        parameters.put("offset", 0);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithInvalidStatus() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", "INVALID_STATUS");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("status")));
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
        parameters.put("limit", 2000);

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
    void testValidateParametersWithInvalidIncludeTriggers() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeTriggers", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("includeTriggers")));
    }

    @Test
    void testValidateParametersWithInvalidIncludeConditions() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeConditions", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("includeConditions")));
    }

    @Test
    void testValidateParametersWithInvalidIncludeActions() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeActions", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("includeActions")));
    }

    @Test
    void testValidateParametersWithInvalidIncludeConfiguration() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeConfiguration", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("includeConfiguration")));
    }

    @Test
    void testExecuteBasicRuleListing() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("rules"));
        assertNotNull(data.get("totalCount"));
        assertNotNull(data.get("statusBreakdown"));
        assertNotNull(data.get("typeBreakdown"));
    }

    @Test
    void testExecuteRuleListingWithStatusFilter() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", "ENABLED");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("ENABLED", data.get("status"));
        assertNotNull(data.get("rules"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteRuleListingWithTagFilter() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tag", "automation");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("automation", data.get("tag"));
        assertNotNull(data.get("rules"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteRuleListingWithTypeFilter() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "core");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("core", data.get("type"));
        assertNotNull(data.get("rules"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteRuleListingWithIncludeTriggers() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeTriggers", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(true, data.get("includeTriggers"));
        assertNotNull(data.get("rules"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteRuleListingWithIncludeConditions() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeConditions", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(true, data.get("includeConditions"));
        assertNotNull(data.get("rules"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteRuleListingWithIncludeActions() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeActions", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(true, data.get("includeActions"));
        assertNotNull(data.get("rules"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteRuleListingWithIncludeConfiguration() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeConfiguration", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(true, data.get("includeConfiguration"));
        assertNotNull(data.get("rules"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteRuleListingWithSorting() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sortBy", "status");
        parameters.put("sortOrder", "desc");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("status", data.get("sortBy"));
        assertEquals("desc", data.get("sortOrder"));
        assertNotNull(data.get("rules"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteRuleListingWithPagination() throws AIActionException {
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
        assertNotNull(data.get("rules"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteRuleListingWithComplexParameters() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", "ENABLED");
        parameters.put("tag", "automation");
        parameters.put("type", "core");
        parameters.put("includeTriggers", true);
        parameters.put("includeConditions", true);
        parameters.put("includeActions", true);
        parameters.put("includeConfiguration", true);
        parameters.put("sortBy", "name");
        parameters.put("sortOrder", "asc");
        parameters.put("limit", 100);
        parameters.put("offset", 0);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("ENABLED", data.get("status"));
        assertEquals("automation", data.get("tag"));
        assertEquals("core", data.get("type"));
        assertEquals(true, data.get("includeTriggers"));
        assertEquals(true, data.get("includeConditions"));
        assertEquals(true, data.get("includeActions"));
        assertEquals(true, data.get("includeConfiguration"));
        assertEquals("name", data.get("sortBy"));
        assertEquals("asc", data.get("sortOrder"));
        assertEquals(100, data.get("limit"));
        assertEquals(0, data.get("offset"));
        assertNotNull(data.get("rules"));
        assertNotNull(data.get("totalCount"));
        assertNotNull(data.get("statusBreakdown"));
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
        assertTrue(properties.containsKey("status"));
        assertTrue(properties.containsKey("tag"));
        assertTrue(properties.containsKey("type"));
        assertTrue(properties.containsKey("includeTriggers"));
        assertTrue(properties.containsKey("includeConditions"));
        assertTrue(properties.containsKey("includeActions"));
        assertTrue(properties.containsKey("includeConfiguration"));
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
        assertTrue(properties.containsKey("rules"));
        assertTrue(properties.containsKey("totalCount"));
        assertTrue(properties.containsKey("statusBreakdown"));
        assertTrue(properties.containsKey("typeBreakdown"));
        assertTrue(properties.containsKey("status"));
        assertTrue(properties.containsKey("tag"));
        assertTrue(properties.containsKey("type"));
        assertTrue(properties.containsKey("includeTriggers"));
        assertTrue(properties.containsKey("includeConditions"));
        assertTrue(properties.containsKey("includeActions"));
        assertTrue(properties.containsKey("includeConfiguration"));
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
