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
import org.openhab.core.ai.agent.internal.A2ARestEndpoint;
import org.openhab.core.ai.agent.internal.AgentAgentExecutor;
import org.openhab.core.ai.agent.internal.AgentProtocolHandler;
import org.openhab.core.ai.agent.internal.AgentSecurityManager;
import org.openhab.core.ai.agent.internal.AgentSkillRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A2A Client Integration Tests for openHAB AI A2A bundle.
 * 
 * Tests A2A client communication, protocol compliance, and client-specific
 * functionality as outlined in the TEST_PLAN.md integration testing strategy.
 */
@ExtendWith(MockitoExtension.class)
class A2AClientIntegrationTest {

    private ObjectMapper objectMapper;
    private A2ATestClient testClient;

    @Mock
    private AgentProtocolHandler protocolHandler;

    @Mock
    private AgentSkillRegistry skillRegistry;

    @Mock
    private AgentSecurityManager securityManager;

    @Mock
    private AgentAgentExecutor agentExecutor;

    @Mock
    private A2ARestEndpoint restEndpoint;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        testClient = new A2ATestClient();

        // Mock server manager behavior
        when(serverManager.isRunning()).thenReturn(true);

        // Initialize test client
        testClient.initialize();
        testClient.initializeA2A();
    }

    /**
     * Test A2A client initialization and handshake.
     */
    @Test
    void testClientInitialization() throws Exception {
        ObjectNode response = testClient.initializeA2A();

        assertEquals("2.0", response.get("jsonrpc").asText());
        assertNotNull(response.get("result"));

        ObjectNode result = (ObjectNode) response.get("result");
        assertNotNull(result.get("agentCard"));
        assertNotNull(result.get("capabilities"));

        // Verify agent information
        ObjectNode agentCard = (ObjectNode) result.get("agentCard");
        assertEquals("openHAB A2A Agent", agentCard.get("name").asText());
        assertNotNull(agentCard.get("version"));
    }

    /**
     * Test client authentication and authorization.
     */
    @Test
    void testClientAuthentication() throws Exception {
        // Test with valid credentials
        testClient.setCredentials("valid-user", "valid-token");
        ObjectNode validResponse = testClient.initializeA2A();
        assertNotNull(validResponse.get("result"));

        // Test with invalid credentials
        testClient.setCredentials("invalid-user", "invalid-token");
        ObjectNode invalidResponse = testClient.initializeA2A();

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
        ObjectNode initResponse = testClient.initializeA2A();

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
        // Test invalid skill call
        Map<String, Object> invalidArgs = Map.of("invalid", "parameter");
        ObjectNode errorResponse = testClient.executeSkill("openhab.items.list", invalidArgs);

        assertEquals("2.0", errorResponse.get("jsonrpc").asText());
        assertNotNull(errorResponse.get("error"));

        ObjectNode error = (ObjectNode) errorResponse.get("error");
        assertTrue(error.get("code").asInt() > 0, "Should have error code");
        assertNotNull(error.get("message"), "Should have error message");

        // Test non-existent skill
        ObjectNode notFoundResponse = testClient.executeSkill("non.existent.skill", Map.of());

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

        CompletableFuture<ObjectNode> future3 = CompletableFuture.supplyAsync(() -> {
            try {
                return testClient.executeSkill("openhab.things.list", Map.of("filter", "all"));
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
            ObjectNode response = testClient.executeSkill("openhab.items.list", Map.of("filter", "all"));
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
        ObjectNode initialResponse = testClient.initializeA2A();
        assertNotNull(initialResponse.get("result"));

        // Simulate connection loss and reconnection
        testClient.close();
        testClient.initialize();

        ObjectNode reconnectionResponse = testClient.initializeA2A();
        assertNotNull(reconnectionResponse.get("result"));
    }

    /**
     * Test client session management.
     */
    @Test
    void testClientSessionManagement() throws Exception {
        // Test session creation
        ObjectNode initResponse = testClient.initializeA2A();
        assertNotNull(initResponse.get("result"));

        // Test session persistence across requests
        ObjectNode listResponse = testClient.listSkills();
        assertNotNull(listResponse.get("result"));

        ObjectNode callResponse = testClient.executeSkill("openhab.items.list", Map.of("filter", "all"));
        assertNotNull(callResponse.get("result"));
    }
}
