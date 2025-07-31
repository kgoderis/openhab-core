package org.openhab.core.ai.common.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.actions.items.ListItemsAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.State;

@ExtendWith(MockitoExtension.class)
class ListItemsActionTest {

    @Mock
    private AIActionContext mockContext;

    @Mock
    private ItemRegistry mockItemRegistry;

    @Mock
    private ItemChannelLinkRegistry mockItemChannelLinkRegistry;

    @Mock
    private MetadataRegistry mockMetadataRegistry;

    @Mock
    private Item mockItem1;

    @Mock
    private Item mockItem2;

    @Mock
    private Item mockItem3;

    @Mock
    private State mockState1;

    @Mock
    private State mockState2;

    @Mock
    private ItemChannelLink mockChannelLink;

    private Metadata mockMetadata;

    private ListItemsAction action;

    @BeforeEach
    void setUp() {
        action = new ListItemsAction();

        // Setup mock items
        when(mockItem1.getName()).thenReturn("LivingRoom_Light");
        when(mockItem1.getType()).thenReturn("Switch");
        when(mockItem1.getState()).thenReturn(mockState1);
        when(mockState1.toString()).thenReturn("ON");
        when(mockItem1.getTags()).thenReturn(java.util.Set.of("lighting", "livingroom"));
        when(mockItem1.getGroupNames()).thenReturn(java.util.List.of("Lights", "LivingRoom"));

        when(mockItem2.getName()).thenReturn("Kitchen_Temperature");
        when(mockItem2.getType()).thenReturn("Number");
        when(mockItem2.getState()).thenReturn(mockState2);
        when(mockState2.toString()).thenReturn("22.5");
        when(mockItem2.getTags()).thenReturn(java.util.Set.of("temperature", "kitchen"));
        when(mockItem2.getGroupNames()).thenReturn(java.util.List.of("Sensors", "Kitchen"));

        when(mockItem3.getName()).thenReturn("Bedroom_Dimmer");
        when(mockItem3.getType()).thenReturn("Dimmer");
        when(mockItem3.getState()).thenReturn(mockState1);
        when(mockItem3.getTags()).thenReturn(java.util.Set.of("lighting", "bedroom"));
        when(mockItem3.getGroupNames()).thenReturn(java.util.List.of("Lights", "Bedroom"));

        // Setup mock channel link
        when(mockChannelLink.getItemName()).thenReturn("LivingRoom_Light");
        when(mockChannelLink.getLinkedUID()).thenReturn(new org.openhab.core.thing.ChannelUID("binding:thing:channel"));

        // Setup mock metadata - create a simple mock since Metadata is final
        mockMetadata = new Metadata(new org.openhab.core.items.MetadataKey("test-namespace", "test-item"), "test-value",
                Map.of("key", "value"));

        // Initialize the action
        action.initialize(mockContext);
    }

    @Test
    void testGetActionId() {
        assertEquals("openhab.items.list", action.getActionId());
    }

