package org.openhab.core.ai.common.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.PersistenceServiceRegistry;

/**
 * Test utilities for AIAction testing.
 * Provides common mock objects and helper methods for AIAction unit tests.
 */
public class AIActionTestUtils {

    /**
     * Create a mock AIActionContext with basic configuration.
     */
    public static AIActionContext createMockContext() {
        AIActionContext context = mock(AIActionContext.class);

        // Setup basic context properties
        when(context.getProtocol()).thenReturn("a2a");
        when(context.getClientId()).thenReturn("test-client");
        when(context.getSessionId()).thenReturn("test-session");
        when(context.getExecutionStartTime()).thenReturn(System.currentTimeMillis());
        when(context.getCorrelationId()).thenReturn("test-correlation");
        when(context.getProtocolContext()).thenReturn(Map.of());

        return context;
    }

    /**
     * Create a mock ItemRegistry with basic configuration.
     */
    public static ItemRegistry createMockItemRegistry() {
        ItemRegistry registry = mock(ItemRegistry.class);
        return registry;
    }

    /**
     * Create a mock PersistenceServiceRegistry with basic configuration.
     */
    public static PersistenceServiceRegistry createMockPersistenceRegistry() {
        PersistenceServiceRegistry registry = mock(PersistenceServiceRegistry.class);
        return registry;
    }

    /**
     * Create valid parameters for persistence status action.
     */
    public static Map<String, Object> createValidStatusParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "status");
        return parameters;
    }

    /**
     * Create valid parameters for persistence info action.
     */
    public static Map<String, Object> createValidInfoParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "info");
        return parameters;
    }

    /**
     * Create invalid parameters for testing validation.
     */
    public static Map<String, Object> createInvalidParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "invalid_action");
        return parameters;
    }

    /**
     * Create valid parameters for items list action.
     */
    public static Map<String, Object> createValidItemsListParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "all");
        parameters.put("includeState", true);
        return parameters;
    }

    /**
     * Create parameters for items list with type filter.
     */
    public static Map<String, Object> createItemsListWithTypeFilter(String type) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", type);
        return parameters;
    }

    /**
     * Create parameters for items list with state filter.
     */
    public static Map<String, Object> createItemsListWithStateFilter(String state) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("state", state);
        return parameters;
    }

    /**
     * Create parameters for items list with group filter.
     */
    public static Map<String, Object> createItemsListWithGroupFilter(String group) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("group", group);
        return parameters;
    }

    /**
     * Create parameters for items list with tag filter.
     */
    public static Map<String, Object> createItemsListWithTagFilter(String tag) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tag", tag);
        return parameters;
    }

    /**
     * Create parameters for items list with pagination.
     */
    public static Map<String, Object> createItemsListWithPagination(int limit, int offset) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("limit", limit);
        parameters.put("offset", offset);
        return parameters;
    }

    /**
     * Create parameters for items list with sorting.
     */
    public static Map<String, Object> createItemsListWithSorting(String sortBy, String sortOrder) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sortBy", sortBy);
        parameters.put("sortOrder", sortOrder);
        return parameters;
    }

    /**
     * Assert that a persistence status result has the expected structure.
     */
    public static void assertPersistenceStatusResult(Map<String, Object> data) {
        assertNotNull(data);
        assertEquals("status", data.get("action"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("persistenceAvailable"));
        assertNotNull(data.get("message"));
    }

    /**
     * Assert that a persistence info result has the expected structure.
     */
    public static void assertPersistenceInfoResult(Map<String, Object> data) {
        assertNotNull(data);
        assertEquals("info", data.get("action"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("supportedActions"));
        assertNotNull(data.get("message"));
    }

    /**
     * Assert that an items list result has the expected structure.
     */
    public static void assertItemsListResult(Map<String, Object> data) {
        assertNotNull(data);
        assertNotNull(data.get("items"));
        assertNotNull(data.get("totalCount"));
        assertNotNull(data.get("filteredCount"));
        assertNotNull(data.get("typeBreakdown"));
        assertNotNull(data.get("stateBreakdown"));
    }

    /**
     * Create a mock Item with basic configuration.
     */
    public static Item createMockItem(String name, String type, String state) {
        Item item = mock(Item.class);
        when(item.getName()).thenReturn(name);
        when(item.getType()).thenReturn(type);

        org.openhab.core.types.State mockState = mock(org.openhab.core.types.State.class);
        when(mockState.toString()).thenReturn(state);
        when(item.getState()).thenReturn(mockState);

        when(item.getTags()).thenReturn(java.util.Set.of());
        when(item.getGroupNames()).thenReturn(java.util.Set.of());

        return item;
    }

    /**
     * Create a mock Item with tags and groups.
     */
    public static Item createMockItem(String name, String type, String state, java.util.Set<String> tags,
            java.util.Set<String> groups) {
        Item item = createMockItem(name, type, state);
        when(item.getTags()).thenReturn(tags);
        when(item.getGroupNames()).thenReturn(groups);
        return item;
    }

    /**
     * Create a list of mock items for testing.
     */
    public static List<Item> createMockItems() {
        Item item1 = createMockItem("LivingRoom_Light", "Switch", "ON", java.util.Set.of("lighting", "livingroom"),
                java.util.Set.of("Lights", "LivingRoom"));

        Item item2 = createMockItem("Kitchen_Temperature", "Number", "22.5", java.util.Set.of("temperature", "kitchen"),
                java.util.Set.of("Sensors", "Kitchen"));

        Item item3 = createMockItem("Bedroom_Dimmer", "Dimmer", "ON", java.util.Set.of("lighting", "bedroom"),
                java.util.Set.of("Lights", "Bedroom"));

        return List.of(item1, item2, item3);
    }

    /**
     * Assert that a validation result is valid.
     */
    public static void assertValidValidationResult(AIActionValidationResult result) {
        assertTrue(result.isValid());
        assertNotNull(result.getSanitizedParameters());
        assertTrue(result.getErrors().isEmpty());
    }

    /**
     * Assert that a validation result is invalid with specific errors.
     */
    public static void assertInvalidValidationResult(AIActionValidationResult result, String... expectedErrors) {
        assertFalse(result.isValid());
        assertFalse(result.getErrors().isEmpty());

        for (String expectedError : expectedErrors) {
            assertTrue(result.getErrors().stream().anyMatch(error -> error.contains(expectedError)),
                    "Expected error containing: " + expectedError);
        }
    }

    /**
     * Assert that a validation result has warnings.
     */
    public static void assertValidationResultWithWarnings(AIActionValidationResult result, String... expectedWarnings) {
        assertFalse(result.getWarnings().isEmpty());

        for (String expectedWarning : expectedWarnings) {
            assertTrue(result.getWarnings().stream().anyMatch(warning -> warning.contains(expectedWarning)),
                    "Expected warning containing: " + expectedWarning);
        }
    }
}
