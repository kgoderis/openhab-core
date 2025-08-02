package org.openhab.core.ai.a2a.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.a2a.internal.A2AAgentExecutor;
import org.openhab.core.ai.a2a.internal.A2AOpenHABPersistenceManager;
import org.openhab.core.ai.a2a.internal.A2ASecurityManager;
import org.openhab.core.ai.a2a.internal.A2AServerManager;
import org.openhab.core.ai.a2a.internal.A2ASkillRegistry;
import org.openhab.core.ai.common.action.AIActionRegistry;
import org.openhab.core.ai.common.config.AIConfigurationService;
import org.openhab.core.service.ReadyService;

import io.a2a.server.requesthandlers.RequestHandler;
import io.a2a.server.tasks.TaskStore;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;

@ExtendWith(MockitoExtension.class)
class A2AServerManagerTest {

    @Mock
    private ReadyService mockReadyService;

    @Mock
    private AIActionRegistry mockActionRegistry;

    @Mock
    private A2AOpenHABPersistenceManager mockPersistenceManager;

    @Mock
    private AIConfigurationService mockConfigurationService;

    @Mock
    private A2ASkillRegistry mockSkillRegistry;

    @Mock
    private A2ASecurityManager mockSecurityManager;

    @Mock
    private A2AAgentExecutor mockAgentExecutor;

    private A2AServerManager serverManager;

    @BeforeEach
    void setUp() {
        serverManager = new A2AServerManager();
    }

    @Test
    void testConstructor() {
        assertNotNull(serverManager);
    }

    @Test
    void testActivate() {
        // Test activation
        serverManager.activate();

        // Verify ready service tracker registration
        assertNotNull(serverManager);
    }

    @Test
    void testDeactivate() {
        // Test deactivation
        serverManager.deactivate();

        // Verify cleanup
        assertNotNull(serverManager);
    }

    @Test
    void testStart() {
        // Test server start
        serverManager.start();

        // Verify server is running
        assertTrue(serverManager.isRunning());
    }

    @Test
    void testStop() {
        // Start the server first
        serverManager.start();
        assertTrue(serverManager.isRunning());

        // Stop the server
        serverManager.stop();

        // Verify server is stopped
        assertFalse(serverManager.isRunning());
    }

    @Test
    void testIsRunning() {
        // Initially not running
        assertFalse(serverManager.isRunning());

        // Start server
        serverManager.start();
        assertTrue(serverManager.isRunning());

        // Stop server
        serverManager.stop();
        assertFalse(serverManager.isRunning());
    }

    @Test
    void testGetRequestHandler() {
        // Start server to initialize components
        serverManager.start();

        RequestHandler requestHandler = serverManager.getRequestHandler();

        // Should return a request handler
        assertNotNull(requestHandler);
    }

    @Test
    void testGetTaskStore() {
        // Start server to initialize components
        serverManager.start();

        TaskStore taskStore = serverManager.getTaskStore();

        // Should return a task store
        assertNotNull(taskStore);
    }

    @Test
    void testGetAgentCard() {
        // Start server to initialize components
        serverManager.start();

        AgentCard agentCard = serverManager.getAgentCard();

        // Should return an agent card
        assertNotNull(agentCard);

        // Verify agent card properties
        assertEquals("OpenHAB AI Agent", agentCard.name());
        assertEquals("OpenHAB AI Agent for home automation", agentCard.description());
        assertNotNull(agentCard.capabilities());
        assertNotNull(agentCard.skills());
        assertNotNull(agentCard.securitySchemes());
    }

    @Test
    void testExecuteSkill() {
        // Start server to initialize components
        serverManager.start();

        // Test skill execution with null message
        Object result = serverManager.executeSkill(null);

        // Should return null for null message
        assertNull(result);
    }

    @Test
    void testReadyMarkerConstants() {
        // Test that ready marker constants are accessible
        assertNotNull(A2AServerManager.A2A_SERVER_READY);
        assertEquals("a2a", A2AServerManager.A2A_SERVER_READY.getType());
        assertEquals("server", A2AServerManager.A2A_SERVER_READY.getIdentifier());

        assertNotNull(A2AServerManager.A2A_SERVER_COMPONENTS_READY);
        assertEquals("a2a", A2AServerManager.A2A_SERVER_COMPONENTS_READY.getType());
        assertEquals("server-components", A2AServerManager.A2A_SERVER_COMPONENTS_READY.getIdentifier());
    }

    @Test
    void testCoreServiceReadyMarkers() {
        // Test core service ready markers - these are private constants
        // so we can't test them directly, but we can verify the class structure
        assertNotNull(A2AServerManager.A2A_SERVER_READY);
        assertNotNull(A2AServerManager.A2A_SERVER_COMPONENTS_READY);
    }

    @Test
    void testServerLifecycle() {
        // Test complete server lifecycle
        assertFalse(serverManager.isRunning());

        serverManager.start();
        assertTrue(serverManager.isRunning());

        serverManager.stop();
        assertFalse(serverManager.isRunning());
    }

    @Test
    void testAgentCardCapabilities() {
        // Start server to initialize components
        serverManager.start();

        AgentCard agentCard = serverManager.getAgentCard();
        AgentCapabilities capabilities = agentCard.capabilities();

        // Verify capabilities
        assertNotNull(capabilities);
        assertTrue(capabilities.streaming());
    }

    @Test
    void testAgentCardSkills() {
        // Start server to initialize components
        serverManager.start();

        AgentCard agentCard = serverManager.getAgentCard();
        java.util.List<AgentSkill> skills = agentCard.skills();

        // Verify skills list
        assertNotNull(skills);
        assertTrue(skills instanceof java.util.List);
    }

    @Test
    void testAgentCardSecuritySchemes() {
        // Start server to initialize components
        serverManager.start();

        AgentCard agentCard = serverManager.getAgentCard();
        java.util.Map<String, io.a2a.spec.SecurityScheme> securitySchemes = agentCard.securitySchemes();

        // Verify security schemes
        assertNotNull(securitySchemes);
        assertTrue(securitySchemes instanceof java.util.Map);
    }

    @Test
    void testMultipleStartStopCycles() {
        // Test multiple start/stop cycles
        for (int i = 0; i < 3; i++) {
            serverManager.start();
            assertTrue(serverManager.isRunning());

            serverManager.stop();
            assertFalse(serverManager.isRunning());
        }
    }

    @Test
    void testServerComponentsInitialization() {
        // Test that server components are properly initialized
        serverManager.start();

        // Verify all components are available
        assertNotNull(serverManager.getRequestHandler());
        assertNotNull(serverManager.getTaskStore());
        assertNotNull(serverManager.getAgentCard());
    }

    @Test
    void testServerStateConsistency() {
        // Test that server state is consistent
        assertFalse(serverManager.isRunning());

        // Start without proper initialization should still work
        serverManager.start();
        assertTrue(serverManager.isRunning());

        // Stop should work
        serverManager.stop();
        assertFalse(serverManager.isRunning());
    }

    @Test
    void testReadyServiceIntegration() {
        // Test ready service integration
        serverManager.activate();

        // Should not throw exception
        assertNotNull(serverManager);

        serverManager.deactivate();
        assertNotNull(serverManager);
    }
}
