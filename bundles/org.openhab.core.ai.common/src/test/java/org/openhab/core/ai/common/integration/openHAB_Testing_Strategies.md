# openHAB Testing Strategies Guide

## Overview

This guide provides comprehensive strategies for testing AI actions against openHAB, covering different approaches from mocked data structures to live instances, with practical examples and best practices.

## 1. Testing Approaches Overview

### 1.1 Mocked Data Structures (Unit Testing)
- **Use Case**: Fast, isolated unit tests
- **Pros**: Fast execution, no external dependencies, predictable results
- **Cons**: May not catch real-world integration issues
- **Best For**: Individual action logic, parameter validation, error handling

### 1.2 Embedded openHAB (Integration Testing)
- **Use Case**: Testing with real openHAB services in controlled environment
- **Pros**: Real service behavior, no external dependencies, good isolation
- **Cons**: More complex setup, slower than pure unit tests
- **Best For**: Service integration, data consistency, workflow testing

### 1.3 Live openHAB Instance (End-to-End Testing)
- **Use Case**: Full system testing with real openHAB instance
- **Pros**: Most realistic testing, catches all integration issues
- **Cons**: Requires running openHAB, slower, more complex
- **Best For**: End-to-end workflows, performance testing, real-world scenarios

## 2. Mocked Data Structures Approach

### 2.1 Mocked ItemRegistry Example

```java
package org.openhab.core.ai.common.actions.items;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;

@ExtendWith(MockitoExtension.class)
class ListItemsActionMockedTest {

    @Mock
    private AIActionContext mockContext;

    @Mock
    private ItemRegistry mockItemRegistry;

    private ListItemsAction action;
    private Item mockSwitchItem;
    private Item mockNumberItem;
    private Item mockStringItem;

    @BeforeEach
    void setUp() {
        action = new ListItemsAction();
        
        // Setup mock context
        when(mockContext.getService(ItemRegistry.class))
            .thenReturn(mockItemRegistry);
        
        // Create mock items
        mockSwitchItem = new SwitchItem("LivingRoom_Light");
        mockNumberItem = new NumberItem("Kitchen_Temperature");
        mockStringItem = new StringItem("Weather_Status");
        
        // Initialize action
        action.initialize(mockContext);
    }

    @Test
    void testListAllItems() throws AIActionException {
        // Setup mock data
        when(mockItemRegistry.getItems())
            .thenReturn(List.of(mockSwitchItem, mockNumberItem, mockStringItem));

        // Execute action
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify result
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.getData().get("items");
        assertNotNull(items);
        assertEquals(3, items.size());
        
        // Verify item data
        Map<String, Object> switchItem = items.get(0);
        assertEquals("LivingRoom_Light", switchItem.get("name"));
        assertEquals("Switch", switchItem.get("type"));
        
        Map<String, Object> numberItem = items.get(1);
        assertEquals("Kitchen_Temperature", numberItem.get("name"));
        assertEquals("Number", numberItem.get("type"));
    }

    @Test
    void testListItemsByType() throws AIActionException {
        // Setup mock data
        when(mockItemRegistry.getItems())
            .thenReturn(List.of(mockSwitchItem, mockNumberItem, mockStringItem));

        // Execute action with type filter
        Map<String, Object> parameters = Map.of("filter", "type", "type", "Switch");
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify result
        assertTrue(result.isSuccess());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.getData().get("items");
        assertEquals(1, items.size());
        assertEquals("Switch", items.get(0).get("type"));
    }

    @Test
    void testListItemsByNamePattern() throws AIActionException {
        // Setup mock data
        when(mockItemRegistry.getItems())
            .thenReturn(List.of(mockSwitchItem, mockNumberItem, mockStringItem));

        // Execute action with name filter
        Map<String, Object> parameters = Map.of("filter", "name", "name", "LivingRoom");
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify result
        assertTrue(result.isSuccess());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.getData().get("items");
        assertEquals(1, items.size());
        assertTrue(items.get(0).get("name").toString().contains("LivingRoom"));
    }

    @Test
    void testEmptyItemRegistry() throws AIActionException {
        // Setup empty mock data
        when(mockItemRegistry.getItems())
            .thenReturn(List.of());

        // Execute action
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify result
        assertTrue(result.isSuccess());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.getData().get("items");
        assertTrue(items.isEmpty());
    }

    @Test
    void testItemRegistryUnavailable() throws AIActionException {
        // Setup null service
        when(mockContext.getService(ItemRegistry.class))
            .thenReturn(null);

        // Execute action
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify graceful handling
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertTrue(result.getData().containsKey("error"));
    }
}
```