    @Test
    void testGetActionName() {
        assertEquals("List Items", action.getActionName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(action.getDescription());
        assertTrue(action.getDescription().contains("Items"));
    }

    @Test
    void testGetCategory() {
        assertEquals("items", action.getCategory());
    }

    @Test
    void testGetVersion() {
        assertEquals("1.0.0", action.getVersion());
    }

    @Test
    void testValidateParametersWithValidParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "Switch");
        parameters.put("includeState", true);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getSanitizedParameters());
    }

    @Test
    void testValidateParametersWithInvalidType() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "InvalidType");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("Invalid type")));
    }

    @Test
    void testValidateParametersWithInvalidSortOrder() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sortOrder", "invalid");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("Invalid sort order")));
    }

    @Test
    void testValidateParametersWithInvalidLimit() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("limit", -1);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("Limit must be positive")));
    }

    @Test
    void testValidateParametersWithInvalidOffset() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("offset", -1);

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("Offset must be non-negative")));
    }

    @Test
    void testExecuteListAllItems() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "all");

        when(mockItemRegistry.getItems()).thenReturn(List.of(mockItem1, mockItem2, mockItem3));

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("items"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
        assertEquals(3, items.size());

        // Verify first item
        Map<String, Object> item1 = items.get(0);
        assertEquals("LivingRoom_Light", item1.get("name"));
        assertEquals("Switch", item1.get("type"));
        assertEquals("ON", item1.get("state"));
    }

    @Test
    void testExecuteWithTypeFilter() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "Switch");

        when(mockItemRegistry.getItems()).thenReturn(List.of(mockItem1, mockItem2, mockItem3));

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");

        // Should only have Switch items
        assertEquals(1, items.size());
        assertEquals("Switch", items.get(0).get("type"));
    }

    @Test
    void testExecuteWithStateFilter() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("state", "ON");

        when(mockItemRegistry.getItems()).thenReturn(List.of(mockItem1, mockItem2, mockItem3));

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");

        // Should only have items with state "ON"
        assertEquals(2, items.size());
        items.forEach(item -> assertEquals("ON", item.get("state")));
    }

    @Test
    void testExecuteWithGroupFilter() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("group", "Lights");

        when(mockItemRegistry.getItems()).thenReturn(List.of(mockItem1, mockItem2, mockItem3));

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");

        // Should only have items in "Lights" group
        assertEquals(2, items.size());
        items.forEach(item -> {
            @SuppressWarnings("unchecked")
            List<String> groups = (List<String>) item.get("groups");
            assertTrue(groups.contains("Lights"));
        });
    }

    @Test
    void testExecuteWithTagFilter() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tag", "lighting");

        when(mockItemRegistry.getItems()).thenReturn(List.of(mockItem1, mockItem2, mockItem3));

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");

        // Should only have items with "lighting" tag
        assertEquals(2, items.size());
        items.forEach(item -> {
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) item.get("tags");
            assertTrue(tags.contains("lighting"));
        });
    }

    @Test
    void testExecuteWithIncludeMetadata() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeMetadata", true);

        when(mockItemRegistry.getItems()).thenReturn(List.of(mockItem1));
        when(mockMetadataRegistry.get(new org.openhab.core.items.MetadataKey("namespace", "LivingRoom_Light")))
                .thenReturn(mockMetadata);

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");

        Map<String, Object> item = items.get(0);
        assertTrue(item.containsKey("metadata"));
    }

    @Test
    void testExecuteWithIncludeChannelLinks() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("includeChannelLinks", true);

        when(mockItemRegistry.getItems()).thenReturn(List.of(mockItem1));
        when(mockItemChannelLinkRegistry.getLinks("LivingRoom_Light")).thenReturn(java.util.Set.of(mockChannelLink));

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");

        Map<String, Object> item = items.get(0);
        assertTrue(item.containsKey("channelLinks"));
    }

    @Test
    void testExecuteWithSorting() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sortBy", "name");
        parameters.put("sortOrder", "desc");

        when(mockItemRegistry.getItems()).thenReturn(List.of(mockItem1, mockItem2, mockItem3));

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");

        // Should be sorted by name in descending order
        assertEquals("LivingRoom_Light", items.get(0).get("name"));
        assertEquals("Kitchen_Temperature", items.get(1).get("name"));
        assertEquals("Bedroom_Dimmer", items.get(2).get("name"));
    }

    @Test
    void testExecuteWithPagination() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("limit", 2);
        parameters.put("offset", 1);

        when(mockItemRegistry.getItems()).thenReturn(List.of(mockItem1, mockItem2, mockItem3));

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");

        // Should have 2 items starting from offset 1
        assertEquals(2, items.size());
        assertEquals("Kitchen_Temperature", items.get(0).get("name"));
        assertEquals("Bedroom_Dimmer", items.get(1).get("name"));
    }

    @Test
    void testExecuteWithEmptyRegistry() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();

        when(mockItemRegistry.getItems()).thenReturn(List.of());

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");

        assertTrue(items.isEmpty());
    }

    @Test
    void testExecuteAsync() {
        // Setup
        Map<String, Object> parameters = new HashMap<>();

        when(mockItemRegistry.getItems()).thenReturn(List.of(mockItem1, mockItem2));

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
    void testGetParameterSchema() {
        Map<String, Object> schema = action.getParameterSchema();

        assertNotNull(schema);
        assertEquals("object", schema.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("type"));
        assertTrue(properties.containsKey("state"));
        assertTrue(properties.containsKey("group"));
        assertTrue(properties.containsKey("tag"));
        assertTrue(properties.containsKey("includeMetadata"));
        assertTrue(properties.containsKey("includeChannelLinks"));
        assertTrue(properties.containsKey("includeGroups"));
        assertTrue(properties.containsKey("includeState"));
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
        assertTrue(properties.containsKey("items"));
        assertTrue(properties.containsKey("totalCount"));
        assertTrue(properties.containsKey("filteredCount"));
        assertTrue(properties.containsKey("typeBreakdown"));
        assertTrue(properties.containsKey("stateBreakdown"));
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

    @Test
    void testExecuteWithInvalidParameters() {
        // Should throw exception for invalid parameters
        Map<String, Object> invalidParams = Map.of("invalidParam", "invalidValue");
        assertThrows(AIActionException.class, () -> {
            action.execute(invalidParams, mockContext);
        });
    }

    @Test
    void testExecuteWithInvalidContext() {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        AIActionContext invalidContext = AIActionContext.builder().build();

        // Should throw exception for invalid context
        assertThrows(AIActionException.class, () -> {
            action.execute(parameters, invalidContext);
        });
    }

    @Test
    void testExecuteAsyncWithInvalidParameters() {
        // Should throw exception for invalid parameters
        Map<String, Object> invalidParams = Map.of("invalidParam", "invalidValue");
        assertThrows(AIActionException.class, () -> {
            action.executeAsync(invalidParams, mockContext);
        });
    }

    @Test
    void testExecuteAsyncWithInvalidContext() {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        AIActionContext invalidContext = AIActionContext.builder().build();

        // Should throw exception for invalid context
        assertThrows(AIActionException.class, () -> {
            action.executeAsync(parameters, invalidContext);
        });
    }
}
