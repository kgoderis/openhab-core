package org.openhab.core.ai.mcp.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.mcp.internal.MCPServerManager;
import org.openhab.core.ai.mcp.internal.MCPToolRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Main MCP Integration Tests for openHAB AI MCP bundle.
 * 
 * Tests MCP client connection, tool execution, and transport integration
 * as outlined in the TEST_PLAN.md integration testing strategy.
 */
@ExtendWith(MockitoExtension.class)
class MCPIntegrationTest {

    private ObjectMapper objectMapper;
    private MCPTestClient testClient;

    @Mock
    private MCPServerManager serverManager;

    @Mock
    private MCPToolRegistry toolRegistry;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        testClient = new MCPTestClient();
        
        // Mock server manager behavior
        when(serverManager.isStarted()).thenReturn(true);
        when(serverManager.getAllServerInstances()).thenReturn(java.util.Map.of());
        when(serverManager.getToolRegistry()).thenReturn(toolRegistry);
        
        // Initialize test client
        testClient.initialize();
        
        // Initialize MCP connection
        testClient.initializeMCP();
    }

    /**
     * Test MCP client connection and communication.
     */
    @Test
    void testMCPClientConnection() throws Exception {
        // Test client initialization
        ObjectNode initResponse = testClient.initializeMCP();
        
        assertEquals("2.0", initResponse.get("jsonrpc").asText());
        assertNotNull(initResponse.get("result"));
        
        ObjectNode result = (ObjectNode) initResponse.get("result");
        assertNotNull(result.get("serverInfo"));
        assertNotNull(result.get("capabilities"));
        
        // Verify server information
        ObjectNode serverInfo = (ObjectNode) result.get("serverInfo");
        assertEquals("openHAB MCP Server", serverInfo.get("name").asText());
        assertNotNull(serverInfo.get("version"));
    }

    /**
     * Test tool execution through MCP protocol.
     */
    @Test
    void testToolExecutionViaMCP() throws Exception {
        // First, get list of available tools
        ObjectNode listResponse = testClient.listTools();
        
        assertEquals("2.0", listResponse.get("jsonrpc").asText());
        
        ObjectNode result = (ObjectNode) listResponse.get("result");
        assertNotNull(result.get("tools"));
        
        var tools = result.get("tools");
        assertTrue(tools.size() > 0, "Should have available tools");
        
        // Test execution of items list tool
        Map<String, Object> arguments = Map.of("filter", "all");
        ObjectNode callResponse = testClient.callTool("openhab.items.list", arguments);
        
        assertEquals("2.0", callResponse.get("jsonrpc").asText());
        assertNotNull(callResponse.get("result"));
        
        ObjectNode callResult = (ObjectNode) callResponse.get("result");
        assertNotNull(callResult.get("content"));
        
        // Verify content contains items data
        var content = callResult.get("content");
        assertTrue(content.toString().contains("items"), "Response should contain items data");
    }

    /**
     * Test transport layer integration.
     */
    @Test
    void testTransportIntegration() throws Exception {
        // Test different transport types
        testClient.setTransportType("STDIO");
        ObjectNode stdioResponse = testClient.initializeMCP();
        assertNotNull(stdioResponse.get("result"));
        
        testClient.setTransportType("HTTP");
        ObjectNode httpResponse = testClient.initializeMCP();
        assertNotNull(httpResponse.get("result"));
        
        testClient.setTransportType("WEBSOCKET");
        ObjectNode wsResponse = testClient.initializeMCP();
        assertNotNull(wsResponse.get("result"));
    }

    /**
     * Test error handling and recovery.
     */
    @Test
    void testErrorHandling() throws Exception {
        // Test invalid tool call
        Map<String, Object> invalidArgs = Map.of("invalid", "parameter");
        ObjectNode errorResponse = testClient.callTool("openhab.items.list", invalidArgs);
        
        assertEquals("2.0", errorResponse.get("jsonrpc").asText());
        assertNotNull(errorResponse.get("error"));
        
        ObjectNode error = (ObjectNode) errorResponse.get("error");
        assertTrue(error.get("code").asInt() > 0, "Should have error code");
        assertNotNull(error.get("message"), "Should have error message");
        
        // Test non-existent tool
        ObjectNode notFoundResponse = testClient.callTool("non.existent.tool", Map.of());
        
        assertEquals("2.0", notFoundResponse.get("jsonrpc").asText());
        assertNotNull(notFoundResponse.get("error"));
        
        ObjectNode notFoundError = (ObjectNode) notFoundResponse.get("error");
        assertEquals(-32601, notFoundError.get("code").asInt(), "Should have method not found error");
    }

    /**
     * Test concurrent tool execution.
     */
    @Test
    void testConcurrentExecution() throws Exception {
        // Send multiple concurrent requests
        CompletableFuture<ObjectNode> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                return testClient.callTool("openhab.items.list", Map.of("filter", "all"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        CompletableFuture<ObjectNode> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                return testClient.callTool("openhab.persistence.manage", Map.of("action", "status"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        CompletableFuture<ObjectNode> future3 = CompletableFuture.supplyAsync(() -> {
            try {
                return testClient.callTool("openhab.things.list", Map.of("filter", "all"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        // Wait for all responses
        ObjectNode response1 = future1.get(30, TimeUnit.SECONDS);
        ObjectNode response2 = future2.get(30, TimeUnit.SECONDS);
        ObjectNode response3 = future3.get(30, TimeUnit.SECONDS);
        
        assertNotNull(response1.get("result"));
        assertNotNull(response2.get("result"));
        assertNotNull(response3.get("result"));
    }

    /**
     * Test streaming tool execution.
     */
    @Test
    void testStreamingExecution() throws Exception {
        // Test streaming execution for long-running operations
        Map<String, Object> arguments = Map.of("action", "backup");
        var streamResponse = testClient.callToolStreaming("openhab.persistence.manage", arguments);
        
        assertNotNull(streamResponse);
        assertTrue(streamResponse.size() > 0, "Should have received streaming events");
        
        boolean hasData = false;
        boolean hasDone = false;
        
        for (ObjectNode event : streamResponse) {
            if (event.has("content")) {
                hasData = true;
            }
            if (event.has("isError") && !event.get("isError").asBoolean()) {
                hasDone = true;
            }
        }
        
        assertTrue(hasData, "Should have received data events");
        assertTrue(hasDone, "Should have received completion event");
    }

    /**
     * Test authentication and authorization.
     */
    @Test
    void testAuthenticationAndAuthorization() throws Exception {
        // Test with valid credentials
        testClient.setCredentials("valid-user", "valid-token");
        ObjectNode validResponse = testClient.initializeMCP();
        assertNotNull(validResponse.get("result"));
        
        // Test with invalid credentials
        testClient.setCredentials("invalid-user", "invalid-token");
        ObjectNode invalidResponse = testClient.initializeMCP();
        
        // Should either succeed (if no auth required) or fail with auth error
        if (invalidResponse.has("error")) {
            ObjectNode error = (ObjectNode) invalidResponse.get("error");
            int errorCode = error.get("code").asInt();
            assertTrue(errorCode == -32001 || errorCode == -32603, 
                "Should have authentication or internal error code");
        }
    }

    /**
     * Test protocol compliance.
     */
    @Test
    void testProtocolCompliance() throws Exception {
        // Test JSON-RPC 2.0 compliance
        ObjectNode initResponse = testClient.initializeMCP();
        
        // Verify required JSON-RPC 2.0 fields
        assertEquals("2.0", initResponse.get("jsonrpc").asText());
        assertTrue(initResponse.has("id"));
        
        // Test with invalid JSON-RPC version
        ObjectNode invalidVersionResponse = testClient.sendInvalidRequest("1.0");
        assertNotNull(invalidVersionResponse.get("error"));
        
        ObjectNode error = (ObjectNode) invalidVersionResponse.get("error");
        assertEquals(-32600, error.get("code").asInt(), "Should have invalid request error");
    }

    /**
     * Test performance under load.
     */
    @Test
    void testPerformanceUnderLoad() throws Exception {
        long startTime = System.currentTimeMillis();
        
        // Execute multiple requests in sequence
        for (int i = 0; i < 10; i++) {
            ObjectNode response = testClient.callTool("openhab.items.list", Map.of("filter", "all"));
            assertNotNull(response.get("result"));
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Performance assertion: 10 requests should complete within 10 seconds
        assertTrue(duration < 10000, "Performance test took too long: " + duration + "ms");
        
        // Test memory usage
        Runtime runtime = Runtime.getRuntime();
        long memoryUsed = runtime.totalMemory() - runtime.freeMemory();
        
        // Memory assertion: Should not exceed 100MB
        assertTrue(memoryUsed < 100 * 1024 * 1024, "Memory usage too high: " + memoryUsed + " bytes");
    }

    /**
     * Test server lifecycle management.
     */
    @Test
    void testServerLifecycle() throws Exception {
        // Test server status
        assertTrue(serverManager.isStarted(), "Server should be started");
        
        // Test server instance management
        var instances = serverManager.getAllServerInstances();
        assertNotNull(instances);
        assertTrue(instances.size() > 0, "Should have at least one server instance");
        
        // Test tool registry integration
        var toolRegistry = serverManager.getToolRegistry();
        assertNotNull(toolRegistry, "Tool registry should be available");
    }

    /**
     * Test tool registry integration.
     */
    @Test
    void testToolRegistryIntegration() throws Exception {
        // Get list of tools from registry
        ObjectNode listResponse = testClient.listTools();
        
        ObjectNode result = (ObjectNode) listResponse.get("result");
        var tools = result.get("tools");
        
        // Verify specific tools are available
        boolean hasItemsTool = false;
        boolean hasPersistenceTool = false;
        boolean hasThingsTool = false;
        
        for (var tool : tools) {
            String name = tool.get("name").asText();
            if ("openhab.items.list".equals(name)) hasItemsTool = true;
            if ("openhab.persistence.manage".equals(name)) hasPersistenceTool = true;
            if ("openhab.things.list".equals(name)) hasThingsTool = true;
        }
        
        assertTrue(hasItemsTool, "Items list tool should be available");
        assertTrue(hasPersistenceTool, "Persistence tool should be available");
        assertTrue(hasThingsTool, "Things list tool should be available");
    }
} 