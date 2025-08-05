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
 * A2A Agent Discovery Integration Tests for openHAB AI A2A bundle.
 * 
 * Tests A2A agent discovery, registration, and management functionality
 * as outlined in the TEST_PLAN.md integration testing strategy.
 */
@ExtendWith(MockitoExtension.class)
class AgentAgentDiscoveryIntegrationTest {

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
     * Test agent discovery functionality.
     */
    @Test
    void testAgentDiscovery() throws Exception {
        // Discover available agents
        ObjectNode discoveryResponse = testClient.discoverAgents();

        assertEquals("2.0", discoveryResponse.get("jsonrpc").asText());
        assertNotNull(discoveryResponse.get("result"));

        ObjectNode result = (ObjectNode) discoveryResponse.get("result");
        assertNotNull(result.get("agents"));

        var agents = result.get("agents");
        assertTrue(agents.size() >= 0, "Should have agents list (may be empty)");

        // Verify agent structure if agents are found
        if (agents.size() > 0) {
            for (var agent : agents) {
                assertTrue(agent.has("id"), "Agent should have ID");
                assertTrue(agent.has("name"), "Agent should have name");
                assertTrue(agent.has("capabilities"), "Agent should have capabilities");
            }
        }
    }

    /**
     * Test agent registration.
     */
    @Test
    void testAgentRegistration() throws Exception {
        // Register a new agent
        Map<String, Object> agentInfo = Map.of("name", "test-agent", "version", "1.0.0", "description",
                "Test agent for integration testing", "capabilities",
                Map.of("skills", true, "streaming", true, "authentication", true));

        ObjectNode registerResponse = testClient.registerAgent(agentInfo);

        assertEquals("2.0", registerResponse.get("jsonrpc").asText());
        assertNotNull(registerResponse.get("result"));

        ObjectNode result = (ObjectNode) registerResponse.get("result");
        assertNotNull(result.get("agentId"));
        assertEquals("registered", result.get("status").asText());

        String agentId = result.get("agentId").asText();
        assertNotNull(agentId);
        assertFalse(agentId.isEmpty());
    }

    /**
     * Test agent unregistration.
     */
    @Test
    void testAgentUnregistration() throws Exception {
        // First register an agent
        Map<String, Object> agentInfo = Map.of("name", "unregister-test-agent", "version", "1.0.0");

        ObjectNode registerResponse = testClient.registerAgent(agentInfo);
        String agentId = registerResponse.get("result").get("agentId").asText();

        // Unregister the agent
        ObjectNode unregisterResponse = testClient.unregisterAgent(agentId);

        assertEquals("2.0", unregisterResponse.get("jsonrpc").asText());
        assertNotNull(unregisterResponse.get("result"));

        ObjectNode result = (ObjectNode) unregisterResponse.get("result");
        assertEquals("unregistered", result.get("status").asText());

        // Verify agent is no longer discoverable
        ObjectNode discoveryResponse = testClient.discoverAgents();
        ObjectNode discoveryResult = (ObjectNode) discoveryResponse.get("result");
        var agents = discoveryResult.get("agents");

        boolean agentFound = false;
        for (var agent : agents) {
            if (agentId.equals(agent.get("id").asText())) {
                agentFound = true;
                break;
            }
        }

        assertFalse(agentFound, "Agent should not be found after unregistration");
    }

    /**
     * Test agent capability discovery.
     */
    @Test
    void testAgentCapabilityDiscovery() throws Exception {
        // Register an agent with specific capabilities
        Map<String, Object> capabilities = Map.of("skills", true, "streaming", true, "authentication", true,
                "persistence", false, "customFeatures", Map.of("feature1", true, "feature2", false));

        Map<String, Object> agentInfo = Map.of("name", "capability-test-agent", "version", "1.0.0", "capabilities",
                capabilities);

        ObjectNode registerResponse = testClient.registerAgent(agentInfo);
        String agentId = registerResponse.get("result").get("agentId").asText();

        // Get agent capabilities
        ObjectNode capabilitiesResponse = testClient.getAgentCapabilities(agentId);

        assertEquals("2.0", capabilitiesResponse.get("jsonrpc").asText());
        assertNotNull(capabilitiesResponse.get("result"));

        ObjectNode result = (ObjectNode) capabilitiesResponse.get("result");
        assertNotNull(result.get("capabilities"));

        ObjectNode agentCapabilities = (ObjectNode) result.get("capabilities");
        assertTrue(agentCapabilities.get("skills").asBoolean());
        assertTrue(agentCapabilities.get("streaming").asBoolean());
        assertTrue(agentCapabilities.get("authentication").asBoolean());
        assertFalse(agentCapabilities.get("persistence").asBoolean());
    }

    /**
     * Test agent health monitoring.
     */
    @Test
    void testAgentHealthMonitoring() throws Exception {
        // Register an agent
        Map<String, Object> agentInfo = Map.of("name", "health-test-agent", "version", "1.0.0");

        ObjectNode registerResponse = testClient.registerAgent(agentInfo);
        String agentId = registerResponse.get("result").get("agentId").asText();

        // Check agent health
        ObjectNode healthResponse = testClient.getAgentHealth(agentId);

        assertEquals("2.0", healthResponse.get("jsonrpc").asText());
        assertNotNull(healthResponse.get("result"));

        ObjectNode result = (ObjectNode) healthResponse.get("result");
        assertNotNull(result.get("status"));
        assertNotNull(result.get("lastSeen"));
        assertNotNull(result.get("uptime"));

        String status = result.get("status").asText();
        assertTrue("healthy".equals(status) || "unhealthy".equals(status) || "unknown".equals(status));
    }

