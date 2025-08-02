package org.openhab.core.ai.a2a.integration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Test utilities for A2A integration tests.
 * 
 * Provides common utilities and helper methods for A2A integration testing.
 */
public class A2ATestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static Properties testConfig;

    static {
        loadTestConfiguration();
    }

    /**
     * Load test configuration from properties file.
     */
    private static void loadTestConfiguration() {
        testConfig = new Properties();
        try (InputStream input = A2ATestUtils.class.getClassLoader()
                .getResourceAsStream("a2a-test-config.properties")) {
            if (input != null) {
                testConfig.load(input);
            }
        } catch (IOException e) {
            // Use default configuration if file not found
            System.err.println("Warning: Could not load a2a-test-config.properties, using defaults");
        }
    }

    /**
     * Get test configuration property.
     */
    public static String getConfigProperty(String key) {
        return testConfig.getProperty(key);
    }

    /**
     * Get test configuration property with default value.
     */
    public static String getConfigProperty(String key, String defaultValue) {
        return testConfig.getProperty(key, defaultValue);
    }

    /**
     * Get test configuration as integer.
     */
    public static int getConfigPropertyAsInt(String key, int defaultValue) {
        String value = testConfig.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Get test configuration as boolean.
     */
    public static boolean getConfigPropertyAsBoolean(String key, boolean defaultValue) {
        String value = testConfig.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    /**
     * Create a mock A2A agent card.
     */
    public static ObjectNode createMockAgentCard() {
        ObjectNode agentCard = objectMapper.createObjectNode();
        agentCard.put("name", "openHAB A2A Agent");
        agentCard.put("version", "1.0.0");
        agentCard.put("description", "openHAB Agent2Agent Protocol Agent");

        ObjectNode capabilities = objectMapper.createObjectNode();
        capabilities.put("protocolVersion", "2024-11-05");
        capabilities.put("supportsStreaming", true);
        capabilities.put("supportsAuthentication", true);
        agentCard.set("capabilities", capabilities);

        return agentCard;
    }

    /**
     * Create a mock skill definition.
     */
    public static ObjectNode createMockSkill(String name, String description) {
        ObjectNode skill = objectMapper.createObjectNode();
        skill.put("name", name);
        skill.put("description", description);

        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put("type", "object");
        parameters.set("properties", objectMapper.createObjectNode());
        skill.set("parameters", parameters);

        return skill;
    }

    /**
     * Create a mock skill result.
     */
    public static ObjectNode createMockSkillResult(String content) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("content", content);
        result.put("timestamp", System.currentTimeMillis());
        result.put("success", true);
        return result;
    }

    /**
     * Create a mock error response.
     */
    public static ObjectNode createMockErrorResponse(int code, String message) {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("code", code);
        error.put("message", message);
        return error;
    }

    /**
     * Create a mock JSON-RPC request.
     */
    public static ObjectNode createMockRequest(String method, ObjectNode params, int id) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", id);
        request.put("method", method);
        if (params != null) {
            request.set("params", params);
        }
        return request;
    }

    /**
     * Create a mock JSON-RPC response.
     */
    public static ObjectNode createMockResponse(ObjectNode result, int id) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.set("result", result);
        return response;
    }

    /**
     * Create a mock streaming event.
     */
    public static ObjectNode createMockStreamingEvent(String content, boolean isError) {
        ObjectNode event = objectMapper.createObjectNode();
        if (content != null) {
            event.put("content", content);
        }
        event.put("isError", isError);
        event.put("timestamp", System.currentTimeMillis());
        return event;
    }

    /**
     * Validate JSON-RPC 2.0 compliance.
     */
    public static boolean validateJsonRpcCompliance(ObjectNode response) {
        if (!response.has("jsonrpc") || !"2.0".equals(response.get("jsonrpc").asText())) {
            return false;
        }

        if (!response.has("id")) {
            return false;
        }

        return response.has("result") || response.has("error");
    }

    /**
     * Validate skill result structure.
     */
    public static boolean validateSkillResult(ObjectNode result) {
        if (result == null) {
            return false;
        }

        if (!result.has("content")) {
            return false;
        }

        if (!result.has("timestamp")) {
            return false;
        }

        if (!result.has("success")) {
            return false;
        }

        return true;
    }

    /**
     * Validate skill definition structure.
     */
    public static boolean validateSkillDefinition(ObjectNode skill) {
        if (skill == null) {
            return false;
        }

        if (!skill.has("name")) {
            return false;
        }

        if (!skill.has("description")) {
            return false;
        }

        if (!skill.has("parameters")) {
            return false;
        }

        return true;
    }

    /**
     * Create test data for items.
     */
    public static ObjectNode createTestItemsData() {
        ObjectNode itemsData = objectMapper.createObjectNode();
        itemsData.put("totalCount", 100);
        itemsData.put("activeCount", 85);
        itemsData.put("inactiveCount", 15);

        ObjectNode items = objectMapper.createObjectNode();
        items.put("LivingRoom_Light", "Switch");
        items.put("Kitchen_Temperature", "Number");
        items.put("Bedroom_Motion", "Contact");
        itemsData.set("items", items);

        return itemsData;
    }

    /**
     * Create test data for things.
     */
    public static ObjectNode createTestThingsData() {
        ObjectNode thingsData = objectMapper.createObjectNode();
        thingsData.put("totalCount", 50);
        thingsData.put("onlineCount", 45);
        thingsData.put("offlineCount", 5);

        ObjectNode things = objectMapper.createObjectNode();
        things.put("hue:bridge:001", "Philips Hue Bridge");
        things.put("zwave:device:002", "Z-Wave Device");
        things.put("mqtt:broker:003", "MQTT Broker");
        thingsData.set("things", things);

        return thingsData;
    }

    /**
     * Create test data for persistence.
     */
    public static ObjectNode createTestPersistenceData() {
        ObjectNode persistenceData = objectMapper.createObjectNode();
        persistenceData.put("serviceCount", 3);
        persistenceData.put("activeServices", 2);

        ObjectNode services = objectMapper.createObjectNode();
        services.put("rrd4j", "active");
        services.put("influxdb", "active");
        services.put("jdbc", "inactive");
        persistenceData.set("services", services);

        return persistenceData;
    }

    /**
     * Get test timeout from configuration.
     */
    public static int getTestTimeout() {
        return getConfigPropertyAsInt("a2a.test.timeout", 30000);
    }

    /**
     * Get test retry count from configuration.
     */
    public static int getTestRetryCount() {
        return getConfigPropertyAsInt("a2a.test.retry.count", 3);
    }

    /**
     * Get max concurrent requests from configuration.
     */
    public static int getMaxConcurrentRequests() {
        return getConfigPropertyAsInt("a2a.test.concurrent.requests", 5);
    }

    /**
     * Check if mock mode is enabled.
     */
    public static boolean isMockEnabled() {
        return getConfigPropertyAsBoolean("a2a.mock.enabled", true);
    }

    /**
     * Get mock response delay.
     */
    public static int getMockResponseDelay() {
        return getConfigPropertyAsInt("a2a.mock.response.delay", 100);
    }

    /**
     * Get mock error rate.
     */
    public static double getMockErrorRate() {
        String value = testConfig.getProperty("a2a.mock.error.rate");
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return 0.1;
            }
        }
        return 0.1;
    }
}