### 2.2 Mocked ThingRegistry Example

```java
package org.openhab.core.ai.common.actions.things;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingStatus;

@ExtendWith(MockitoExtension.class)
class ListThingsActionMockedTest {

    @Mock
    private AIActionContext mockContext;

    @Mock
    private ThingRegistry mockThingRegistry;

    @Mock
    private Thing mockThing1;

    @Mock
    private Thing mockThing2;

    private ListThingsAction action;

    @BeforeEach
    void setUp() {
        action = new ListThingsAction();
        
        when(mockContext.getService(ThingRegistry.class))
            .thenReturn(mockThingRegistry);
        
        // Setup mock things
        ThingUID thingUID1 = new ThingUID("hue:bridge:livingroom");
        ThingUID thingUID2 = new ThingUID("zwave:device:kitchen");
        
        when(mockThing1.getUID()).thenReturn(thingUID1);
        when(mockThing1.getLabel()).thenReturn("Philips Hue Bridge");
        when(mockThing1.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(mockThing1.getThingTypeUID()).thenReturn(new ThingTypeUID("hue", "bridge"));
        
        when(mockThing2.getUID()).thenReturn(thingUID2);
        when(mockThing2.getLabel()).thenReturn("Z-Wave Switch");
        when(mockThing2.getStatus()).thenReturn(ThingStatus.OFFLINE);
        when(mockThing2.getThingTypeUID()).thenReturn(new ThingTypeUID("zwave", "device"));
        
        action.initialize(mockContext);
    }

    @Test
    void testListAllThings() throws AIActionException {
        // Setup mock data
        when(mockThingRegistry.getAll())
            .thenReturn(List.of(mockThing1, mockThing2));

        // Execute action
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify result
        assertTrue(result.isSuccess());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> things = (List<Map<String, Object>>) result.getData().get("things");
        assertEquals(2, things.size());
        
        // Verify thing data
        Map<String, Object> thing1 = things.get(0);
        assertEquals("hue:bridge:livingroom", thing1.get("uid"));
        assertEquals("Philips Hue Bridge", thing1.get("label"));
        assertEquals("ONLINE", thing1.get("status"));
    }

    @Test
    void testListThingsByStatus() throws AIActionException {
        // Setup mock data
        when(mockThingRegistry.getAll())
            .thenReturn(List.of(mockThing1, mockThing2));

        // Execute action with status filter
        Map<String, Object> parameters = Map.of("filter", "status", "status", "ONLINE");
        AIActionResult result = action.execute(parameters, mockContext);

        // Verify result
        assertTrue(result.isSuccess());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> things = (List<Map<String, Object>>) result.getData().get("things");
        assertEquals(1, things.size());
        assertEquals("ONLINE", things.get(0).get("status"));
    }
}
```

## 3. Embedded openHAB Approach

### 3.1 How Embedded openHAB Works

**Key Point**: In the embedded openHAB approach, openHAB is **NOT explicitly started** as a separate process. Instead, it's **embedded within the test JVM** using OSGi container testing.

#### 3.1.1 OSGi Container Startup Process

When you use `@ExtendWith(JavaOSGiTest.class)`, here's what happens:

