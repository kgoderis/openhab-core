package org.openhab.core.ai.agent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.agent.api.AgentModelContext;
import org.openhab.core.ai.agent.api.AgentModelProvider;
import org.openhab.core.ai.agent.api.AgentModelStatistics;
import org.openhab.core.ai.agent.api.ModelHealthStatus;
import org.openhab.core.ai.agent.api.ModelIntegrationStatistics;
import org.openhab.core.ai.model.AgentModelIntegrationServiceImpl;
import org.openhab.core.ai.model.api.ModelConfigurationService;
import org.openhab.core.ai.model.api.ModelParameters;

/**
 * Unit tests for AgentModelIntegrationService
 * 
 * @author Karel Goderis - Initial Contribution
 */
@ExtendWith(MockitoExtension.class)
class AgentModelIntegrationServiceTest {

    @Mock
    private ModelConfigurationService modelConfigurationService;

    private AgentModelIntegrationServiceImpl integrationService;

    @BeforeEach
    void setUp() {
        integrationService = new AgentModelIntegrationServiceImpl();
        // Inject mock dependencies
        // Note: In a real OSGi environment, this would be done via dependency injection
    }

    @Test
    void testRegisterAgent() {
        // Given
        String agentId = "test-agent";
        AgentModelContext context = createTestAgentContext(agentId);

        // When
        boolean result = integrationService.registerAgent(agentId, context);

        // Then
        assertTrue(result);
        assertTrue(integrationService.isAgentRegistered(agentId));
        assertEquals(1, integrationService.getRegisteredAgentIds().size());
        assertTrue(integrationService.getRegisteredAgentIds().contains(agentId));
    }

    @Test
    void testUnregisterAgent() {
        // Given
        String agentId = "test-agent";
        AgentModelContext context = createTestAgentContext(agentId);
        integrationService.registerAgent(agentId, context);

        // When
        boolean result = integrationService.unregisterAgent(agentId);

        // Then
        assertTrue(result);
        assertFalse(integrationService.isAgentRegistered(agentId));
        assertEquals(0, integrationService.getRegisteredAgentIds().size());
    }

    @Test
    void testUpdateAgentContext() {
        // Given
        String agentId = "test-agent";
        AgentModelContext originalContext = createTestAgentContext(agentId);
        integrationService.registerAgent(agentId, originalContext);

        AgentModelContext updatedContext = AgentModelContext.builder().agentId(agentId)
                .specialization("updated-specialization").domain("updated-domain").build();

        // When
        boolean result = integrationService.updateAgentContext(agentId, updatedContext);

        // Then
        assertTrue(result);
        AgentModelContext retrievedContext = integrationService.getAgentContext(agentId);
        assertNotNull(retrievedContext);
        assertEquals("updated-specialization", retrievedContext.getSpecialization());
        assertEquals("updated-domain", retrievedContext.getDomain());
    }

    @Test
    void testUpdateAgentContextForUnregisteredAgent() {
        // Given
        String agentId = "unregistered-agent";
        AgentModelContext context = createTestAgentContext(agentId);

        // When
        boolean result = integrationService.updateAgentContext(agentId, context);

        // Then
        assertFalse(result);
    }

    @Test
    void testGetAgentStatistics() {
        // Given
        String agentId = "test-agent";
        AgentModelContext context = createTestAgentContext(agentId);
        integrationService.registerAgent(agentId, context);

        // When
        AgentModelStatistics statistics = integrationService.getAgentStatistics(agentId);

        // Then
        assertNotNull(statistics);
        assertEquals(agentId, statistics.getAgentId());
        assertEquals(0, statistics.getTotalRequests());
        assertEquals(0, statistics.getSuccessfulRequests());
        assertEquals(0, statistics.getFailedRequests());
    }

    @Test
    void testGetOverallStatistics() {
        // Given
        String agentId1 = "test-agent-1";
        String agentId2 = "test-agent-2";
        integrationService.registerAgent(agentId1, createTestAgentContext(agentId1));
        integrationService.registerAgent(agentId2, createTestAgentContext(agentId2));

        // When
        ModelIntegrationStatistics statistics = integrationService.getOverallStatistics();

        // Then
        assertNotNull(statistics);
        assertEquals(2, statistics.getTotalAgents());
        assertEquals(2, statistics.getActiveAgents());
        assertEquals(0, statistics.getTotalRequests());
        assertEquals(0, statistics.getSuccessfulRequests());
        assertEquals(0, statistics.getFailedRequests());
        assertEquals(1.0, statistics.getActiveAgentRate());
    }

    @Test
    void testGetModelHealthStatus() {
        // Given
        String agentId = "test-agent";
        integrationService.registerAgent(agentId, createTestAgentContext(agentId));

        // When
        ModelHealthStatus healthStatus = integrationService.getModelHealthStatus();

        // Then
        assertNotNull(healthStatus);
        assertTrue(healthStatus.isHealthy() || healthStatus.isDegraded() || healthStatus.isUnhealthy());
        assertTrue(healthStatus.isPrimaryModelAvailable());
        assertTrue(healthStatus.isFallbackModelAvailable());
        assertEquals(0.0, healthStatus.getErrorRate());
    }

    @Test
    void testClearAgentCache() {
        // Given
        String agentId = "test-agent";
        integrationService.registerAgent(agentId, createTestAgentContext(agentId));

        // When
        boolean result = integrationService.clearAgentCache(agentId);

        // Then
        assertTrue(result);
    }