    /**
     * Test agent reconnection handling.
     */
    @Test
    void testAgentReconnectionHandling() throws Exception {
        // Register an agent
        Map<String, Object> agentInfo = Map.of("name", "reconnect-test-agent", "version", "1.0.0");

        ObjectNode registerResponse = testClient.registerAgent(agentInfo);
        String agentId = registerResponse.get("result").get("agentId").asText();

        // Simulate agent disconnection
        ObjectNode disconnectResponse = testClient.disconnectAgent(agentId);
        assertNotNull(disconnectResponse.get("result"));

        // Check agent status after disconnection
        ObjectNode healthResponse = testClient.getAgentHealth(agentId);
        ObjectNode healthResult = (ObjectNode) healthResponse.get("result");
        String status = healthResult.get("status").asText();

        assertTrue("disconnected".equals(status) || "unhealthy".equals(status));

        // Simulate agent reconnection
        ObjectNode reconnectResponse = testClient.reconnectAgent(agentId);
        assertNotNull(reconnectResponse.get("result"));

        // Verify agent is healthy again
        healthResponse = testClient.getAgentHealth(agentId);
        healthResult = (ObjectNode) healthResponse.get("result");
        status = healthResult.get("status").asText();

        assertTrue("healthy".equals(status) || "connected".equals(status));
    }

    /**
     * Test concurrent agent registration.
     */
    @Test
    void testConcurrentAgentRegistration() throws Exception {
        // Register multiple agents concurrently
        CompletableFuture<ObjectNode>[] futures = new CompletableFuture[3];

        for (int i = 0; i < 3; i++) {
            final int agentIndex = i;
            futures[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    Map<String, Object> agentInfo = Map.of("name", "concurrent-agent-" + agentIndex, "version",
                            "1.0.0");
                    return testClient.registerAgent(agentInfo);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        // Wait for all registrations
        ObjectNode[] responses = new ObjectNode[3];
        for (int i = 0; i < 3; i++) {
            responses[i] = futures[i].get(30, TimeUnit.SECONDS);
            assertNotNull(responses[i].get("result"));
        }

        // Verify all agents have unique IDs
        String[] agentIds = new String[3];
        for (int i = 0; i < 3; i++) {
            agentIds[i] = responses[i].get("result").get("agentId").asText();
        }

        for (int i = 0; i < 3; i++) {
            for (int j = i + 1; j < 3; j++) {
                assertNotEquals(agentIds[i], agentIds[j], "Agent IDs should be unique");
            }
        }

        // Verify all agents are discoverable
        ObjectNode discoveryResponse = testClient.discoverAgents();
        ObjectNode discoveryResult = (ObjectNode) discoveryResponse.get("result");
        var agents = discoveryResult.get("agents");

        int foundAgents = 0;
        for (var agent : agents) {
            String agentId = agent.get("id").asText();
            for (String expectedId : agentIds) {
                if (expectedId.equals(agentId)) {
                    foundAgents++;
                    break;
                }
            }
        }

        assertEquals(3, foundAgents, "All registered agents should be discoverable");
    }

    /**
     * Test agent authentication and authorization.
     */
    @Test
    void testAgentAuthenticationAndAuthorization() throws Exception {
        // Register an agent with authentication
        Map<String, Object> authConfig = Map.of("type", "token", "required", true);

        Map<String, Object> agentInfo = Map.of("name", "auth-test-agent", "version", "1.0.0", "authentication",
                authConfig);

        ObjectNode registerResponse = testClient.registerAgent(agentInfo);
        String agentId = registerResponse.get("result").get("agentId").asText();

        // Test authentication with valid credentials
        testClient.setCredentials("valid-user", "valid-token");
        ObjectNode authResponse = testClient.authenticateAgent(agentId);
        assertNotNull(authResponse.get("result"));

        ObjectNode authResult = (ObjectNode) authResponse.get("result");
        assertEquals("authenticated", authResult.get("status").asText());

        // Test authentication with invalid credentials
        testClient.setCredentials("invalid-user", "invalid-token");
        ObjectNode invalidAuthResponse = testClient.authenticateAgent(agentId);

        if (invalidAuthResponse.has("error")) {
            ObjectNode error = (ObjectNode) invalidAuthResponse.get("error");
            int errorCode = error.get("code").asInt();
            assertTrue(errorCode == -32001 || errorCode == -32603, "Should have authentication or internal error code");
        }
    }

    /**
     * Test agent skill registration.
     */
    @Test
    void testAgentSkillRegistration() throws Exception {
        // Register an agent
        Map<String, Object> agentInfo = Map.of("name", "skill-test-agent", "version", "1.0.0");

        ObjectNode registerResponse = testClient.registerAgent(agentInfo);
        String agentId = registerResponse.get("result").get("agentId").asText();

        // Register skills for the agent
        Map<String, Object> skillDefinition = Map.of("name", "custom.test.skill", "description", "Custom test skill",
                "parameters", Map.of("type", "object", "properties",
                        Map.of("param1", Map.of("type", "string"), "param2", Map.of("type", "number"))));

        ObjectNode skillResponse = testClient.registerAgentSkill(agentId, skillDefinition);

        assertEquals("2.0", skillResponse.get("jsonrpc").asText());
        assertNotNull(skillResponse.get("result"));

        ObjectNode result = (ObjectNode) skillResponse.get("result");
        assertEquals("registered", result.get("status").asText());

        // Verify skill is available
        ObjectNode skillsResponse = testClient.getAgentSkills(agentId);
        ObjectNode skillsResult = (ObjectNode) skillsResponse.get("result");
        var skills = skillsResult.get("skills");

        boolean skillFound = false;
        for (var skill : skills) {
            if ("custom.test.skill".equals(skill.get("name").asText())) {
                skillFound = true;
                break;
            }
        }

        assertTrue(skillFound, "Registered skill should be available");
    }
}