1. **OSGi Container Initialization**: The `JavaOSGiTest` extension automatically starts an OSGi container (typically Equinox or Felix) within the test JVM
2. **Bundle Deployment**: Required openHAB core bundles are automatically deployed to the OSGi container
3. **Service Registration**: OSGi services (like `ItemRegistry`, `ThingRegistry`, etc.) are automatically registered
4. **Service Injection**: The `@InjectService` annotation automatically injects available OSGi services into your test
5. **Lifecycle Management**: The container manages the lifecycle of all bundles and services

#### 3.1.2 What Gets Started Automatically

The `JavaOSGiTest` framework automatically starts:

- **OSGi Framework**: Equinox or Felix OSGi container
- **Core openHAB Bundles**: Essential openHAB services and registries
- **Test Dependencies**: Any bundles required by your test classes
- **Service Registry**: OSGi service registry for dependency injection

#### 3.1.3 Configuration and Setup

```java
// The @ExtendWith annotation triggers the OSGi container startup
@ExtendWith(JavaOSGiTest.class)
class ItemsActionOSGiTest {

    // Services are automatically injected when available
    @InjectService
    private AIActionContext actionContext;

    @InjectService
    private ItemRegistry itemRegistry;

    @InjectService
    private ServiceComponentRuntime scr;

    @BeforeEach
    void setUp() throws Exception {
        // Wait for services to be ready (OSGi container startup)
        waitForServices();
        
        // Setup test data in the embedded environment
        setupTestItems();
    }

    private void waitForServices() throws InterruptedException {
        // Wait for OSGi services to be ready
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 30000) {
            if (itemRegistry != null && actionContext != null) {
                return;
            }
            Thread.sleep(100);
        }
        throw new RuntimeException("Services not ready within timeout");
    }
}
```

### 3.2 OSGi Container Testing

```java
package org.openhab.core.ai.common.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.test.java.JavaOSGiTest;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;

@ExtendWith(JavaOSGiTest.class)
class ItemsActionOSGiTest {

    @InjectService
    private AIActionContext actionContext;

    @InjectService
    private ItemRegistry itemRegistry;

    @InjectService
    private ServiceComponentRuntime scr;

    private ListItemsAction action;

    @BeforeEach
    void setUp() throws Exception {
        action = new ListItemsAction();
        action.initialize(actionContext);
        
        // Wait for services to be ready
        waitForServices();
        
        // Setup test data
        setupTestItems();
    }

    @Test
    void testListItemsWithRealRegistry() throws AIActionException {
        // Execute action
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = action.execute(parameters, actionContext);

        // Verify result
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.getData().get("items");
        assertNotNull(items);
        assertTrue(items.size() >= 2); // At least our test items
        
        // Verify test items are present
        boolean hasTestSwitch = false;
        boolean hasTestNumber = false;
        
        for (Map<String, Object> item : items) {
            String name = (String) item.get("name");
            if ("TestSwitch".equals(name)) hasTestSwitch = true;
            if ("TestNumber".equals(name)) hasTestNumber = true;
        }
        
        assertTrue(hasTestSwitch, "Test switch item should be present");
        assertTrue(hasTestNumber, "Test number item should be present");
    }

    @Test
    void testItemOperationsWithRealRegistry() throws AIActionException {
        // Test item state operations
        Map<String, Object> parameters = Map.of(
            "action", "setState",
            "itemName", "TestSwitch",
            "state", "ON"
        );
        
        AIActionResult result = action.execute(parameters, actionContext);
        assertTrue(result.isSuccess());
        
        // Verify state was set
        Item testItem = itemRegistry.get("TestSwitch");
        assertNotNull(testItem);
        assertEquals("ON", testItem.getState().toString());
    }

    private void waitForServices() throws InterruptedException {
        // Wait for OSGi services to be ready
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 30000) {
            if (itemRegistry != null && actionContext != null) {
                return;
            }
            Thread.sleep(100);
        }
        throw new RuntimeException("Services not ready within timeout");
    }

    private void setupTestItems() {
        // Add test items to registry
        try {
            SwitchItem testSwitch = new SwitchItem("TestSwitch");
            NumberItem testNumber = new NumberItem("TestNumber");
            
            // Note: In real OSGi environment, items are typically added through ItemProvider
            // This is a simplified example for testing
            itemRegistry.add(testSwitch);
            itemRegistry.add(testNumber);
            
            // Wait for items to be registered
            Thread.sleep(1000);
        } catch (Exception e) {
            // Handle item registration errors
            System.err.println("Failed to setup test items: " + e.getMessage());
        }
    }
}
```

