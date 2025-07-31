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
 * MCP Tool Integration Tests for openHAB AI MCP bundle.
 * 
 * Tests MCP tool execution, tool registry integration, and tool-specific
 * functionality as outlined in the TEST_PLAN.md integration testing strategy.
 */
@ExtendWith(MockitoExtension.class)
class MCPToolIntegrationTest {

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
        testClient.initializeMCP();
    }

    /**
     * Test tool listing and discovery.
     */
    @Test
    void testToolListing() throws Exception {
        ObjectNode listResponse = testClient.listTools();
        
        assertEquals("2.0", listResponse.get("jsonrpc").asText());
        
        ObjectNode result = (ObjectNode) listResponse.get("result");
        assertNotNull(result.get("tools"));
        
        var tools = result.get("tools");
        assertTrue(tools.size() > 0, "Should have available tools");
        
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

    /**
     * Test items list tool execution.
     */
    @Test
    void testItemsListTool() throws Exception {
        // Test items list tool with different filters
        Map<String, Object> allArgs = Map.of("filter", "all");
        ObjectNode allResponse = testClient.callTool("openhab.items.list", allArgs);
        
        assertEquals("2.0", allResponse.get("jsonrpc").asText());
        assertNotNull(allResponse.get("result"));
        
        ObjectNode result = (ObjectNode) allResponse.get("result");
        assertNotNull(result.get("content"));
        
        // Test with specific filter
        Map<String, Object> switchArgs = Map.of("filter", "Switch");
        ObjectNode switchResponse = testClient.callTool("openhab.items.list", switchArgs);
        
        assertEquals("2.0", switchResponse.get("jsonrpc").asText());
        assertNotNull(switchResponse.get("result"));
    }

    /**
     * Test persistence management tool execution.
     */
    @Test
    void testPersistenceManagementTool() throws Exception {
        // Test persistence status
        Map<String, Object> statusArgs = Map.of("action", "status");
        ObjectNode statusResponse = testClient.callTool("openhab.persistence.manage", statusArgs);
        
        assertEquals("2.0", statusResponse.get("jsonrpc").asText());
        assertNotNull(statusResponse.get("result"));
        
        // Test persistence backup
        Map<String, Object> backupArgs = Map.of("action", "backup");
        ObjectNode backupResponse = testClient.callTool("openhab.persistence.manage", backupArgs);
        
        assertEquals("2.0", backupResponse.get("jsonrpc").asText());
        assertNotNull(backupResponse.get("result"));
    }

    /**
     * Test things list tool execution.
     */
    @Test
    void testThingsListTool() throws Exception {
        // Test things list tool
        Map<String, Object> allArgs = Map.of("filter", "all");
        ObjectNode allResponse = testClient.callTool("openhab.things.list", allArgs);
        
        assertEquals("2.0", allResponse.get("jsonrpc").asText());
        assertNotNull(allResponse.get("result"));
        
        ObjectNode result = (ObjectNode) allResponse.get("result");
        assertNotNull(result.get("content"));
    }

    /**
     * Test tool parameter validation.
     */
    @Test
    void testToolParameterValidation() throws Exception {
        // Test with invalid parameters
        Map<String, Object> invalidArgs = Map.of("invalid", "parameter");
        ObjectNode errorResponse = testClient.callTool("openhab.items.list", invalidArgs);
        
        assertEquals("2.0", errorResponse.get("jsonrpc").asText());
        assertNotNull(errorResponse.get("error"));
        
        ObjectNode error = (ObjectNode) errorResponse.get("error");
        assertTrue(error.get("code").asInt() > 0, "Should have error code");
        assertNotNull(error.get("message"), "Should have error message");
        
        // Test with missing required parameters
        Map<String, Object> missingArgs = Map.of();
        ObjectNode missingResponse = testClient.callTool("openhab.items.list", missingArgs);
        
        assertEquals("2.0", missingResponse.get("jsonrpc").asText());
        // Should either succeed with defaults or fail with validation error
        assertTrue(missingResponse.has("result") || missingResponse.has("error"));
    }

    /**
     * Test tool error handling.
     */
    @Test
    void testToolErrorHandling() throws Exception {
        // Test non-existent tool
        ObjectNode notFoundResponse = testClient.callTool("non.existent.tool", Map.of());
        
        assertEquals("2.0", notFoundResponse.get("jsonrpc").asText());
        assertNotNull(notFoundResponse.get("error"));
        
        ObjectNode error = (ObjectNode) notFoundResponse.get("error");
        assertEquals(-32601, error.get("code").asInt(), "Should have method not found error");
        
        // Test tool with malformed parameters
        Map<String, Object> malformedArgs = Map.of("filter", 123); // Should be string
        ObjectNode malformedResponse = testClient.callTool("openhab.items.list", malformedArgs);
        
        assertEquals("2.0", malformedResponse.get("jsonrpc").asText());
        // Should either succeed (if type conversion is supported) or fail with validation error
        assertTrue(malformedResponse.has("result") || malformedResponse.has("error"));
    }

    /**
     * Test tool concurrent execution.
     */
    @Test
    void testToolConcurrentExecution() throws Exception {
        // Send multiple concurrent tool calls
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
     * Test tool streaming execution.
     */
    @Test
    void testToolStreamingExecution() throws Exception {
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
     * Test tool performance.
     */
    @Test
    void testToolPerformance() throws Exception {
        long startTime = System.currentTimeMillis();
        
        // Execute multiple tool calls in sequence
        for (int i = 0; i < 10; i++) {
            ObjectNode response = testClient.callTool("openhab.items.list", Map.of("filter", "all"));
            assertNotNull(response.get("result"));
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Performance assertion: 10 tool calls should complete within 10 seconds
        assertTrue(duration < 10000, "Tool performance test took too long: " + duration + "ms");
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
        
        // Verify tool registry contains expected tools
        assertTrue(tools.size() > 0, "Tool registry should contain tools");
        
        // Verify tool metadata
        for (var tool : tools) {
            assertTrue(tool.has("name"), "Tool should have name");
            assertTrue(tool.has("description"), "Tool should have description");
            assertTrue(tool.has("inputSchema"), "Tool should have input schema");
        }
    }

    /**
     * Test tool schema validation.
     */
    @Test
    void testToolSchemaValidation() throws Exception {
        // Get tool list to verify schemas
        ObjectNode listResponse = testClient.listTools();
        
        ObjectNode result = (ObjectNode) listResponse.get("result");
        var tools = result.get("tools");
        
        // Verify each tool has valid schema
        for (var tool : tools) {
            ObjectNode inputSchema = (ObjectNode) tool.get("inputSchema");
            assertNotNull(inputSchema, "Tool should have input schema");
            
            // Verify schema structure
            assertTrue(inputSchema.has("type"), "Schema should have type");
            assertTrue(inputSchema.has("properties"), "Schema should have properties");
        }
    }
} 