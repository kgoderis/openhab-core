package org.openhab.core.ai.mcp.integration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Test utilities for MCP integration tests.
 * 
 * Provides common utilities and helper methods for MCP integration testing.
 */
public class MCPTestUtils {
    
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
        try (InputStream input = MCPTestUtils.class.getClassLoader()
                .getResourceAsStream("mcp-test-config.properties")) {
            if (input != null) {
                testConfig.load(input);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load MCP test configuration", e);
        }
    }
    
    /**
     * Get test configuration value.
     */
    public static String getConfig(String key) {
        return testConfig.getProperty(key);
    }
    
    /**
     * Get test configuration value with default.
     */
    public static String getConfig(String key, String defaultValue) {
        return testConfig.getProperty(key, defaultValue);
    }
    
    /**
     * Get test configuration as integer.
     */
    public static int getConfigInt(String key, int defaultValue) {
        String value = testConfig.getProperty(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }
    
    /**
     * Get test configuration as boolean.
     */
    public static boolean getConfigBoolean(String key, boolean defaultValue) {
        String value = testConfig.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    
    /**
     * Create a mock MCP initialization request.
     */
    public static ObjectNode createInitRequest() {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", 1);
        request.put("method", "initialize");
        
        ObjectNode params = objectMapper.createObjectNode();
        params.put("protocolVersion", "2024-11-05");
        params.put("capabilities", objectMapper.createObjectNode());
        
        ObjectNode clientInfo = objectMapper.createObjectNode();
        clientInfo.put("name", "test-client");
        clientInfo.put("version", "1.0.0");
        params.set("clientInfo", clientInfo);
        
        request.set("params", params);
        return request;
    }
    
    /**
     * Create a mock MCP tool list request.
     */
    public static ObjectNode createListToolsRequest() {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", 2);
        request.put("method", "tools/list");
        request.set("params", objectMapper.createObjectNode());
        return request;
    }
    
    /**
     * Create a mock MCP tool call request.
     */
    public static ObjectNode createToolCallRequest(String toolName, Map<String, Object> arguments) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", 3);
        request.put("method", "tools/call");
        
        ObjectNode params = objectMapper.createObjectNode();
        params.put("name", toolName);
        params.set("arguments", objectMapper.valueToTree(arguments));
        
        request.set("params", params);
        return request;
    }
    
    /**
     * Create a mock MCP streaming tool call request.
     */
    public static ObjectNode createStreamingToolCallRequest(String toolName, Map<String, Object> arguments) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", 4);
        request.put("method", "tools/call");
        
        ObjectNode params = objectMapper.createObjectNode();
        params.put("name", toolName);
        params.put("stream", true);
        params.set("arguments", objectMapper.valueToTree(arguments));
        
        request.set("params", params);
        return request;
    }
    
    /**
     * Create a mock MCP error response.
     */
    public static ObjectNode createErrorResponse(int code, String message) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", 1);
        
        ObjectNode error = objectMapper.createObjectNode();
        error.put("code", code);
        error.put("message", message);
        response.set("error", error);
        
        return response;
    }
    
    /**
     * Create a mock MCP success response.
     */
    public static ObjectNode createSuccessResponse(ObjectNode result) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", 1);
        response.set("result", result);
        return response;
    }
    
    /**
     * Create a mock tool metadata object.
     */
    public static ObjectNode createToolMetadata(String name, String description) {
        ObjectNode tool = objectMapper.createObjectNode();
        tool.put("name", name);
        tool.put("description", description);
        
        ObjectNode inputSchema = objectMapper.createObjectNode();
        inputSchema.put("type", "object");
        inputSchema.set("properties", objectMapper.createObjectNode());
        tool.set("inputSchema", inputSchema);
        
        return tool;
    }
    
    /**
     * Create a mock tool result content.
     */
    public static ObjectNode createToolResultContent(String content) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("content", content);
        return result;
    }
    
    /**
     * Validate JSON-RPC 2.0 response structure.
     */
    public static void validateJsonRpcResponse(ObjectNode response) {
        assert response.has("jsonrpc") : "Response should have jsonrpc field";
        assert response.get("jsonrpc").asText().equals("2.0") : "Response should have jsonrpc version 2.0";
        assert response.has("id") : "Response should have id field";
    }
    
    /**
     * Validate MCP tool response structure.
     */
    public static void validateToolResponse(ObjectNode response) {
        validateJsonRpcResponse(response);
        assert response.has("result") || response.has("error") : "Response should have result or error";
        
        if (response.has("result")) {
            ObjectNode result = (ObjectNode) response.get("result");
            assert result.has("content") : "Tool result should have content";
        }
    }
    
    /**
     * Wait for a condition with timeout.
     */
    public static void waitForCondition(java.util.function.BooleanSupplier condition, long timeoutMs) 
            throws InterruptedException {
        long startTime = System.currentTimeMillis();
        while (!condition.getAsBoolean() && (System.currentTimeMillis() - startTime) < timeoutMs) {
            Thread.sleep(100);
        }
        if (!condition.getAsBoolean()) {
            throw new RuntimeException("Condition not met within timeout: " + timeoutMs + "ms");
        }
    }
    
    /**
     * Create test data for items.
     */
    public static Map<String, Object> createTestItemsData() {
        return Map.of(
            "items", java.util.List.of(
                Map.of("name", "TestSwitch", "type", "Switch", "state", "OFF"),
                Map.of("name", "TestDimmer", "type", "Dimmer", "state", "50"),
                Map.of("name", "TestContact", "type", "Contact", "state", "OPEN")
            )
        );
    }
    
    /**
     * Create test data for things.
     */
    public static Map<String, Object> createTestThingsData() {
        return Map.of(
            "things", java.util.List.of(
                Map.of("uid", "test:thing:1", "label", "Test Thing 1", "status", "ONLINE"),
                Map.of("uid", "test:thing:2", "label", "Test Thing 2", "status", "OFFLINE")
            )
        );
    }
    
    /**
     * Create test data for persistence.
     */
    public static Map<String, Object> createTestPersistenceData() {
        return Map.of(
            "status", "RUNNING",
            "services", java.util.List.of("rrd4j", "mapdb"),
            "backup", Map.of("lastBackup", "2024-01-01T00:00:00Z", "size", "1024KB")
        );
    }
} 