### 3.3 Embedded openHAB with Test Data

```java
package org.openhab.core.ai.common.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.config.core.ConfigConstants;
import org.openhab.core.test.java.JavaOSGiTest;
import org.osgi.test.common.annotation.InjectService;

@ExtendWith(JavaOSGiTest.class)
class PersistenceActionOSGiTest {

    @TempDir
    Path tempDir;

    @InjectService
    private AIActionContext actionContext;

    private PersistenceAction action;

    @BeforeEach
    void setUp() throws Exception {
        // Setup openHAB configuration directory
        System.setProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT, tempDir.toString());
        
        // Create test configuration
        setupTestConfiguration();
        
        action = new PersistenceAction();
        action.initialize(actionContext);
    }

    @Test
    void testPersistenceWithRealServices() throws AIActionException {
        // Execute persistence action
        Map<String, Object> parameters = Map.of("action", "status");
        AIActionResult result = action.execute(parameters, actionContext);

        // Verify result
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        
        Map<String, Object> data = result.getData();
        assertNotNull(data.get("persistenceAvailable"));
        assertNotNull(data.get("persistenceServices"));
    }

    @Test
    void testPersistenceDataOperations() throws AIActionException {
        // Test data operations with real persistence services
        Map<String, Object> parameters = Map.of(
            "action", "query",
            "itemName", "TestItem",
            "startTime", "2024-01-01T00:00:00Z",
            "endTime", "2024-01-02T00:00:00Z"
        );
        
        AIActionResult result = action.execute(parameters, actionContext);
        assertTrue(result.isSuccess());
        
        // Verify query result structure
        Map<String, Object> data = result.getData();
        assertNotNull(data.get("data"));
        assertNotNull(data.get("count"));
    }

    private void setupTestConfiguration() throws Exception {
        // Create test items configuration
        Path itemsDir = tempDir.resolve("items");
        Files.createDirectories(itemsDir);
        
        String itemsConfig = """
            Switch TestSwitch "Test Switch" <switch>
            Number TestNumber "Test Number" <temperature>
            String TestString "Test String" <text>
            """;
        
        Files.write(itemsDir.resolve("test.items"), itemsConfig.getBytes());
        
        // Create test persistence configuration
        Path persistenceDir = tempDir.resolve("persistence");
        Files.createDirectories(persistenceDir);
        
        String persistenceConfig = """
            Strategies {
                default = everyChange
            }
            
            Items {
                TestSwitch, TestNumber, TestString : strategy = everyChange
            }
            """;
        
        Files.write(persistenceDir.resolve("rrd4j.persist"), persistenceConfig.getBytes());
    }
}
```

### 3.4 Required Dependencies for Embedded Testing

```xml
<dependencies>
    <!-- OSGi Testing Framework -->
    <dependency>
        <groupId>org.osgi</groupId>
        <artifactId>org.osgi.test.junit5</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- openHAB Testing Framework -->
    <dependency>
        <groupId>org.openhab.core.bundles</groupId>
        <artifactId>org.openhab.core.test</artifactId>
        <version>${project.version}</version>
        <scope>test</scope>
    </dependency>
    
    <!-- OSGi Service Component Runtime -->
    <dependency>
        <groupId>org.osgi</groupId>
        <artifactId>org.osgi.service.component.runtime</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## 4. Live openHAB Instance Testing

### 4.1 Remote openHAB Testing

```java
package org.openhab.core.ai.common.integration.live;

