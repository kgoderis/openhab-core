package org.openhab.core.ai.model.tracking;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.model.ModelTrackingService;
import org.openhab.core.ai.model.api.ModelProviderType;

/**
 * Test class for LLMClientTrackingService
 * 
 * @author Karel Goderis - Initial Contribution
 */
class LLMClientTrackingServiceTest {

    private ModelTrackingService trackingService;

    @BeforeEach
    void setUp() {
        trackingService = new ModelTrackingService();
    }

    @Test
    void testRecordClientUsage() {
        // Test recording client usage
        trackingService.recordClientUsage("agent1", ModelProviderType.OPENAI, "gpt-4", "session1");
        trackingService.recordClientUsage("agent2", ModelProviderType.ANTHROPIC, "claude-3", "session2");

        // Verify client usage was recorded
        Map<String, ModelTrackingService.ClientUsageInfo> clientUsage = trackingService.getClientUsage();
        assertEquals(2, clientUsage.size());

        // Check specific client
        ModelTrackingService.ClientUsageInfo openaiUsage = trackingService.getClientUsage(ModelProviderType.OPENAI,
                "gpt-4");
        assertNotNull(openaiUsage);
        assertEquals(ModelProviderType.OPENAI, openaiUsage.getProviderType());
        assertEquals("gpt-4", openaiUsage.getModelName());
        assertEquals(1, openaiUsage.getActiveAgents().size());
        assertTrue(openaiUsage.getActiveAgents().containsKey("agent1"));
    }

    @Test
    void testRecordRequestCompletion() {
        // Setup: Record client usage first
        trackingService.recordClientUsage("agent1", ModelProviderType.OPENAI, "gpt-4", "session1");

        // Test recording request completion
        trackingService.recordRequestCompletion("agent1", ModelProviderType.OPENAI, "gpt-4", "req1", 100, 0.05, 1500,
                true);

        // Verify request was recorded
        ModelTrackingService.ClientUsageInfo usage = trackingService.getClientUsage(ModelProviderType.OPENAI, "gpt-4");
        assertNotNull(usage);
        assertEquals(1, usage.getTotalRequests());
        assertEquals(100, usage.getTotalTokens());
        assertEquals(0.05, usage.getTotalCost(), 0.001);
        assertEquals(1.0, usage.getSuccessRate(), 0.001);
        assertEquals(1500, usage.getAverageResponseTime(), 0.001);
    }

    @Test
    void testRecordClientRelease() {
        // Setup: Record client usage
        trackingService.recordClientUsage("agent1", ModelProviderType.OPENAI, "gpt-4", "session1");
        trackingService.recordClientUsage("agent2", ModelProviderType.OPENAI, "gpt-4", "session2");

        // Verify both agents are using the client
        ModelTrackingService.ClientUsageInfo usage = trackingService.getClientUsage(ModelProviderType.OPENAI, "gpt-4");
        assertEquals(2, usage.getActiveAgents().size());

        // Release one agent
        trackingService.recordClientRelease("agent1", ModelProviderType.OPENAI, "gpt-4");

        // Verify only one agent remains
        usage = trackingService.getClientUsage(ModelProviderType.OPENAI, "gpt-4");
        assertEquals(1, usage.getActiveAgents().size());
        assertFalse(usage.getActiveAgents().containsKey("agent1"));
        assertTrue(usage.getActiveAgents().containsKey("agent2"));
    }

