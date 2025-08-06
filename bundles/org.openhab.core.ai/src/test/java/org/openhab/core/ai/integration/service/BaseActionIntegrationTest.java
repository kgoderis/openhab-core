package org.openhab.core.ai.integration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.CallItem;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.ImageItem;
import org.openhab.core.library.items.LocationItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.PlayerItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.type.ThingTypeRegistry;

/**
 * Base class for Action integration tests using mocked openHAB services.
 * 
 * This class provides common setup and utilities for testing Actions
 * with mocked openHAB services for unit testing. It uses Mockito to mock
 * the required openHAB services (ItemRegistry, ThingRegistry, etc.) and
 * provides test data and utility methods for comprehensive testing.
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseActionIntegrationTest {

    protected ActionContext actionContext;
    protected Path testDataDir;
    protected Map<String, Object> testParameters;

    @Mock
    protected ItemRegistry itemRegistry;

    @Mock
    protected ThingRegistry thingRegistry;

    @Mock
    protected ThingTypeRegistry thingTypeRegistry;

    @BeforeEach
    void setUp(TestInfo testInfo) throws IOException, InterruptedException {
        // Create test data directory
        testDataDir = Files.createTempDirectory("Action-test-" + testInfo.getTestMethod().get().getName());

        // Set openHAB configuration directory
        System.setProperty("openhab.configdir", testDataDir.toString());

        // Wait for OSGi services to be ready
        waitForServices();

        // Initialize test parameters
        testParameters = new HashMap<>();

        // Create ActionContext with test configuration
        actionContext = createTestActionContext();

        // Setup test environment
        setupTestEnvironment();

        // Setup test data
        setupTestData();
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    /**
     * Wait for required services to be available.
     */
    protected void waitForServices() throws InterruptedException {
        // In mocked environment, services are immediately available
        // This method is kept for compatibility but does nothing
    }

    /**
     * Create a test ActionContext with embedded openHAB services.
     */
    protected ActionContext createTestActionContext() {
        Map<String, Object> protocolContext = new HashMap<>();
        protocolContext.put("testDataDir", testDataDir.toString());
        protocolContext.put("testMode", true);
        protocolContext.put("embedded", true);

        return ActionContext.builder().protocol("test").clientId("test-client").sessionId("test-session")
                .protocolContext(protocolContext).correlationId("test-correlation-" + System.currentTimeMillis())
                .build();
    }

    /**
     * Setup test environment - override in subclasses for specific setup.
     */
    protected void setupTestEnvironment() {
        // Default implementation - can be overridden
    }

    /**
     * Setup test data - creates common test items and things.
     */
    protected void setupTestData() {
        try {
            // Create test items
            createTestItems();

            // Create test things
            createTestThings();

            // Wait for data to be registered
            Thread.sleep(1000);
        } catch (Exception e) {
            System.err.println("Failed to setup test data: " + e.getMessage());
        }
    }

    /**
     * Create common test items for integration testing.
     */
    protected void createTestItems() {
        try {
            // Basic items
            SwitchItem testSwitch = new SwitchItem("TestSwitch");
            NumberItem testNumber = new NumberItem("TestNumber");
            StringItem testString = new StringItem("TestString");
            ContactItem testContact = new ContactItem("TestContact");
            DimmerItem testDimmer = new DimmerItem("TestDimmer");
            ColorItem testColor = new ColorItem("TestColor");
            RollershutterItem testRoller = new RollershutterItem("TestRoller");
            DateTimeItem testDateTime = new DateTimeItem("TestDateTime");
            LocationItem testLocation = new LocationItem("TestLocation");
            PlayerItem testPlayer = new PlayerItem("TestPlayer");
            ImageItem testImage = new ImageItem("TestImage");
            CallItem testCall = new CallItem("TestCall");

            // Mock item registry behavior
            when(itemRegistry.get("TestSwitch")).thenReturn(testSwitch);
            when(itemRegistry.get("TestNumber")).thenReturn(testNumber);
            when(itemRegistry.get("TestString")).thenReturn(testString);
            when(itemRegistry.get("TestContact")).thenReturn(testContact);
            when(itemRegistry.get("TestDimmer")).thenReturn(testDimmer);
            when(itemRegistry.get("TestColor")).thenReturn(testColor);
            when(itemRegistry.get("TestRoller")).thenReturn(testRoller);
            when(itemRegistry.get("TestDateTime")).thenReturn(testDateTime);
            when(itemRegistry.get("TestLocation")).thenReturn(testLocation);
            when(itemRegistry.get("TestPlayer")).thenReturn(testPlayer);
            when(itemRegistry.get("TestImage")).thenReturn(testImage);
            when(itemRegistry.get("TestCall")).thenReturn(testCall);

            // Set some initial states - using proper State objects
            testSwitch.setState(org.openhab.core.library.types.OnOffType.OFF);
            testNumber.setState(new org.openhab.core.library.types.DecimalType(42));
            testString.setState(new org.openhab.core.library.types.StringType("Hello World"));
            testContact.setState(org.openhab.core.library.types.OpenClosedType.OPEN);
            testDimmer.setState(new org.openhab.core.library.types.PercentType(50));
            testColor.setState(new org.openhab.core.library.types.HSBType("255,255,255"));
            testRoller.setState(new org.openhab.core.library.types.PercentType(50));
            testDateTime.setState(new org.openhab.core.library.types.DateTimeType("2024-01-01T12:00:00"));
            testLocation.setState(new org.openhab.core.library.types.PointType("40.7128,-74.0060"));
            testPlayer.setState(org.openhab.core.library.types.PlayPauseType.PLAY);
            testImage.setState(new org.openhab.core.library.types.RawType(
                    "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=="
                            .getBytes(),
                    "image/png"));
            testCall.setState(new org.openhab.core.library.types.StringType("INCOMING"));

        } catch (Exception e) {
            System.err.println("Failed to create test items: " + e.getMessage());
        }
    }

    /**
     * Create common test things for integration testing.
     */
    protected void createTestThings() {
        // This would require more complex setup with thing handlers
        // For now, we'll rely on the item registry for basic testing
        // Thing creation can be implemented in specific test classes
    }

    /**
     * Execute an Action with test parameters.
     */
    protected ActionResult executeAction(Action action, Map<String, Object> parameters) throws ActionException {
        return action.execute(parameters, actionContext);
    }

    /**
     * Execute an Action with default test parameters.
     */
    protected ActionResult executeAction(Action action) throws ActionException {
        return executeAction(action, testParameters);
    }

    /**
     * Execute an Action asynchronously.
     */
    protected ActionResult executeActionAsync(Action action, Map<String, Object> parameters) throws Exception {
        return action.executeAsync(parameters, actionContext).get(10, TimeUnit.SECONDS);
    }

    /**
     * Assert that an action result indicates success.
     */
    protected void assertSuccess(ActionResult result) {
        assertNotNull(result, "ActionResult should not be null");
        assertTrue(result.isSuccess(), "ActionResult should indicate success");
        assertNotNull(result.getData(), "ActionResult data should not be null");
    }

    /**
     * Assert that an action result indicates failure.
     */
    protected void assertFailure(ActionResult result) {
        assertNotNull(result, "ActionResult should not be null");
        assertFalse(result.isSuccess(), "ActionResult should indicate failure");
        assertNotNull(result.getMessage(), "Failure should have an error message");
    }

    /**
     * Assert that an action result contains expected data.
     */
    protected void assertResultContains(ActionResult result, String key, Object expectedValue) {
        assertSuccess(result);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertTrue(data.containsKey(key), "Result should contain key: " + key);
        assertEquals(expectedValue, data.get(key), "Value for key '" + key + "' should match");
    }

    /**
     * Assert that an action result contains a key.
     */
    protected void assertResultContainsKey(ActionResult result, String key) {
        assertSuccess(result);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertTrue(data.containsKey(key), "Result should contain key: " + key);
    }

    /**
     * Assert that an action result contains a list with expected size.
     */
    protected void assertResultListSize(ActionResult result, String key, int expectedSize) {
        assertSuccess(result);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertTrue(data.containsKey(key), "Result should contain key: " + key);

        @SuppressWarnings("unchecked")
        java.util.List<?> list = (java.util.List<?>) data.get(key);
        assertNotNull(list, "Value for key '" + key + "' should be a list");
        assertEquals(expectedSize, list.size(), "List size should match");
    }

    /**
     * Create test parameters with common values.
     */
    protected Map<String, Object> createTestParameters(String... keyValuePairs) {
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            if (i + 1 < keyValuePairs.length) {
                params.put(keyValuePairs[i], keyValuePairs[i + 1]);
            }
        }
        return params;
    }

    /**
     * Get a test item by name.
     */
    protected Item getTestItem(String itemName) {
        try {
            return itemRegistry.get(itemName);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Assert that a test item exists.
     */
    protected void assertTestItemExists(String itemName) {
        Item item = getTestItem(itemName);
        assertNotNull(item, "Test item should exist: " + itemName);
    }

    /**
     * Assert that a test item has the expected state.
     */
    protected void assertTestItemState(String itemName, String expectedState) {
        Item item = getTestItem(itemName);
        assertNotNull(item, "Test item should exist: " + itemName);
        assertEquals(expectedState, item.getState().toString(), "Item state should match");
    }

    /**
     * Clean up test resources.
     */
    protected void cleanup() {
        if (testDataDir != null && Files.exists(testDataDir)) {
            try {
                Files.walk(testDataDir).sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                // Log but don't fail test
                                System.err.println("Failed to delete test file: " + path);
                            }
                        });
            } catch (IOException e) {
                // Log but don't fail test
                System.err.println("Failed to cleanup test directory: " + testDataDir);
            }
        }
    }
}
