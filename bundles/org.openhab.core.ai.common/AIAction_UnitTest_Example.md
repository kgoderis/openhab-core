# AIAction Unit Test Implementation Example

## Overview

This document provides concrete examples of how to implement unit tests for AIActions in the openHAB AI bundles.

## Example: PersistenceAction Unit Test

### 1. Test Class Structure

```java
package org.openhab.core.ai.common.actions.persistence;

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
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;

@ExtendWith(MockitoExtension.class)
class PersistenceActionTest {

    @Mock
    private AIActionContext mockContext;

    @Mock
    private PersistenceServiceRegistry mockPersistenceRegistry;

    @Mock
    private QueryablePersistenceService mockPersistenceService;

    private PersistenceAction action;

    @BeforeEach
    void setUp() {
        action = new PersistenceAction();
        
        // Setup mock context with required services
        when(mockContext.getService(PersistenceServiceRegistry.class))
            .thenReturn(mockPersistenceRegistry);
        
        // Initialize the action
        action.initialize(mockContext);
    }

    @Test
    void testGetActionId() {
        assertEquals("openhab.persistence.manage", action.getActionId());
    }

    @Test
    void testGetActionName() {
        assertEquals("Persistence Management", action.getActionName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(action.getDescription());
        assertTrue(action.getDescription().contains("persistence"));
    }

    @Test
    void testGetCategory() {
        assertEquals("persistence", action.getCategory());
    }

    @Test
    void testGetVersion() {
        assertEquals("1.0.0", action.getVersion());
    }

    @Test
    void testValidateParametersWithValidAction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "status");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertTrue(result.isValid());
        assertEquals(parameters, result.getValidatedParameters());
    }

    @Test
    void testValidateParametersWithMissingAction() {
        Map<String, Object> parameters = new HashMap<>();
        // Missing "action" parameter

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Missing required parameter: action"));
    }

    @Test
    void testValidateParametersWithInvalidAction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "invalid_action");

        AIActionValidationResult result = action.validateParameters(parameters);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Invalid action. Must be one of: [status, info]"));
    }

    @Test
    void testExecuteStatusAction() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "status");

        when(mockPersistenceRegistry.getPersistenceServices())
            .thenReturn(java.util.List.of("rrd4j", "influxdb"));

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        
        Map<String, Object> data = result.getData();
        assertEquals("status", data.get("action"));
        assertTrue((Boolean) data.get("persistenceAvailable"));
        assertNotNull(data.get("persistenceServices"));
        assertNotNull(data.get("timestamp"));
    }

    @Test
    void testExecuteInfoAction() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "info");

        when(mockPersistenceRegistry.getPersistenceServices())
            .thenReturn(java.util.List.of("rrd4j"));

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        
        Map<String, Object> data = result.getData();
        assertEquals("info", data.get("action"));
        assertNotNull(data.get("supportedActions"));
        assertNotNull(data.get("timestamp"));
    }

    @Test
    void testExecuteAsync() {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "status");

        when(mockPersistenceRegistry.getPersistenceServices())
            .thenReturn(java.util.List.of("rrd4j"));

        // Execute
        CompletableFuture<AIActionResult> future = action.executeAsync(parameters, mockContext);

        // Verify
        assertNotNull(future);
        assertTrue(future.isDone());
        
        AIActionResult result = future.join();
        assertTrue(result.isSuccess());
    }

    @Test
    void testExecuteWithServiceUnavailable() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "status");

        when(mockContext.getService(PersistenceServiceRegistry.class))
            .thenReturn(null); // Service unavailable

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess()); // Should handle gracefully
        assertNotNull(result.getData());
        
        Map<String, Object> data = result.getData();
        assertFalse((Boolean) data.get("persistenceAvailable"));
    }

    @Test
    void testExecuteWithException() {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "status");

        when(mockPersistenceRegistry.getPersistenceServices())
            .thenThrow(new RuntimeException("Service error"));

        // Execute and verify exception
        assertThrows(AIActionException.class, () -> {
            action.execute(parameters, mockContext);
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
        assertTrue(properties.containsKey("action"));
        
        @SuppressWarnings("unchecked")
        java.util.List<String> required = (java.util.List<String>) schema.get("required");
        assertNotNull(required);
        assertTrue(required.contains("action"));
    }

    @Test
    void testGetReturnSchema() {
        Map<String, Object> schema = action.getReturnSchema();

        assertNotNull(schema);
        assertEquals("object", schema.get("type"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("action"));
        assertTrue(properties.containsKey("timestamp"));
        assertTrue(properties.containsKey("persistenceAvailable"));
    }

    @Test
    void testGetMetadata() {
        var metadata = action.getMetadata();

        assertNotNull(metadata);
        assertEquals("openhab.persistence.manage", metadata.getActionId());
        assertEquals("Persistence Management", metadata.getActionName());
        assertEquals("persistence", metadata.getCategory());
        assertEquals("1.0.0", metadata.getVersion());
    }

    @Test
    void testGetCapabilities() {
        Map<String, Object> capabilities = action.getCapabilities();

        assertNotNull(capabilities);
        assertTrue(capabilities.containsKey("supportsAsync"));
        assertTrue(capabilities.containsKey("supportsValidation"));
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
}
```

