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
 * MCP Client Integration Tests for openHAB AI MCP bundle.
 * 
 * Tests MCP client communication, protocol compliance, and client-specific
 * functionality as outlined in the TEST_PLAN.md integration testing strategy.
 */
@ExtendWith(MockitoExtension.class)
class MCPClientIntegrationTest {

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
     * Test MCP client initialization and handshake.
     */
    @Test
    void testClientInitialization() throws Exception {
        ObjectNode response = testClient.initializeMCP();

        assertEquals("2.0", response.get("jsonrpc").asText());
        assertNotNull(response.get("result"));

        ObjectNode result = (ObjectNode) response.get("result");
        assertNotNull(result.get("serverInfo"));
        assertNotNull(result.get("capabilities"));

        // Verify server information
        ObjectNode serverInfo = (ObjectNode) result.get("serverInfo");
        assertEquals("openHAB MCP Server", serverInfo.get("name").asText());
        assertNotNull(serverInfo.get("version"));
    }

    /**
     * Test client authentication and authorization.
     */
    @Test
    void testClientAuthentication() throws Exception {
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
            assertTrue(errorCode == -32001 || errorCode == -32603, "Should have authentication or internal error code");
        }
    }

    /**
     * Test client protocol compliance.
     */
    @Test
    void testClientProtocolCompliance() throws Exception {
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
     * Test client error handling.
     */
    @Test
    void testClientErrorHandling() throws Exception {
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
     * Test client concurrent requests.
     */
    @Test
    void testClientConcurrentRequests() throws Exception {
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
     * Test client performance under load.
     */
    @Test
    void testClientPerformanceUnderLoad() throws Exception {
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
     * Test client reconnection behavior.
     */
    @Test
    void testClientReconnection() throws Exception {
        // Test initial connection
        ObjectNode initialResponse = testClient.initializeMCP();
        assertNotNull(initialResponse.get("result"));

        // Simulate connection loss and reconnection
        testClient.close();
        testClient.initialize();

        ObjectNode reconnectionResponse = testClient.initializeMCP();
        assertNotNull(reconnectionResponse.get("result"));
    }

    /**
     * Test client session management.
     */
    @Test
    void testClientSessionManagement() throws Exception {
        // Test session creation
        ObjectNode initResponse = testClient.initializeMCP();
        assertNotNull(initResponse.get("result"));

        // Test session persistence across requests
        ObjectNode listResponse = testClient.listTools();
        assertNotNull(listResponse.get("result"));

        ObjectNode callResponse = testClient.callTool("openhab.items.list", Map.of("filter", "all"));
        assertNotNull(callResponse.get("result"));
    }
}
