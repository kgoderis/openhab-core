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
import org.openhab.core.ai.common.actions.things.ListThingsAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.type.ThingTypeRegistry;

/**
 * Unit tests for ListThingsAction.
 * 
 * Tests cover:
 * - Action metadata (ID, name, description, category, version)
 * - Parameter validation (valid and invalid scenarios)
 * - Execution (thing listing scenarios)
 * - Async execution
 * - Error handling
 * - Schema generation
 * - Capabilities verification
 * - Lifecycle management
 */
@ExtendWith(MockitoExtension.class)
class ListThingsActionTest {

    @Mock
    private AIActionContext mockContext;

    @Mock
    private ThingRegistry mockThingRegistry;

    @Mock
    private ThingTypeRegistry mockThingTypeRegistry;

    private ListThingsAction action;

    @BeforeEach
    void setUp() {
        action = new ListThingsAction();

        // Setup mock context
        when(mockContext.getProtocol()).thenReturn("mcp");
        when(mockContext.getClientId()).thenReturn("test-client");
        when(mockContext.getSessionId()).thenReturn("test-session");

        // Initialize the action
        action.initialize(mockContext);
    }

    @Test
    void testGetActionId() {
        assertEquals("openhab.things.list", action.getActionId());
    }

    @Test
    void testGetActionName() {
        assertEquals("List Things", action.getActionName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(action.getDescription());
        assertTrue(action.getDescription().contains("Lists openHAB Things"));
    }

    @Test
    void testGetCategory() {
        assertEquals("things", action.getCategory());
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
    void testValidateParametersWithValidStatusFilter() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", "ONLINE");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidBindingFilter() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("binding", "zwave");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidThingTypeFilter() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("thingType", "zwave:device");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidBridgeUIDFilter() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("bridgeUID", "zwave:controller:123");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidLocationFilter() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("location", "Living Room");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidEnabledFilter() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("enabled", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidIncludeOptions() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeChannels", true);
        parameters.put("includeConfiguration", true);
        parameters.put("includeProperties", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidSorting() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sortBy", "label");
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
    void testValidateParametersWithValidComplexFilters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", "ONLINE");
        parameters.put("binding", "zwave");
        parameters.put("enabled", true);
        parameters.put("includeChannels", true);
        parameters.put("sortBy", "status");
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
        parameters.put("limit", 1500);

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
    void testValidateParametersWithInvalidIncludeChannels() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeChannels", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("includeChannels")));
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
    void testValidateParametersWithInvalidIncludeProperties() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeProperties", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("includeProperties")));
    }

    @Test
    void testValidateParametersWithInvalidEnabled() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("enabled", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("enabled")));
    }

    @Test
    void testExecuteBasicThingListing() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("things"));
        assertNotNull(data.get("totalCount"));
        assertNotNull(data.get("statusBreakdown"));
        assertNotNull(data.get("bindingBreakdown"));
    }

    @Test
    void testExecuteThingListingWithStatusFilter() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", "ONLINE");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("things"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteThingListingWithBindingFilter() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("binding", "zwave");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("things"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteThingListingWithIncludeChannels() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeChannels", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("things"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteThingListingWithIncludeConfiguration() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeConfiguration", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("things"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteThingListingWithIncludeProperties() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeProperties", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("things"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteThingListingWithSorting() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sortBy", "label");
        parameters.put("sortOrder", "desc");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("things"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteThingListingWithPagination() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("limit", 50);
        parameters.put("offset", 10);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("things"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteThingListingWithComplexFilters() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", "ONLINE");
        parameters.put("binding", "zwave");
        parameters.put("enabled", true);
        parameters.put("includeChannels", true);
        parameters.put("includeConfiguration", true);
        parameters.put("includeProperties", true);
        parameters.put("sortBy", "status");
        parameters.put("sortOrder", "asc");
        parameters.put("limit", 100);
        parameters.put("offset", 0);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("things"));
        assertNotNull(data.get("totalCount"));
        assertNotNull(data.get("statusBreakdown"));
        assertNotNull(data.get("bindingBreakdown"));
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
        assertTrue(properties.containsKey("binding"));
        assertTrue(properties.containsKey("thingType"));
        assertTrue(properties.containsKey("bridgeUID"));
        assertTrue(properties.containsKey("location"));
        assertTrue(properties.containsKey("enabled"));
        assertTrue(properties.containsKey("includeChannels"));
        assertTrue(properties.containsKey("includeConfiguration"));
        assertTrue(properties.containsKey("includeProperties"));
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
        assertTrue(properties.containsKey("things"));
        assertTrue(properties.containsKey("totalCount"));
        assertTrue(properties.containsKey("statusBreakdown"));
        assertTrue(properties.containsKey("bindingBreakdown"));
        assertTrue(properties.containsKey("filters"));
        assertTrue(properties.containsKey("sorting"));
        assertTrue(properties.containsKey("pagination"));
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