    @Test
    void testGetActiveSessions() {
        // Setup: Record multiple sessions
        trackingService.recordClientUsage("agent1", ModelProviderType.OPENAI, "gpt-4", "session1");
        trackingService.recordClientUsage("agent2", ModelProviderType.ANTHROPIC, "claude-3", "session2");

        // Get active sessions
        List<ModelTrackingService.AgentClientSession> activeSessions = trackingService.getActiveSessions();
        assertEquals(2, activeSessions.size());

        // Verify session details
        boolean foundOpenAISession = false;
        boolean foundAnthropicSession = false;

        for (ModelTrackingService.AgentClientSession session : activeSessions) {
            if (session.getAgentId().equals("agent1") && session.getProviderType() == ModelProviderType.OPENAI) {
                foundOpenAISession = true;
                assertEquals("gpt-4", session.getModelName());
                assertEquals("session1", session.getSessionId());
                assertTrue(session.isActive());
            } else if (session.getAgentId().equals("agent2")
                    && session.getProviderType() == ModelProviderType.ANTHROPIC) {
                foundAnthropicSession = true;
                assertEquals("claude-3", session.getModelName());
                assertEquals("session2", session.getSessionId());
                assertTrue(session.isActive());
            }
        }

        assertTrue(foundOpenAISession);
        assertTrue(foundAnthropicSession);
    }

    @Test
    void testGetAgentSessions() {
        // Setup: Record sessions for one agent
        trackingService.recordClientUsage("agent1", ModelProviderType.OPENAI, "gpt-4", "session1");
        trackingService.recordClientUsage("agent1", ModelProviderType.ANTHROPIC, "claude-3", "session2");

        // Get sessions for the agent
        List<ModelTrackingService.AgentClientSession> sessions = trackingService.getAgentSessions("agent1");
        assertEquals(2, sessions.size());

        // Verify both sessions belong to agent1
        for (ModelTrackingService.AgentClientSession session : sessions) {
            assertEquals("agent1", session.getAgentId());
        }
    }

    @Test
    void testGetProviderStats() {
        // Setup: Record usage across multiple providers
        trackingService.recordClientUsage("agent1", ModelProviderType.OPENAI, "gpt-4", "session1");
        trackingService.recordClientUsage("agent2", ModelProviderType.ANTHROPIC, "claude-3", "session2");
        trackingService.recordClientUsage("agent3", ModelProviderType.OPENAI, "gpt-3.5", "session3");

        // Record some requests
        trackingService.recordRequestCompletion("agent1", ModelProviderType.OPENAI, "gpt-4", "req1", 100, 0.05, 1500,
                true);
        trackingService.recordRequestCompletion("agent2", ModelProviderType.ANTHROPIC, "claude-3", "req2", 200, 0.10,
                2000, true);
        trackingService.recordRequestCompletion("agent3", ModelProviderType.OPENAI, "gpt-3.5", "req3", 50, 0.02, 1000,
                false);

        // Get provider stats
        Map<ModelProviderType, ModelTrackingService.ProviderUsageStats> providerStats = trackingService
                .getProviderStats();

        // Verify OpenAI stats
        ModelTrackingService.ProviderUsageStats openaiStats = providerStats.get(ModelProviderType.OPENAI);
        assertNotNull(openaiStats);
        assertEquals(2, openaiStats.getModelUsage().size()); // gpt-4 and gpt-3.5
        assertEquals(2, openaiStats.getAgentUsage().size()); // agent1 and agent3
        assertEquals(2, openaiStats.getTotalRequests());
        assertEquals(150, openaiStats.getTotalTokens());
        assertEquals(0.07, openaiStats.getTotalCost(), 0.001);
        assertEquals(0.5, openaiStats.getSuccessRate(), 0.001); // 1 success, 1 failure

        // Verify Anthropic stats
        ModelTrackingService.ProviderUsageStats anthropicStats = providerStats.get(ModelProviderType.ANTHROPIC);
        assertNotNull(anthropicStats);
        assertEquals(1, anthropicStats.getTotalRequests());
        assertEquals(200, anthropicStats.getTotalTokens());
        assertEquals(0.10, anthropicStats.getTotalCost(), 0.001);
        assertEquals(1.0, anthropicStats.getSuccessRate(), 0.001);
    }

