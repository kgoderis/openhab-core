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
import org.openhab.core.ai.tool.internal.MCPServerManager;
import org.openhab.core.ai.tool.internal.MCPToolRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * MCP Transport Integration Tests for openHAB AI MCP bundle.
 * 
 * Tests MCP transport layer integration across different transport types
 * (STDIO, HTTP, WebSocket) as outlined in the TEST_PLAN.md integration testing strategy.
 */
@ExtendWith(MockitoExtension.class)
class MCPTransportIntegrationTest {

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
    }

    /**
     * Test STDIO transport integration.
     */
    @Test
    void testStdioTransport() throws Exception {
        testClient.setTransportType("STDIO");
        testClient.initialize();

        ObjectNode response = testClient.initializeMCP();
        assertNotNull(response.get("result"));

        // Test tool execution via STDIO
        ObjectNode toolResponse = testClient.callTool("openhab.items.list", Map.of("filter", "all"));
        assertNotNull(toolResponse.get("result"));
    }

    /**
     * Test HTTP transport integration.
     */
    @Test
    void testHttpTransport() throws Exception {
        testClient.setTransportType("HTTP");
        testClient.initialize();

        ObjectNode response = testClient.initializeMCP();
        assertNotNull(response.get("result"));

        // Test tool execution via HTTP
        ObjectNode toolResponse = testClient.callTool("openhab.items.list", Map.of("filter", "all"));
        assertNotNull(toolResponse.get("result"));
    }

    /**
     * Test WebSocket transport integration.
     */
    @Test
    void testWebSocketTransport() throws Exception {
        testClient.setTransportType("WEBSOCKET");
        testClient.initialize();

        ObjectNode response = testClient.initializeMCP();
        assertNotNull(response.get("result"));

        // Test tool execution via WebSocket
        ObjectNode toolResponse = testClient.callTool("openhab.items.list", Map.of("filter", "all"));
        assertNotNull(toolResponse.get("result"));
    }

    /**
     * Test transport switching.
     */
    @Test
    void testTransportSwitching() throws Exception {
        // Test STDIO
        testClient.setTransportType("STDIO");
        testClient.initialize();
        ObjectNode stdioResponse = testClient.initializeMCP();
        assertNotNull(stdioResponse.get("result"));

        // Switch to HTTP
        testClient.setTransportType("HTTP");
        testClient.initialize();
        ObjectNode httpResponse = testClient.initializeMCP();
        assertNotNull(httpResponse.get("result"));

        // Switch to WebSocket
        testClient.setTransportType("WEBSOCKET");
        testClient.initialize();
        ObjectNode wsResponse = testClient.initializeMCP();
        assertNotNull(wsResponse.get("result"));
    }

    /**
     * Test transport error handling.
     */
    @Test
    void testTransportErrorHandling() throws Exception {
        // Test with invalid transport type
        assertThrows(IllegalArgumentException.class, () -> {
            testClient.setTransportType("INVALID");
            testClient.initialize();
        });

        // Test with valid transport type
        testClient.setTransportType("STDIO");
        testClient.initialize();
        ObjectNode response = testClient.initializeMCP();
        assertNotNull(response.get("result"));
    }

    /**
     * Test transport performance.
     */
    @Test
    void testTransportPerformance() throws Exception {
        testClient.setTransportType("STDIO");
        testClient.initialize();

        long startTime = System.currentTimeMillis();

        // Execute multiple requests to test transport performance
        for (int i = 0; i < 5; i++) {
            ObjectNode response = testClient.callTool("openhab.items.list", Map.of("filter", "all"));
            assertNotNull(response.get("result"));
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Performance assertion: 5 requests should complete within 5 seconds
        assertTrue(duration < 5000, "Transport performance test took too long: " + duration + "ms");
    }

    /**
     * Test transport concurrent requests.
     */
    @Test
    void testTransportConcurrentRequests() throws Exception {
        testClient.setTransportType("STDIO");
        testClient.initialize();

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

        // Wait for both responses
        ObjectNode response1 = future1.get(30, TimeUnit.SECONDS);
        ObjectNode response2 = future2.get(30, TimeUnit.SECONDS);

        assertNotNull(response1.get("result"));
        assertNotNull(response2.get("result"));
    }

    /**
     * Test transport streaming.
     */
    @Test
    void testTransportStreaming() throws Exception {
        testClient.setTransportType("STDIO");
        testClient.initialize();

        // Test streaming execution
        var streamResponse = testClient.callToolStreaming("openhab.persistence.manage", Map.of("action", "backup"));

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
     * Test transport connection stability.
     */
    @Test
    void testTransportConnectionStability() throws Exception {
        testClient.setTransportType("STDIO");
        testClient.initialize();

        // Test multiple initialization cycles
        for (int i = 0; i < 3; i++) {
            ObjectNode response = testClient.initializeMCP();
            assertNotNull(response.get("result"));

            ObjectNode toolResponse = testClient.callTool("openhab.items.list", Map.of("filter", "all"));
            assertNotNull(toolResponse.get("result"));
        }
    }

    /**
     * Test transport protocol compliance.
     */
    @Test
    void testTransportProtocolCompliance() throws Exception {
        testClient.setTransportType("STDIO");
        testClient.initialize();

        // Test JSON-RPC 2.0 compliance across transport
        ObjectNode initResponse = testClient.initializeMCP();

        // Verify required JSON-RPC 2.0 fields
        assertEquals("2.0", initResponse.get("jsonrpc").asText());
        assertTrue(initResponse.has("id"));

        // Test tool call protocol compliance
        ObjectNode toolResponse = testClient.callTool("openhab.items.list", Map.of("filter", "all"));
        assertEquals("2.0", toolResponse.get("jsonrpc").asText());
        assertTrue(toolResponse.has("id"));
    }
}
