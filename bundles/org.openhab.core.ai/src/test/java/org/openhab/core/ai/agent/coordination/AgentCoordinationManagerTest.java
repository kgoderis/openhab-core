package org.openhab.core.ai.agent.coordination;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for AgentCoordinationManager
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class AgentCoordinationManagerTest {

    private AgentCoordinationManager coordinationManager;

    @BeforeEach
    void setUp() {
        coordinationManager = new AgentCoordinationManager();
    }

    @Test
    void testGetStatistics() {
        AgentCoordinationManager.CoordinationStatistics stats = coordinationManager.getStatistics();

        assertNotNull(stats);
        assertEquals(0, stats.getTotalSessions());
        assertEquals(0, stats.getTotalConflictResolutions());
        assertEquals(0, stats.getTotalContextSharing());
        assertEquals(0, stats.getTotalProtocolExecutions());
        assertEquals(0, stats.getActiveSessions());
        assertEquals(0, stats.getActiveConflictSessions());
        assertEquals(0, stats.getSharedContexts());
        assertEquals(0, stats.getRegisteredProtocols());
    }

    @Test
    void testCreateSharedContext() {
        String contextId = "test-context";
        List<String> agentIds = List.of("agent1", "agent2");
        Map<String, Object> contextData = Map.of("key", "value");

        AgentCoordinationManager.SharedContext context = coordinationManager.createSharedContext(contextId, agentIds,
                contextData, AgentCoordinationManager.ContextAccessLevel.SHARED);

        assertNotNull(context);
        assertEquals(contextId, context.getContextId());
        assertEquals(agentIds, context.getAgentIds());
        assertEquals(contextData, context.getContextData());
        assertEquals(AgentCoordinationManager.ContextAccessLevel.SHARED, context.getAccessLevel());
        assertEquals(1, context.getVersion());
    }

    @Test
    void testGetSharedContext() {
        // Create a context first
        String contextId = "test-context";
        List<String> agentIds = List.of("agent1", "agent2");
        Map<String, Object> contextData = Map.of("key", "value");

        coordinationManager.createSharedContext(contextId, agentIds, contextData,
                AgentCoordinationManager.ContextAccessLevel.SHARED);

        // Test access by participant
        AgentCoordinationManager.SharedContext context = coordinationManager.getSharedContext(contextId, "agent1");
        assertNotNull(context);
        assertEquals(contextId, context.getContextId());

        // Test access by non-participant (should be denied for non-public contexts)
        AgentCoordinationManager.SharedContext deniedContext = coordinationManager.getSharedContext(contextId,
                "agent3");
        assertNull(deniedContext);
    }

    @Test
    void testUpdateSharedContext() {
        // Create a context first
        String contextId = "test-context";
        List<String> agentIds = List.of("agent1", "agent2");
        Map<String, Object> contextData = Map.of("key", "value");

        coordinationManager.createSharedContext(contextId, agentIds, contextData,
                AgentCoordinationManager.ContextAccessLevel.SHARED);

        // Update context
        Map<String, Object> updates = Map.of("newKey", "newValue");
        AgentCoordinationManager.SharedContext updatedContext = coordinationManager.updateSharedContext(contextId,
                updates, "agent1");

        assertNotNull(updatedContext);
        assertEquals(2, updatedContext.getVersion());
        assertTrue(updatedContext.getContextData().containsKey("newKey"));
        assertEquals("newValue", updatedContext.getContextData().get("newKey"));
    }

    @Test
    void testGetActiveSessions() {
        List<AgentCoordinationManager.CoordinationSession> sessions = coordinationManager.getActiveSessions();
        assertNotNull(sessions);
        assertTrue(sessions.isEmpty());
    }

    @Test
    void testGetActiveConflictSessions() {
        List<AgentCoordinationManager.ConflictResolutionSession> sessions = coordinationManager
                .getActiveConflictSessions();
        assertNotNull(sessions);
        assertTrue(sessions.isEmpty());
    }

    @Test
    void testRegisterProtocol() {
        String protocolId = "test-protocol";
        AgentCoordinationManager.CoordinationProtocol protocol = session -> CompletableFuture
                .completedFuture(new AgentCoordinationManager.CoordinationResult() {
                    @Override
                    public boolean isSuccess() {
                        return true;
                    }

                    @Override
                    public String getMessage() {
                        return "Test protocol executed";
                    }

                    @Override
                    public Map<String, Object> getData() {
                        return Map.of();
                    }
                });

        coordinationManager.registerProtocol(protocolId, protocol);

        // Verify protocol was registered (indirectly through statistics)
        AgentCoordinationManager.CoordinationStatistics stats = coordinationManager.getStatistics();
        assertEquals(1, stats.getRegisteredProtocols());
    }

    @Test
    void testCleanupCompletedSessions() {
        // This test verifies the cleanup method doesn't throw exceptions
        assertDoesNotThrow(() -> coordinationManager.cleanupCompletedSessions());
    }
}