    @Test
    void testGetSystemStats() {
        // Setup: Record some usage
        trackingService.recordClientUsage("agent1", ModelProviderType.OPENAI, "gpt-4", "session1");
        trackingService.recordRequestCompletion("agent1", ModelProviderType.OPENAI, "gpt-4", "req1", 100, 0.05, 1500,
                true);
        trackingService.recordRequestCompletion("agent1", ModelProviderType.OPENAI, "gpt-4", "req2", 200, 0.10, 2500,
                false);

        // Get system stats
        ModelTrackingService.SystemUsageStats systemStats = trackingService.getSystemStats();

        assertEquals(2, systemStats.getTotalRequests());
        assertEquals(300, systemStats.getTotalTokens());
        assertEquals(0.15, systemStats.getTotalCost(), 0.001);
        assertEquals(0.5, systemStats.getErrorRate(), 0.001); // 1 success, 1 failure
        assertEquals(2000, systemStats.getAverageResponseTime(), 0.001); // (1500 + 2500) / 2
        assertNotNull(systemStats.getLastRequestTime());
    }

    @Test
    void testGetAgentClients() {
        // Setup: Record multiple clients for one agent
        trackingService.recordClientUsage("agent1", ModelProviderType.OPENAI, "gpt-4", "session1");
        trackingService.recordClientUsage("agent1", ModelProviderType.ANTHROPIC, "claude-3", "session2");

        // Get clients for the agent
        List<ModelTrackingService.ClientUsageInfo> agentClients = trackingService.getAgentClients("agent1");
        assertEquals(2, agentClients.size());

        // Verify both clients are present
        boolean foundOpenAI = false;
        boolean foundAnthropic = false;

        for (ModelTrackingService.ClientUsageInfo client : agentClients) {
            if (client.getProviderType() == ModelProviderType.OPENAI) {
                foundOpenAI = true;
                assertEquals("gpt-4", client.getModelName());
            } else if (client.getProviderType() == ModelProviderType.ANTHROPIC) {
                foundAnthropic = true;
                assertEquals("claude-3", client.getModelName());
            }
        }

        assertTrue(foundOpenAI);
        assertTrue(foundAnthropic);
    }

    @Test
    void testGetClientAgents() {
        // Setup: Record multiple agents using the same client
        trackingService.recordClientUsage("agent1", ModelProviderType.OPENAI, "gpt-4", "session1");
        trackingService.recordClientUsage("agent2", ModelProviderType.OPENAI, "gpt-4", "session2");

        // Get agents using the client
        List<String> clientAgents = trackingService.getClientAgents(ModelProviderType.OPENAI, "gpt-4");
        assertEquals(2, clientAgents.size());
        assertTrue(clientAgents.contains("agent1"));
        assertTrue(clientAgents.contains("agent2"));
    }

    @Test
    void testIsClientInUse() {
        // Test when client is not in use
        assertFalse(trackingService.isClientInUse(ModelProviderType.OPENAI, "gpt-4"));

        // Record client usage
        trackingService.recordClientUsage("agent1", ModelProviderType.OPENAI, "gpt-4", "session1");
        assertTrue(trackingService.isClientInUse(ModelProviderType.OPENAI, "gpt-4"));

        // Release client
        trackingService.recordClientRelease("agent1", ModelProviderType.OPENAI, "gpt-4");
        assertFalse(trackingService.isClientInUse(ModelProviderType.OPENAI, "gpt-4"));
    }

    @Test
    void testGetActiveAgentCount() {
        // Test when no agents are using the client
        assertEquals(0, trackingService.getActiveAgentCount(ModelProviderType.OPENAI, "gpt-4"));

        // Record multiple agents using the client
        trackingService.recordClientUsage("agent1", ModelProviderType.OPENAI, "gpt-4", "session1");
        trackingService.recordClientUsage("agent2", ModelProviderType.OPENAI, "gpt-4", "session2");
        assertEquals(2, trackingService.getActiveAgentCount(ModelProviderType.OPENAI, "gpt-4"));

        // Release one agent
        trackingService.recordClientRelease("agent1", ModelProviderType.OPENAI, "gpt-4");
        assertEquals(1, trackingService.getActiveAgentCount(ModelProviderType.OPENAI, "gpt-4"));
    }

