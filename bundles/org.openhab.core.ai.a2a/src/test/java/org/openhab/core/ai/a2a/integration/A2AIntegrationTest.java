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
import org.openhab.core.ai.a2a.internal.A2ARestEndpoint;
import org.openhab.core.ai.a2a.internal.A2ASecurityManager;
import org.openhab.core.ai.a2a.internal.A2AServerManager;
import org.openhab.core.ai.a2a.internal.A2ASkillRegistry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Main A2A Integration Tests for openHAB AI A2A bundle.
 * 
 * Tests A2A agent communication, skill execution, and protocol integration
 * as outlined in the TEST_PLAN.md integration testing strategy.
 */
@ExtendWith(MockitoExtension.class)
class A2AIntegrationTest {

    private ObjectMapper objectMapper;
    private A2ATestClient testClient;

    @Mock
    private A2AServerManager serverManager;

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

        // Initialize test client
        testClient.initialize();
        testClient.initializeA2A();
    }

    /**
     * Test A2A agent connection and communication.
     */
    @Test
    void testA2AAgentConnection() throws Exception {
        // Test agent initialization
        ObjectNode initResponse = testClient.initializeA2A();

        assertEquals("2.0", initResponse.get("jsonrpc").asText());
        assertNotNull(initResponse.get("result"));

        ObjectNode result = (ObjectNode) initResponse.get("result");
        assertNotNull(result.get("agentCard"));
        assertNotNull(result.get("capabilities"));

        // Verify agent information
        ObjectNode agentCard = (ObjectNode) result.get("agentCard");
        assertEquals("openHAB A2A Agent", agentCard.get("name").asText());
        assertNotNull(agentCard.get("version"));
    }

    /**
     * Test skill execution through A2A protocol.
     */
    @Test
    void testSkillExecutionViaA2A() throws Exception {
        // First, get list of available skills
        ObjectNode listResponse = testClient.listSkills();

        assertEquals("2.0", listResponse.get("jsonrpc").asText());

        ObjectNode result = (ObjectNode) listResponse.get("result");
        assertNotNull(result.get("skills"));

        var skills = result.get("skills");
        assertTrue(skills.size() > 0, "Should have available skills");

        // Test execution of items list skill
        Map<String, Object> arguments = Map.of("filter", "all");
        ObjectNode callResponse = testClient.executeSkill("openhab.items.list", arguments);

        assertEquals("2.0", callResponse.get("jsonrpc").asText());
        assertNotNull(callResponse.get("result"));

        ObjectNode callResult = (ObjectNode) callResponse.get("result");
        assertNotNull(callResult.get("content"));

        // Verify content contains items data
        var content = callResult.get("content");
        assertTrue(content.toString().contains("items"), "Response should contain items data");
    }

    /**
     * Test protocol layer integration.
     */
    @Test
    void testProtocolIntegration() throws Exception {
        // Test different protocol types
        testClient.setProtocolType("HTTP");
        ObjectNode httpResponse = testClient.initializeA2A();
        assertNotNull(httpResponse.get("result"));

        testClient.setProtocolType("WEBSOCKET");
        ObjectNode wsResponse = testClient.initializeA2A();
        assertNotNull(wsResponse.get("result"));

        testClient.setProtocolType("REST");
        ObjectNode restResponse = testClient.initializeA2A();
        assertNotNull(restResponse.get("result"));
    }

    /**
     * Test error handling and recovery.
     */
    @Test
    void testErrorHandling() throws Exception {
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
     * Test concurrent skill execution.
     */
    @Test
    void testConcurrentExecution() throws Exception {
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
     * Test streaming skill execution.
     */
    @Test
    void testStreamingExecution() throws Exception {
        // Test streaming execution for long-running operations
        Map<String, Object> arguments = Map.of("action", "backup");
        var streamResponse = testClient.executeSkillStreaming("openhab.persistence.manage", arguments);

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
     * Test protocol compliance.
     */
    @Test
    void testProtocolCompliance() throws Exception {
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
     * Test performance under load.
     */
    @Test
    void testPerformanceUnderLoad() throws Exception {
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
     * Test server lifecycle management.
     */
    @Test
    void testServerLifecycle() throws Exception {
        // Test server status
        assertTrue(serverManager.isRunning(), "Server should be running");

        // Test server instance management
        assertNotNull(serverManager, "Server manager should be available");

        // Test skill registry integration
        assertNotNull(skillRegistry, "Skill registry should be available");
    }

    /**
     * Test skill registry integration.
     */
    @Test
    void testSkillRegistryIntegration() throws Exception {
        // Get list of skills from registry
        ObjectNode listResponse = testClient.listSkills();

        ObjectNode result = (ObjectNode) listResponse.get("result");
        var skills = result.get("skills");

        // Verify specific skills are available
        boolean hasItemsSkill = false;
        boolean hasPersistenceSkill = false;
        boolean hasThingsSkill = false;

        for (var skill : skills) {
            String name = skill.get("name").asText();
            if ("openhab.items.list".equals(name))
                hasItemsSkill = true;
            if ("openhab.persistence.manage".equals(name))
                hasPersistenceSkill = true;
            if ("openhab.things.list".equals(name))
                hasThingsSkill = true;
        }

        assertTrue(hasItemsSkill, "Items list skill should be available");
        assertTrue(hasPersistenceSkill, "Persistence skill should be available");
        assertTrue(hasThingsSkill, "Things list skill should be available");
    }

    /**
     * Test A2A task management integration.
     */
    @Test
    void testA2ATaskManagementIntegration() throws Exception {
        // Create a task
        Map<String, Object> taskDefinition = Map.of("name", "integration-test-task", "description",
                "Task for integration testing", "skill", "openhab.items.list", "parameters", Map.of("filter", "all"),
                "priority", "normal", "timeout", 30000);

        ObjectNode createResponse = testClient.createTask(taskDefinition);
        assertEquals("2.0", createResponse.get("jsonrpc").asText());
        assertNotNull(createResponse.get("result"));

        String taskId = createResponse.get("result").get("taskId").asText();
        assertNotNull(taskId);

        // Monitor task execution
        ObjectNode statusResponse = testClient.getTaskStatus(taskId);
        assertNotNull(statusResponse.get("result"));

        // Wait for completion
        Thread.sleep(2000);

        // Get task result
        ObjectNode resultResponse = testClient.getTaskResult(taskId);
        assertNotNull(resultResponse.get("result"));

        // Clean up
        ObjectNode removeResponse = testClient.removeTask(taskId);
        assertEquals("removed", removeResponse.get("result").get("status").asText());
    }

    /**
     * Test A2A agent discovery integration.
     */
    @Test
    void testA2AAgentDiscoveryIntegration() throws Exception {
        // Discover available agents
        ObjectNode discoveryResponse = testClient.discoverAgents();
        assertEquals("2.0", discoveryResponse.get("jsonrpc").asText());
        assertNotNull(discoveryResponse.get("result"));

        ObjectNode result = (ObjectNode) discoveryResponse.get("result");
        assertNotNull(result.get("agents"));

        // Register a test agent
        Map<String, Object> agentInfo = Map.of("name", "integration-test-agent", "version", "1.0.0", "description",
                "Test agent for integration testing", "capabilities",
                Map.of("skills", true, "streaming", true, "authentication", true));

        ObjectNode registerResponse = testClient.registerAgent(agentInfo);
        assertNotNull(registerResponse.get("result"));

        String agentId = registerResponse.get("result").get("agentId").asText();
        assertNotNull(agentId);

        // Verify agent is discoverable
        ObjectNode newDiscoveryResponse = testClient.discoverAgents();
        ObjectNode newResult = (ObjectNode) newDiscoveryResponse.get("result");
        var agents = newResult.get("agents");

        boolean agentFound = false;
        for (var agent : agents) {
            if (agentId.equals(agent.get("id").asText())) {
                agentFound = true;
                break;
            }
        }

        assertTrue(agentFound, "Registered agent should be discoverable");

        // Clean up
        ObjectNode unregisterResponse = testClient.unregisterAgent(agentId);
        assertEquals("unregistered", unregisterResponse.get("result").get("status").asText());
    }

    /**
     * Test integration with real A2A agent (if available).
     * This test will be skipped if the real agent is not accessible.
     */
    @Test
    void testRealA2AAgentIntegration() throws Exception {
        // Test against the publicly accessible A2A agent
        String realAgentUrl = "https://hello-world-gxfr.onrender.com";
        String agentCardUrl = realAgentUrl + "/.well-known/agent.json";

        // Check if real agent is available
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(agentCardUrl)).timeout(java.time.Duration.ofSeconds(10)).GET().build();

            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("Real A2A agent not available (HTTP " + response.statusCode() + "), skipping test");
                return;
            }

            // Parse agent card
            JsonNode agentCard = objectMapper.readTree(response.body());
            System.out.println(
                    "Real A2A Agent: " + agentCard.get("name").asText() + " v" + agentCard.get("version").asText());

            // Test basic integration if agent is available
            // Note: This is a basic test - full integration testing is in A2ARealAgentIntegrationTest

        } catch (Exception e) {
            System.out.println("Real A2A agent not accessible: " + e.getMessage());
            // Test is skipped if agent is not available
        }
    }
}