import static org.junit.jupiter.api.Assertions.*;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.test.java.JavaOSGiTest;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(JavaOSGiTest.class)
class LiveOpenHABIntegrationTest {

    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private String openhabUrl = "http://localhost:8080";
    private String openhabToken = System.getenv("OPENHAB_TOKEN");

    private ListItemsAction action;
    private AIActionContext actionContext;

    @BeforeEach
    void setUp() throws Exception {
        httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        objectMapper = new ObjectMapper();
        
        // Verify openHAB is running
        assertTrue(isOpenHABRunning(), "openHAB should be running");
        
        // Setup action with live context
        actionContext = createLiveActionContext();
        action = new ListItemsAction();
        action.initialize(actionContext);
    }

    @Test
    void testListItemsFromLiveOpenHAB() throws AIActionException {
        // Execute action against live openHAB
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = action.execute(parameters, actionContext);

        // Verify result
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.getData().get("items");
        assertNotNull(items);
        assertTrue(items.size() > 0, "Should have items in live openHAB");
        
        // Log items for debugging
        System.out.println("Found " + items.size() + " items in live openHAB:");
        items.forEach(item -> System.out.println("  - " + item.get("name") + " (" + item.get("type") + ")"));
    }

    @Test
    void testItemStateOperations() throws AIActionException {
        // Test setting item state in live openHAB
        Map<String, Object> parameters = Map.of(
            "action", "setState",
            "itemName", "TestSwitch",
            "state", "ON"
        );
        
        AIActionResult result = action.execute(parameters, actionContext);
        assertTrue(result.isSuccess());
        
        // Verify state was set by querying openHAB REST API
        String state = getItemState("TestSwitch");
        assertEquals("ON", state);
    }

    @Test
    void testPersistenceWithLiveData() throws AIActionException {
        // Test persistence operations with live data
        PersistenceAction persistenceAction = new PersistenceAction();
        persistenceAction.initialize(actionContext);
        
        Map<String, Object> parameters = Map.of(
            "action", "query",
            "itemName", "TestNumber",
            "startTime", "2024-01-01T00:00:00Z",
            "endTime", "2024-12-31T23:59:59Z"
        );
        
        AIActionResult result = persistenceAction.execute(parameters, actionContext);
        assertTrue(result.isSuccess());
        
        // Verify persistence data
        Map<String, Object> data = result.getData();
        assertNotNull(data.get("data"));
        assertNotNull(data.get("count"));
    }

    private boolean isOpenHABRunning() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(openhabUrl + "/rest/"))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private AIActionContext createLiveActionContext() {
        // Create action context that connects to live openHAB
        return new AIActionContext() {
            @Override
            public <T> T getService(Class<T> serviceClass) {
                // Return live service implementations
                if (serviceClass == ItemRegistry.class) {
                    return createLiveItemRegistry();
                }
                if (serviceClass == ThingRegistry.class) {
                    return createLiveThingRegistry();
                }
                if (serviceClass == PersistenceServiceRegistry.class) {
                    return createLivePersistenceRegistry();
                }
                return null;
            }
        };
    }

    private ItemRegistry createLiveItemRegistry() {
        return new ItemRegistry() {
            @Override
            public Collection<Item> getItems() {
                try {
                    // Query live openHAB REST API
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(openhabUrl + "/rest/items"))
                        .header("Authorization", "Bearer " + openhabToken)
                        .GET()
                        .build();

                    HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        // Parse items from JSON response
                        return parseItemsFromJson(response.body());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return List.of();
            }
            
            // Implement other methods...
        };
    }

    private String getItemState(String itemName) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(openhabUrl + "/rest/items/" + itemName + "/state"))
                .header("Authorization", "Bearer " + openhabToken)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Item> parseItemsFromJson(String json) {
        // Parse items from openHAB REST API JSON response
        // Implementation depends on openHAB REST API format
        return List.of(); // Placeholder
    }
}
```

### 4.2 Docker-based Live Testing

```java
package org.openhab.core.ai.common.integration.docker;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.test.java.JavaOSGiTest;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ExtendWith(JavaOSGiTest.class)
@Testcontainers
class DockerOpenHABIntegrationTest {