    @Test
    void testGetClientPerformance() {
        // Setup: Record some requests with varying response times
        trackingService.recordClientUsage("agent1", ModelProviderType.OPENAI, "gpt-4", "session1");
        trackingService.recordRequestCompletion("agent1", ModelProviderType.OPENAI, "gpt-4", "req1", 100, 0.05, 1000,
                true);
        trackingService.recordRequestCompletion("agent1", ModelProviderType.OPENAI, "gpt-4", "req2", 200, 0.10, 2000,
                true);
        trackingService.recordRequestCompletion("agent1", ModelProviderType.OPENAI, "gpt-4", "req3", 150, 0.07, 3000,
                false);

        // Get performance metrics
        ModelTrackingService.ClientPerformanceMetrics performance = trackingService
                .getClientPerformance(ModelProviderType.OPENAI, "gpt-4");

        assertNotNull(performance);
        assertEquals(2000, performance.getAverageResponseTime(), 0.001); // (1000 + 2000 + 3000) / 3
        assertEquals(1000, performance.getMinResponseTime());
        assertEquals(3000, performance.getMaxResponseTime());
        assertEquals(0.333, performance.getErrorRate(), 0.001); // 1 error out of 3 requests
        assertEquals(1, performance.getTotalErrors());
        assertEquals(3, performance.getTotalRequests());
    }

    @Test
    void testClientUsageInfoCalculations() {
        // Create a client usage info and record some requests
        ModelTrackingService.ClientUsageInfo usage = new ModelTrackingService.ClientUsageInfo(ModelProviderType.OPENAI,
                "gpt-4");

        usage.addAgent("agent1", Instant.now());
        usage.recordRequest(100, 0.05, 1000, true);
        usage.recordRequest(200, 0.10, 2000, false);

        // Test calculations
        assertEquals(2, usage.getTotalRequests());
        assertEquals(300, usage.getTotalTokens());
        assertEquals(0.15, usage.getTotalCost(), 0.001);
        assertEquals(1500, usage.getAverageResponseTime(), 0.001); // (1000 + 2000) / 2
        assertEquals(0.5, usage.getSuccessRate(), 0.001); // 1 success out of 2 requests
        assertEquals(1, usage.getSuccessfulRequests());
        assertEquals(1, usage.getFailedRequests());
    }

    @Test
    void testProviderUsageStatsCalculations() {
        // Create provider stats and record some usage
        ModelTrackingService.ProviderUsageStats stats = new ModelTrackingService.ProviderUsageStats(
                ModelProviderType.OPENAI);

        stats.recordUsage("agent1", "gpt-4");
        stats.recordUsage("agent2", "gpt-4");
        stats.recordRequest(100, 0.05, 1000, true);
        stats.recordRequest(200, 0.10, 2000, false);

        // Test calculations
        assertEquals(2, stats.getTotalRequests());
        assertEquals(300, stats.getTotalTokens());
        assertEquals(0.15, stats.getTotalCost(), 0.001);
        assertEquals(1500, stats.getAverageResponseTime(), 0.001);
        assertEquals(0.5, stats.getSuccessRate(), 0.001);
        assertEquals(1, stats.getSuccessfulRequests());
        assertEquals(1, stats.getFailedRequests());

        // Test usage maps
        Map<String, Integer> modelUsageMap = stats.getModelUsage();
        assertEquals(2, modelUsageMap.get("gpt-4")); // 2 agents using gpt-4

        Map<String, Integer> agentUsageMap = stats.getAgentUsage();
        assertEquals(1, agentUsageMap.get("agent1")); // agent1 used once
        assertEquals(1, agentUsageMap.get("agent2")); // agent2 used once
    }
}