    @Test
    void testClearAgentCacheForUnregisteredAgent() {
        // Given
        String agentId = "unregistered-agent";

        // When
        boolean result = integrationService.clearAgentCache(agentId);

        // Then
        assertFalse(result);
    }

    @Test
    void testClearAllCaches() {
        // Given
        String agentId1 = "test-agent-1";
        String agentId2 = "test-agent-2";
        integrationService.registerAgent(agentId1, createTestAgentContext(agentId1));
        integrationService.registerAgent(agentId2, createTestAgentContext(agentId2));

        // When
        boolean result = integrationService.clearAllCaches();

        // Then
        assertTrue(result);
    }

    @Test
    void testForceModelFallback() {
        // Given
        String agentId = "test-agent";
        integrationService.registerAgent(agentId, createTestAgentContext(agentId));
        String fallbackModel = "gpt-3.5-turbo";

        // When
        boolean result = integrationService.forceModelFallback(agentId, fallbackModel);

        // Then
        assertTrue(result);
    }

    @Test
    void testForceModelFallbackForUnregisteredAgent() {
        // Given
        String agentId = "unregistered-agent";
        String fallbackModel = "gpt-3.5-turbo";

        // When
        boolean result = integrationService.forceModelFallback(agentId, fallbackModel);

        // Then
        assertFalse(result);
    }

    @Test
    void testResetModelFallback() {
        // Given
        String agentId = "test-agent";
        integrationService.registerAgent(agentId, createTestAgentContext(agentId));

        // When
        boolean result = integrationService.resetModelFallback(agentId);

        // Then
        assertTrue(result);
    }

    @Test
    void testResetModelFallbackForUnregisteredAgent() {
        // Given
        String agentId = "unregistered-agent";

        // When
        boolean result = integrationService.resetModelFallback(agentId);

        // Then
        assertFalse(result);
    }

    @Test
    void testGetAgentModelProvider() {
        // Given
        String agentId = "test-agent";
        integrationService.registerAgent(agentId, createTestAgentContext(agentId));

        // When
        AgentModelProvider provider = integrationService.getAgentModelProvider(agentId);

        // Then
        assertNotNull(provider);
        assertEquals(agentId, provider.getAgentId());
    }

    @Test
    void testGetAgentModelProviderForUnregisteredAgent() {
        // Given
        String agentId = "unregistered-agent";

        // When
        AgentModelProvider provider = integrationService.getAgentModelProvider(agentId);

        // Then
        assertNull(provider);
    }

    @Test
    void testReasonAsyncWithUnregisteredAgent() {
        // Given
        String agentId = "unregistered-agent";
        String prompt = "Test prompt";
        Map<String, Object> context = new HashMap<>();
        ModelParameters parameters = ModelParameters.builder().build();

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            integrationService.reasonAsync(agentId, prompt, context, parameters).get();
        });
    }

    @Test
    void testReasonWithOptimizationAsyncWithUnregisteredAgent() {
        // Given
        String agentId = "unregistered-agent";
        String prompt = "Test prompt";
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> optimizationHints = new HashMap<>();
        ModelParameters parameters = ModelParameters.builder().build();

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            integrationService.reasonWithOptimizationAsync(agentId, prompt, context, optimizationHints, parameters)
                    .get();
        });
    }

    @Test
    void testMultipleAgentRegistration() {
        // Given
        String agentId1 = "test-agent-1";
        String agentId2 = "test-agent-2";
        String agentId3 = "test-agent-3";

        // When
        integrationService.registerAgent(agentId1, createTestAgentContext(agentId1));
        integrationService.registerAgent(agentId2, createTestAgentContext(agentId2));
        integrationService.registerAgent(agentId3, createTestAgentContext(agentId3));

        // Then
        assertEquals(3, integrationService.getRegisteredAgentIds().size());
        assertTrue(integrationService.getRegisteredAgentIds().contains(agentId1));
        assertTrue(integrationService.getRegisteredAgentIds().contains(agentId2));
        assertTrue(integrationService.getRegisteredAgentIds().contains(agentId3));
    }

    @Test
    void testAgentRegistrationWithDuplicateId() {
        // Given
        String agentId = "test-agent";
        AgentModelContext context1 = createTestAgentContext(agentId);
        AgentModelContext context2 = AgentModelContext.builder().agentId(agentId)
                .specialization("different-specialization").domain("different-domain").build();

        // When
        boolean result1 = integrationService.registerAgent(agentId, context1);
        boolean result2 = integrationService.registerAgent(agentId, context2);

        // Then
        assertTrue(result1);
        assertTrue(result2); // Should overwrite the previous registration
        assertEquals(1, integrationService.getRegisteredAgentIds().size());

        AgentModelContext retrievedContext = integrationService.getAgentContext(agentId);
        assertEquals("different-specialization", retrievedContext.getSpecialization());
    }

    // Helper methods
    private AgentModelContext createTestAgentContext(String agentId) {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("reasoning", true);
        capabilities.put("learning", true);

        Map<String, Object> constraints = new HashMap<>();
        constraints.put("maxTokens", 1000);
        constraints.put("timeout", 30);

        Map<String, String> promptTemplates = new HashMap<>();
        promptTemplates.put("default", "You are {{agentId}} specialized in {{specialization}}.");

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("temperature", 0.7);
        preferences.put("maxTokens", 1000);

        return AgentModelContext.builder().agentId(agentId).specialization("test-specialization").domain("test-domain")
                .capabilities(capabilities).constraints(constraints).promptTemplates(promptTemplates)
                .preferences(preferences).createdAt(Instant.now()).lastUpdated(Instant.now()).build();
    }
}