    @Container
    private static GenericContainer<?> openhabContainer = new GenericContainer<>("openhab/openhab:4.0.0")
        .withExposedPorts(8080, 8443)
        .withEnv("EXTRA_JAVA_OPTS", "-Dopenhab.configdir=/openhab/conf")
        .withFileSystemBind("src/test/resources/openhab-config", "/openhab/conf")
        .waitingFor(Wait.forHttp("/rest/").forPort(8080).withStartupTimeout(Duration.ofMinutes(5)));

    private static String openhabUrl;
    private AIActionContext actionContext;

    @BeforeAll
    static void setUpContainer() {
        openhabContainer.start();
        openhabUrl = "http://" + openhabContainer.getHost() + ":" + openhabContainer.getMappedPort(8080);
        
        // Wait for openHAB to be fully ready
        waitForOpenHABReady();
    }

    @Test
    void testWithDockerOpenHAB() throws AIActionException {
        // Create action context for Docker openHAB
        actionContext = createDockerActionContext();
        
        ListItemsAction action = new ListItemsAction();
        action.initialize(actionContext);
        
        // Execute action
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = action.execute(parameters, actionContext);

        // Verify result
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.getData().get("items");
        assertNotNull(items);
        
        // Verify Docker openHAB has expected test items
        boolean hasTestItem = items.stream()
            .anyMatch(item -> "TestItem".equals(item.get("name")));
        assertTrue(hasTestItem, "Docker openHAB should have test item");
    }

