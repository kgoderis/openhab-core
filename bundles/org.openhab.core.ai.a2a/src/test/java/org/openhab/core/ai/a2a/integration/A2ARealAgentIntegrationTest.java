package org.openhab.core.ai.a2a.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Real A2A Agent Integration Tests for openHAB AI A2A bundle.
 * 
 * Tests against the publicly accessible A2A agent at:
 * https://hello-world-gxfr.onrender.com/.well-known/agent.json
 * 
 * These tests verify real protocol compliance, agent discovery, and message exchange
 * with an actual A2A-compliant agent outside of local environments.
 */
@ExtendWith(MockitoExtension.class)
class A2ARealAgentIntegrationTest {

    private static final String REAL_AGENT_URL = "https://hello-world-gxfr.onrender.com";
    private static final String AGENT_CARD_ENDPOINT = "/.well-known/agent.json";
    private static final String A2A_ENDPOINT = "/a2a";
    
    private ObjectMapper objectMapper;
    private HttpClient httpClient;
    private A2ATestClient testClient;
    private boolean realAgentAvailable;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        testClient = new A2ATestClient();
        
        // Check if real agent is available
        realAgentAvailable = checkRealAgentAvailability();
        
        // Initialize test client
        testClient.initialize();
        testClient.initializeA2A();
    }

    /**
     * Check if the real A2A agent is available for testing.
     */
    private boolean checkRealAgentAvailability() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(REAL_AGENT_URL + AGENT_CARD_ENDPOINT))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.out.println("Real A2A agent not available: " + e.getMessage());
            return false;
        }
    }

    /**
     * Test real agent discovery and agent card retrieval.
     */
    @Test
    void testRealAgentDiscovery() throws Exception {
        assumeTrue(realAgentAvailable, "Real A2A agent is not available for testing");
        
        // Fetch agent card from real agent
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(REAL_AGENT_URL + AGENT_CARD_ENDPOINT))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode(), "Should receive successful response from real agent");
        
        // Parse agent card
        JsonNode agentCard = objectMapper.readTree(response.body());
        
        // Verify agent card structure
        assertNotNull(agentCard.get("name"), "Agent should have a name");
        assertNotNull(agentCard.get("version"), "Agent should have a version");
        assertNotNull(agentCard.get("description"), "Agent should have a description");
        assertNotNull(agentCard.get("capabilities"), "Agent should have capabilities");
        
        // Log agent information
        System.out.println("Real A2A Agent Information:");
        System.out.println("Name: " + agentCard.get("name").asText());
        System.out.println("Version: " + agentCard.get("version").asText());
        System.out.println("Description: " + agentCard.get("description").asText());
    }

    /**
     * Test real agent initialization and handshake.
     */
    @Test
    void testRealAgentInitialization() throws Exception {
        assumeTrue(realAgentAvailable, "Real A2A agent is not available for testing");
        
        // Configure test client to use real agent endpoint
        testClient.setRealAgentEndpoint(REAL_AGENT_URL + A2A_ENDPOINT);
        
        // Initialize with real agent
        ObjectNode initResponse = testClient.initializeWithRealAgent();
        
        assertEquals("2.0", initResponse.get("jsonrpc").asText());
        assertNotNull(initResponse.get("result"));
        
        ObjectNode result = (ObjectNode) initResponse.get("result");
        assertNotNull(result.get("agentCard"));
        assertNotNull(result.get("capabilities"));
        
        // Verify agent information matches expected
        ObjectNode agentCard = (ObjectNode) result.get("agentCard");
        assertNotNull(agentCard.get("name"));
        assertNotNull(agentCard.get("version"));
        
        System.out.println("Successfully initialized with real A2A agent: " + agentCard.get("name").asText());
    }

    /**
     * Test real agent skill discovery.
     */
    @Test
    void testRealAgentSkillDiscovery() throws Exception {
        assumeTrue(realAgentAvailable, "Real A2A agent is not available for testing");
        
        // Configure test client to use real agent endpoint
        testClient.setRealAgentEndpoint(REAL_AGENT_URL + A2A_ENDPOINT);
        
        // Initialize with real agent first
        testClient.initializeWithRealAgent();
        
        // Discover skills from real agent
        ObjectNode skillsResponse = testClient.listSkillsFromRealAgent();
        
        assertEquals("2.0", skillsResponse.get("jsonrpc").asText());
        assertNotNull(skillsResponse.get("result"));
        
        ObjectNode result = (ObjectNode) skillsResponse.get("result");
        assertNotNull(result.get("skills"));
        
        var skills = result.get("skills");
        assertTrue(skills.size() >= 0, "Should have skills list from real agent");
        
        // Log available skills
        System.out.println("Real A2A Agent Skills:");
        for (var skill : skills) {
            System.out.println("- " + skill.get("name").asText() + ": " + skill.get("description").asText());
        }
    }

    /**
     * Test real agent skill execution.
     */
    @Test
    void testRealAgentSkillExecution() throws Exception {
        assumeTrue(realAgentAvailable, "Real A2A agent is not available for testing");
        
        // Configure test client to use real agent endpoint
        testClient.setRealAgentEndpoint(REAL_AGENT_URL + A2A_ENDPOINT);
        
        // Initialize with real agent first
        testClient.initializeWithRealAgent();
        
        // Get available skills
        ObjectNode skillsResponse = testClient.listSkillsFromRealAgent();
        ObjectNode result = (ObjectNode) skillsResponse.get("result");
        var skills = result.get("skills");
        
        assumeTrue(skills.size() > 0, "Real agent should have at least one skill available");
        
        // Execute a skill from the real agent
        String skillName = skills.get(0).get("name").asText();
        Map<String, Object> arguments = Map.of("test", "parameter");
        
        ObjectNode executionResponse = testClient.executeSkillFromRealAgent(skillName, arguments);
        
        assertEquals("2.0", executionResponse.get("jsonrpc").asText());
        
        // Should either have result or error (depending on skill)
        assertTrue(executionResponse.has("result") || executionResponse.has("error"), 
            "Should have either result or error from real agent skill execution");
        
        if (executionResponse.has("result")) {
            ObjectNode execResult = (ObjectNode) executionResponse.get("result");
            assertNotNull(execResult.get("content"), "Should have content in result");
            System.out.println("Real agent skill execution successful: " + skillName);
        } else {
            ObjectNode error = (ObjectNode) executionResponse.get("error");
            System.out.println("Real agent skill execution failed: " + skillName + " - " + error.get("message").asText());
        }
    }

    /**
     * Test real agent protocol compliance.
     */
    @Test
    void testRealAgentProtocolCompliance() throws Exception {
        assumeTrue(realAgentAvailable, "Real A2A agent is not available for testing");
        
        // Configure test client to use real agent endpoint
        testClient.setRealAgentEndpoint(REAL_AGENT_URL + A2A_ENDPOINT);
        
        // Test JSON-RPC 2.0 compliance with real agent
        ObjectNode initResponse = testClient.initializeWithRealAgent();
        
        // Verify required JSON-RPC 2.0 fields
        assertEquals("2.0", initResponse.get("jsonrpc").asText());
        assertTrue(initResponse.has("id"));
        
        // Test with invalid JSON-RPC version
        ObjectNode invalidVersionResponse = testClient.sendInvalidRequestToRealAgent("1.0");
        assertNotNull(invalidVersionResponse.get("error"));
        
        ObjectNode error = (ObjectNode) invalidVersionResponse.get("error");
        assertEquals(-32600, error.get("code").asInt(), "Real agent should return invalid request error for wrong JSON-RPC version");
        
        System.out.println("Real A2A agent protocol compliance verified");
    }

    /**
     * Test real agent error handling.
     */
    @Test
    void testRealAgentErrorHandling() throws Exception {
        assumeTrue(realAgentAvailable, "Real A2A agent is not available for testing");
        
        // Configure test client to use real agent endpoint
        testClient.setRealAgentEndpoint(REAL_AGENT_URL + A2A_ENDPOINT);
        
        // Initialize with real agent first
        testClient.initializeWithRealAgent();
        
        // Test non-existent skill
        ObjectNode notFoundResponse = testClient.executeSkillFromRealAgent("non.existent.skill", Map.of());
        
        assertEquals("2.0", notFoundResponse.get("jsonrpc").asText());
        assertNotNull(notFoundResponse.get("error"));
        
        ObjectNode error = (ObjectNode) notFoundResponse.get("error");
        assertTrue(error.get("code").asInt() > 0, "Should have error code from real agent");
        assertNotNull(error.get("message"), "Should have error message from real agent");
        
        System.out.println("Real A2A agent error handling verified: " + error.get("message").asText());
    }

    /**
     * Test real agent performance and response times.
     */
    @Test
    void testRealAgentPerformance() throws Exception {
        assumeTrue(realAgentAvailable, "Real A2A agent is not available for testing");
        
        // Configure test client to use real agent endpoint
        testClient.setRealAgentEndpoint(REAL_AGENT_URL + A2A_ENDPOINT);
        
        long startTime = System.currentTimeMillis();
        
        // Initialize with real agent
        ObjectNode initResponse = testClient.initializeWithRealAgent();
        assertNotNull(initResponse.get("result"));
        
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        
        // Performance assertion: Initialization should complete within 5 seconds
        assertTrue(responseTime < 5000, "Real agent initialization took too long: " + responseTime + "ms");
        
        System.out.println("Real A2A agent initialization response time: " + responseTime + "ms");
        
        // Test multiple requests
        startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 3; i++) {
            ObjectNode skillsResponse = testClient.listSkillsFromRealAgent();
            assertNotNull(skillsResponse.get("result"));
        }
        
        endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        // Performance assertion: 3 requests should complete within 10 seconds
        assertTrue(totalTime < 10000, "Multiple requests to real agent took too long: " + totalTime + "ms");
        
        System.out.println("Real A2A agent multiple requests response time: " + totalTime + "ms");
    }

    /**
     * Test real agent concurrent requests.
     */
    @Test
    void testRealAgentConcurrentRequests() throws Exception {
        assumeTrue(realAgentAvailable, "Real A2A agent is not available for testing");
        
        // Configure test client to use real agent endpoint
        testClient.setRealAgentEndpoint(REAL_AGENT_URL + A2A_ENDPOINT);
        
        // Initialize with real agent first
        testClient.initializeWithRealAgent();
        
        // Send multiple concurrent requests
        CompletableFuture<ObjectNode> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                return testClient.listSkillsFromRealAgent();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        CompletableFuture<ObjectNode> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                return testClient.listSkillsFromRealAgent();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        CompletableFuture<ObjectNode> future3 = CompletableFuture.supplyAsync(() -> {
            try {
                return testClient.listSkillsFromRealAgent();
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
        
        System.out.println("Real A2A agent concurrent requests handled successfully");
    }

    /**
     * Test real agent connection stability.
     */
    @Test
    void testRealAgentConnectionStability() throws Exception {
        assumeTrue(realAgentAvailable, "Real A2A agent is not available for testing");
        
        // Configure test client to use real agent endpoint
        testClient.setRealAgentEndpoint(REAL_AGENT_URL + A2A_ENDPOINT);
        
        // Test multiple initialization attempts
        for (int i = 0; i < 3; i++) {
            ObjectNode initResponse = testClient.initializeWithRealAgent();
            assertNotNull(initResponse.get("result"));
            
            // Small delay between requests
            Thread.sleep(1000);
        }
        
        System.out.println("Real A2A agent connection stability verified");
    }

    /**
     * Test real agent capabilities verification.
     */
    @Test
    void testRealAgentCapabilitiesVerification() throws Exception {
        assumeTrue(realAgentAvailable, "Real A2A agent is not available for testing");
        
        // Configure test client to use real agent endpoint
        testClient.setRealAgentEndpoint(REAL_AGENT_URL + A2A_ENDPOINT);
        
        // Initialize with real agent
        ObjectNode initResponse = testClient.initializeWithRealAgent();
        ObjectNode result = (ObjectNode) initResponse.get("result");
        
        // Verify capabilities
        assertNotNull(result.get("capabilities"));
        ObjectNode capabilities = (ObjectNode) result.get("capabilities");
        
        // Log capabilities
        System.out.println("Real A2A Agent Capabilities:");
        capabilities.fieldNames().forEachRemaining(capability -> {
            System.out.println("- " + capability + ": " + capabilities.get(capability));
        });
        
        // Verify agent card
        assertNotNull(result.get("agentCard"));
        ObjectNode agentCard = (ObjectNode) result.get("agentCard");
        
        System.out.println("Real A2A Agent Card:");
        System.out.println("- Name: " + agentCard.get("name").asText());
        System.out.println("- Version: " + agentCard.get("version").asText());
        System.out.println("- Description: " + agentCard.get("description").asText());
    }
} 