### 2. Test Utilities

```java
package org.openhab.core.ai.common.test.utils;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;

public class PersistenceActionTestUtils {

    public static AIActionContext createMockContext() {
        AIActionContext context = mock(AIActionContext.class);
        PersistenceServiceRegistry registry = mock(PersistenceServiceRegistry.class);
        
        when(context.getService(PersistenceServiceRegistry.class))
            .thenReturn(registry);
        
        return context;
    }

    public static Map<String, Object> createValidStatusParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "status");
        return parameters;
    }

    public static Map<String, Object> createValidInfoParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "info");
        return parameters;
    }

    public static Map<String, Object> createInvalidParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action", "invalid_action");
        return parameters;
    }

    public static void assertPersistenceStatusResult(Map<String, Object> data) {
        assertNotNull(data);
        assertEquals("status", data.get("action"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("persistenceAvailable"));
        assertNotNull(data.get("persistenceServices"));
    }

    public static void assertPersistenceInfoResult(Map<String, Object> data) {
        assertNotNull(data);
        assertEquals("info", data.get("action"));
        assertNotNull(data.get("timestamp"));
        assertNotNull(data.get("supportedActions"));
    }
}
```

### 3. Test Data Factories

```java
package org.openhab.core.ai.common.test.factories;

import java.util.List;
import java.util.Map;

import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;

public class PersistenceTestDataFactory {

    public static List<String> createPersistenceServices() {
        return List.of("rrd4j", "influxdb", "jdbc");
    }

    public static Map<String, Object> createPersistenceStatusData() {
        Map<String, Object> data = new HashMap<>();
        data.put("action", "status");
        data.put("timestamp", "2024-01-01T00:00:00Z");
        data.put("persistenceAvailable", true);
        data.put("persistenceServices", createPersistenceServices());
        data.put("message", "Persistence services available");
        return data;
    }

    public static Map<String, Object> createPersistenceInfoData() {
        Map<String, Object> data = new HashMap<>();
        data.put("action", "info");
        data.put("timestamp", "2024-01-01T00:00:00Z");
        data.put("supportedActions", List.of("status", "info"));
        data.put("message", "Persistence information retrieved");
        return data;
    }

    public static void setupMockPersistenceRegistry(PersistenceServiceRegistry registry) {
        when(registry.getPersistenceServices())
            .thenReturn(createPersistenceServices());
    }
}
```

## Example: ItemsAction Unit Test