    private static void waitForOpenHABReady() {
        // Wait for openHAB to be fully initialized
        try {
            Thread.sleep(30000); // Wait 30 seconds for full startup
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private AIActionContext createDockerActionContext() {
        // Create action context that connects to Docker openHAB
        return new AIActionContext() {
            @Override
            public <T> T getService(Class<T> serviceClass) {
                // Return service implementations that connect to Docker openHAB
                if (serviceClass == ItemRegistry.class) {
                    return createDockerItemRegistry();
                }
                // Add other services as needed
                return null;
            }
        };
    }

    private ItemRegistry createDockerItemRegistry() {
        return new ItemRegistry() {
            @Override
            public Collection<Item> getItems() {
                try {
                    // Query Docker openHAB REST API
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(openhabUrl + "/rest/items"))
                        .GET()
                        .build();

                    HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        return parseItemsFromJson(response.body());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return List.of();
            }
            
            // Implement other methods...
        };
    }
}
```

## 5. Hybrid Testing Strategy

### 5.1 Test Configuration Management

```java
package org.openhab.core.ai.common.test.config;

import java.util.Properties;

public class TestConfiguration {
    
    public enum TestMode {
        MOCKED,      // Use mocked services
        EMBEDDED,    // Use embedded openHAB
        LIVE,        // Use live openHAB instance
        DOCKER       // Use Docker openHAB
    }
    
    private static Properties config;
    private static TestMode testMode;
    
    static {
        config = new Properties();
        String mode = System.getProperty("test.mode", "MOCKED");
        testMode = TestMode.valueOf(mode.toUpperCase());
        
        loadConfiguration();
    }
    
    public static TestMode getTestMode() {
        return testMode;
    }
    
    public static boolean isMockedMode() {
        return testMode == TestMode.MOCKED;
    }
    
    public static boolean isEmbeddedMode() {
        return testMode == TestMode.EMBEDDED;
    }
    
    public static boolean isLiveMode() {
        return testMode == TestMode.LIVE;
    }
    
    public static boolean isDockerMode() {
        return testMode == TestMode.DOCKER;
    }
    
    public static String getOpenHABUrl() {
        return config.getProperty("openhab.url", "http://localhost:8080");
    }
    
    public static String getOpenHABToken() {
        return config.getProperty("openhab.token", "");
    }
    
    private static void loadConfiguration() {
        // Load configuration based on test mode
        String configFile = "test-config-" + testMode.name().toLowerCase() + ".properties";
        // Load configuration file
    }
}
```

### 5.2 Conditional Test Execution

```java
package org.openhab.core.ai.common.test.conditional;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.test.config.TestConfiguration;
import org.openhab.core.test.java.JavaOSGiTest;

@ExtendWith(JavaOSGiTest.class)
class ConditionalItemsActionTest {

    private AIActionContext actionContext;
    private ListItemsAction action;

    @BeforeEach
    void setUp() throws Exception {
        actionContext = createActionContext();
        action = new ListItemsAction();
        action.initialize(actionContext);
    }

    @Test
    void testListItems() throws AIActionException {
        // This test runs in all modes
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = action.execute(parameters, actionContext);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    @ConditionalTest(mode = TestConfiguration.TestMode.LIVE)
    void testLiveSpecificFeatures() throws AIActionException {
        // This test only runs in LIVE mode
        Map<String, Object> parameters = Map.of(
            "action", "setState",
            "itemName", "LiveTestItem",
            "state", "ON"
        );
        
        AIActionResult result = action.execute(parameters, actionContext);
        assertTrue(result.isSuccess());
        
        // Verify with live openHAB REST API
        String state = getLiveItemState("LiveTestItem");
        assertEquals("ON", state);
    }

    @Test
    @ConditionalTest(mode = TestConfiguration.TestMode.EMBEDDED)
    void testEmbeddedSpecificFeatures() throws AIActionException {
        // This test only runs in EMBEDDED mode
        // Test features that require embedded openHAB
    }

    @Test
    @ConditionalTest(mode = TestConfiguration.TestMode.MOCKED)
    void testMockedSpecificFeatures() throws AIActionException {
        // This test only runs in MOCKED mode
        // Test error conditions and edge cases
    }

    private AIActionContext createActionContext() {
        switch (TestConfiguration.getTestMode()) {
            case MOCKED:
                return createMockedContext();
            case EMBEDDED:
                return createEmbeddedContext();
            case LIVE:
                return createLiveContext();
            case DOCKER:
                return createDockerContext();
            default:
                throw new IllegalArgumentException("Unknown test mode: " + TestConfiguration.getTestMode());
        }
    }

    private AIActionContext createMockedContext() {
        // Create mocked context with Mockito
        return mock(AIActionContext.class);
    }

    private AIActionContext createEmbeddedContext() {
        // Create embedded OSGi context
        return getEmbeddedActionContext();
    }

    private AIActionContext createLiveContext() {
        // Create live openHAB context
        return createLiveActionContext();
    }

    private AIActionContext createDockerContext() {
        // Create Docker openHAB context
        return createDockerActionContext();
    }
}
```

## 6. Best Practices

### 6.1 Test Mode Selection
- **Unit Tests**: Use MOCKED mode for fast, isolated testing
- **Integration Tests**: Use EMBEDDED mode for service integration testing
- **End-to-End Tests**: Use LIVE or DOCKER mode for full system testing
- **CI/CD**: Use DOCKER mode for consistent, reproducible testing

### 6.2 Performance Considerations
- **MOCKED**: Fastest (milliseconds)
- **EMBEDDED**: Medium (seconds)
- **LIVE/DOCKER**: Slowest (tens of seconds to minutes)

### 6.3 Reliability Considerations
- **MOCKED**: Most reliable, no external dependencies
- **EMBEDDED**: Reliable, controlled environment
- **LIVE**: May be flaky due to external factors
- **DOCKER**: Reliable but requires Docker infrastructure

### 6.4 Test Data Management
- **MOCKED**: Use predefined test data
- **EMBEDDED**: Create test data in controlled environment
- **LIVE**: Use existing data or create test data carefully
- **DOCKER**: Use containerized test data

This comprehensive approach allows you to choose the right testing strategy based on your needs, balancing speed, reliability, and realism. 