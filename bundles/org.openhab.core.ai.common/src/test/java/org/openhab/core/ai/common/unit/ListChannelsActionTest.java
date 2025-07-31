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
import org.openhab.core.ai.common.actions.channels.ListChannelsAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeRegistry;

/**
 * Unit tests for ListChannelsAction.
 * 
 * Tests cover:
 * - Action metadata (ID, name, description, category, version)
 * - Parameter validation (valid and invalid scenarios)
 * - Execution (channel listing scenarios)
 * - Async execution
 * - Error handling
 * - Schema generation
 * - Capabilities verification
 * - Lifecycle management
 */
@ExtendWith(MockitoExtension.class)
class ListChannelsActionTest {

    @Mock
    private AIActionContext mockContext;

    @Mock
    private ThingRegistry mockThingRegistry;

    @Mock
    private ItemChannelLinkRegistry mockItemChannelLinkRegistry;

    @Mock
    private ChannelTypeRegistry mockChannelTypeRegistry;

    private ListChannelsAction action;

    @BeforeEach
    void setUp() {
        action = new ListChannelsAction();

        // Setup mock context
        when(mockContext.getProtocol()).thenReturn("mcp");
        when(mockContext.getClientId()).thenReturn("test-client");
        when(mockContext.getSessionId()).thenReturn("test-session");

        // Initialize the action
        action.initialize(mockContext);
    }

    @Test
    void testGetActionId() {
        assertEquals("openhab.channels.list", action.getActionId());
    }

    @Test
    void testGetActionName() {
        assertEquals("List Channels", action.getActionName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(action.getDescription());
        assertTrue(action.getDescription().contains("Lists openHAB Channels"));
    }

    @Test
    void testGetCategory() {
        assertEquals("channels", action.getCategory());
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
    void testValidateParametersWithValidThingUIDFilter() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("thingUID", "zwave:device:123");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidChannelTypeFilter() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("channelType", "zwave:switch");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidAcceptedItemTypeFilter() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("acceptedItemType", "Switch");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidKindFilter() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("kind", "STATE");

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
    void testValidateParametersWithValidLinkedOnlyFilter() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("linkedOnly", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidUnlinkedOnlyFilter() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("unlinkedOnly", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithValidIncludeOptions() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeConfiguration", true);
        parameters.put("includeChannelType", true);
        parameters.put("includeLinkedItems", true);

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
        parameters.put("thingUID", "zwave:device:123");
        parameters.put("kind", "STATE");
        parameters.put("linkedOnly", true);
        parameters.put("includeConfiguration", true);
        parameters.put("sortBy", "label");
        parameters.put("sortOrder", "asc");
        parameters.put("limit", 100);
        parameters.put("offset", 0);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithInvalidKind() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("kind", "INVALID_KIND");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("kind")));
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
    void testValidateParametersWithInvalidLinkedOnly() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("linkedOnly", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("linkedOnly")));
    }

    @Test
    void testValidateParametersWithInvalidUnlinkedOnly() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("unlinkedOnly", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("unlinkedOnly")));
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
    void testValidateParametersWithInvalidIncludeChannelType() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeChannelType", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("includeChannelType")));
    }

    @Test
    void testValidateParametersWithInvalidIncludeLinkedItems() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeLinkedItems", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("includeLinkedItems")));
    }

    @Test
    void testExecuteBasicChannelListing() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("channels"));
        assertNotNull(data.get("totalCount"));
        assertNotNull(data.get("kindBreakdown"));
        assertNotNull(data.get("typeBreakdown"));
        assertNotNull(data.get("bindingBreakdown"));
    }

    @Test
    void testExecuteChannelListingWithThingUIDFilter() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("thingUID", "zwave:device:123");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("channels"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteChannelListingWithKindFilter() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("kind", "STATE");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("channels"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteChannelListingWithLinkedOnlyFilter() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("linkedOnly", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("channels"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteChannelListingWithUnlinkedOnlyFilter() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("unlinkedOnly", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("channels"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteChannelListingWithIncludeConfiguration() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeConfiguration", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("channels"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteChannelListingWithIncludeChannelType() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeChannelType", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("channels"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteChannelListingWithIncludeLinkedItems() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeLinkedItems", true);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("channels"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteChannelListingWithSorting() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sortBy", "label");
        parameters.put("sortOrder", "desc");

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("channels"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteChannelListingWithPagination() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("limit", 50);
        parameters.put("offset", 10);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("channels"));
        assertNotNull(data.get("totalCount"));
    }

    @Test
    void testExecuteChannelListingWithComplexFilters() throws AIActionException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("thingUID", "zwave:device:123");
        parameters.put("kind", "STATE");
        parameters.put("linkedOnly", true);
        parameters.put("includeConfiguration", true);
        parameters.put("includeChannelType", true);
        parameters.put("includeLinkedItems", true);
        parameters.put("sortBy", "label");
        parameters.put("sortOrder", "asc");
        parameters.put("limit", 100);
        parameters.put("offset", 0);

        AIActionResult result = action.execute(parameters, mockContext);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("channels"));
        assertNotNull(data.get("totalCount"));
        assertNotNull(data.get("kindBreakdown"));
        assertNotNull(data.get("typeBreakdown"));
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
        assertTrue(properties.containsKey("thingUID"));
        assertTrue(properties.containsKey("channelType"));
        assertTrue(properties.containsKey("acceptedItemType"));
        assertTrue(properties.containsKey("kind"));
        assertTrue(properties.containsKey("binding"));
        assertTrue(properties.containsKey("linkedOnly"));
        assertTrue(properties.containsKey("unlinkedOnly"));
        assertTrue(properties.containsKey("includeConfiguration"));
        assertTrue(properties.containsKey("includeChannelType"));
        assertTrue(properties.containsKey("includeLinkedItems"));
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
        assertTrue(properties.containsKey("channels"));
        assertTrue(properties.containsKey("totalCount"));
        assertTrue(properties.containsKey("kindBreakdown"));
        assertTrue(properties.containsKey("typeBreakdown"));
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