```java
package org.openhab.core.ai.common.actions.items;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;

@ExtendWith(MockitoExtension.class)
class ListItemsActionTest {

    @Mock
    private AIActionContext mockContext;

    @Mock
    private ItemRegistry mockItemRegistry;

    @Mock
    private Item mockItem1;

    @Mock
    private Item mockItem2;

    private ListItemsAction action;

    @BeforeEach
    void setUp() {
        action = new ListItemsAction();
        
        when(mockContext.getService(ItemRegistry.class))
            .thenReturn(mockItemRegistry);
        
        action.initialize(mockContext);
    }

    @Test
    void testExecuteListAllItems() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("filter", "all");

        when(mockItemRegistry.getItems())
            .thenReturn(List.of(mockItem1, mockItem2));
        when(mockItem1.getName()).thenReturn("LivingRoom_Light");
        when(mockItem1.getType()).thenReturn("Switch");
        when(mockItem2.getName()).thenReturn("Kitchen_Temperature");
        when(mockItem2.getType()).thenReturn("Number");

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.getData().get("items");
        assertNotNull(items);
        assertEquals(2, items.size());
        
        Map<String, Object> item1 = items.get(0);
        assertEquals("LivingRoom_Light", item1.get("name"));
        assertEquals("Switch", item1.get("type"));
    }

    @Test
    void testExecuteWithTypeFilter() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("filter", "type");
        parameters.put("type", "Switch");

        when(mockItemRegistry.getItems())
            .thenReturn(List.of(mockItem1));
        when(mockItem1.getName()).thenReturn("LivingRoom_Light");
        when(mockItem1.getType()).thenReturn("Switch");

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.getData().get("items");
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals("Switch", items.get(0).get("type"));
    }

    @Test
    void testExecuteWithNameFilter() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("filter", "name");
        parameters.put("name", "LivingRoom");

        when(mockItemRegistry.getItems())
            .thenReturn(List.of(mockItem1));
        when(mockItem1.getName()).thenReturn("LivingRoom_Light");
        when(mockItem1.getType()).thenReturn("Switch");

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.getData().get("items");
        assertNotNull(items);
        assertEquals(1, items.size());
        assertTrue(items.get(0).get("name").toString().contains("LivingRoom"));
    }

    @Test
    void testExecuteWithEmptyRegistry() throws AIActionException {
        // Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("filter", "all");

        when(mockItemRegistry.getItems())
            .thenReturn(List.of());

        // Execute
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.getData().get("items");
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    void testValidateParameters() {
        // Test valid parameters
        Map<String, Object> validParams = new HashMap<>();
        validParams.put("filter", "all");
        
        var result = action.validateParameters(validParams);
        assertTrue(result.isValid());

        // Test invalid filter
        Map<String, Object> invalidParams = new HashMap<>();
        invalidParams.put("filter", "invalid");
        
        result = action.validateParameters(invalidParams);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Invalid filter type"));
    }
}
```

## Best Practices for AIAction Unit Tests

### 1. Test Structure
- Use descriptive test method names
- Follow AAA pattern (Arrange, Act, Assert)
- Test both success and failure scenarios
- Test edge cases and boundary conditions

### 2. Mocking Strategy
- Mock external dependencies (openHAB services)
- Don't mock the action under test
- Use realistic mock data
- Verify mock interactions when relevant

### 3. Parameter Testing
- Test valid parameters
- Test invalid parameters
- Test missing required parameters
- Test parameter validation logic

### 4. Execution Testing
- Test synchronous execution
- Test asynchronous execution
- Test exception handling
- Test service availability scenarios

### 5. Schema Testing
- Test parameter schema generation
- Test return schema generation
- Verify schema consistency with implementation

### 6. Coverage Goals
- Aim for 80%+ line coverage
- Test all public methods
- Test all code branches
- Test error conditions

This example demonstrates a comprehensive approach to testing AIActions with proper mocking, validation testing, and realistic scenarios. 