package org.openhab.core.ai.a2a.integration;

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
import org.openhab.core.ai.a2a.internal.A2AAgentExecutor;
import org.openhab.core.ai.a2a.internal.A2AProtocolHandler;
import org.openhab.core.ai.a2a.internal.A2ARestEndpoint;
import org.openhab.core.ai.a2a.internal.A2ASecurityManager;
import org.openhab.core.ai.a2a.internal.A2ASkillRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A2A Protocol Integration Tests for openHAB AI A2A bundle.
 * 
 * Tests A2A protocol layer integration across different protocol types
 * (HTTP, WebSocket, REST) as outlined in the TEST_PLAN.md integration testing strategy.
 */
@ExtendWith(MockitoExtension.class)
class A2AProtocolIntegrationTest {

    private ObjectMapper objectMapper;
    private A2ATestClient testClient;

    @Mock
    private A2AProtocolHandler protocolHandler;

    @Mock
    private A2ASkillRegistry skillRegistry;

    @Mock
    private A2ASecurityManager securityManager;

    @Mock
    private A2AAgentExecutor agentExecutor;

    @Mock
    private A2ARestEndpoint restEndpoint;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        testClient = new A2ATestClient();

        // Mock server manager behavior
        when(serverManager.isRunning()).thenReturn(true);
    }

    /**
     * Test HTTP protocol integration.
     */
    @Test
    void testHttpProtocol() throws Exception {
        testClient.setProtocolType("HTTP");
        testClient.initialize();

        ObjectNode response = testClient.initializeA2A();
        assertNotNull(response.get("result"));

        // Test skill execution via HTTP
        ObjectNode skillResponse = testClient.executeSkill("openhab.items.list", Map.of("filter", "all"));
        assertNotNull(skillResponse.get("result"));
    }

    /**
     * Test WebSocket protocol integration.
     */
    @Test
    void testWebSocketProtocol() throws Exception {
        testClient.setProtocolType("WEBSOCKET");
        testClient.initialize();

        ObjectNode response = testClient.initializeA2A();
        assertNotNull(response.get("result"));

        // Test skill execution via WebSocket
        ObjectNode skillResponse = testClient.executeSkill("openhab.items.list", Map.of("filter", "all"));
        assertNotNull(skillResponse.get("result"));
    }

    /**
     * Test REST protocol integration.
     */
    @Test
    void testRestProtocol() throws Exception {
        testClient.setProtocolType("REST");
        testClient.initialize();

        ObjectNode response = testClient.initializeA2A();
        assertNotNull(response.get("result"));

        // Test skill execution via REST
        ObjectNode skillResponse = testClient.executeSkill("openhab.items.list", Map.of("filter", "all"));
        assertNotNull(skillResponse.get("result"));
    }

    /**
     * Test protocol switching.
     */
    @Test
    void testProtocolSwitching() throws Exception {
        // Test HTTP
        testClient.setProtocolType("HTTP");
        testClient.initialize();
        ObjectNode httpResponse = testClient.initializeA2A();
        assertNotNull(httpResponse.get("result"));

        // Switch to WebSocket
        testClient.setProtocolType("WEBSOCKET");
        testClient.initialize();
        ObjectNode wsResponse = testClient.initializeA2A();
        assertNotNull(wsResponse.get("result"));

        // Switch to REST
        testClient.setProtocolType("REST");
        testClient.initialize();
        ObjectNode restResponse = testClient.initializeA2A();
        assertNotNull(restResponse.get("result"));
    }

    /**
     * Test protocol error handling.
     */
    @Test
    void testProtocolErrorHandling() throws Exception {
        // Test with invalid protocol type
        assertThrows(IllegalArgumentException.class, () -> {
            testClient.setProtocolType("INVALID");
            testClient.initialize();
        });

        // Test with valid protocol type
        testClient.setProtocolType("HTTP");
        testClient.initialize();
        ObjectNode response = testClient.initializeA2A();
        assertNotNull(response.get("result"));
    }

    /**
     * Test protocol performance.
     */
    @Test
    void testProtocolPerformance() throws Exception {
        testClient.setProtocolType("HTTP");
        testClient.initialize();

        long startTime = System.currentTimeMillis();

        // Execute multiple requests to test protocol performance
        for (int i = 0; i < 5; i++) {
            ObjectNode response = testClient.executeSkill("openhab.items.list", Map.of("filter", "all"));
            assertNotNull(response.get("result"));
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Performance assertion: 5 requests should complete within 5 seconds
        assertTrue(duration < 5000, "Protocol performance test took too long: " + duration + "ms");
    }

    /**
     * Test protocol concurrent requests.
     */
    @Test
    void testProtocolConcurrentRequests() throws Exception {
        testClient.setProtocolType("HTTP");
        testClient.initialize();

        // Send multiple concurrent requests
        CompletableFuture<ObjectNode> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                return testClient.executeSkill("openhab.items.list", Map.of("filter", "all"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<ObjectNode> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                return testClient.executeSkill("openhab.persistence.manage", Map.of("action", "status"));
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
     * Test protocol streaming.
     */
    @Test
    void testProtocolStreaming() throws Exception {
        testClient.setProtocolType("HTTP");
        testClient.initialize();

        // Test streaming execution
        var streamResponse = testClient.executeSkillStreaming("openhab.persistence.manage", Map.of("action", "backup"));

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
     * Test protocol connection stability.
     */
    @Test
    void testProtocolConnectionStability() throws Exception {
        testClient.setProtocolType("HTTP");
        testClient.initialize();

        // Test multiple initialization cycles
        for (int i = 0; i < 3; i++) {
            ObjectNode response = testClient.initializeA2A();
            assertNotNull(response.get("result"));

            ObjectNode skillResponse = testClient.executeSkill("openhab.items.list", Map.of("filter", "all"));
            assertNotNull(skillResponse.get("result"));
        }
    }

    /**
     * Test protocol compliance.
     */
    @Test
    void testProtocolCompliance() throws Exception {
        testClient.setProtocolType("HTTP");
        testClient.initialize();

        // Test JSON-RPC 2.0 compliance across protocol
        ObjectNode initResponse = testClient.initializeA2A();

        // Verify required JSON-RPC 2.0 fields
        assertEquals("2.0", initResponse.get("jsonrpc").asText());
        assertTrue(initResponse.has("id"));

        // Test skill call protocol compliance
        ObjectNode skillResponse = testClient.executeSkill("openhab.items.list", Map.of("filter", "all"));
        assertEquals("2.0", skillResponse.get("jsonrpc").asText());
        assertTrue(skillResponse.has("id"));
    }
}
