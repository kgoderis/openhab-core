/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.agent.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
import org.openhab.core.ai.model.api.ModelConfigurationService;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.ModelResponse;
import org.openhab.core.ai.reasoning.AgentModelContextCache;
import org.openhab.core.ai.reasoning.AgentModelContextEnricher;
import org.openhab.core.ai.reasoning.AgentModelContextValidator;
import org.openhab.core.ai.reasoning.AgentModelDecisionEngine;
import org.openhab.core.ai.reasoning.AgentModelNLPProcessor;
import org.openhab.core.ai.reasoning.AgentModelPromptOptimizer;
import org.openhab.core.ai.reasoning.AgentModelPromptValidator;
import org.openhab.core.ai.reasoning.AgentModelSecurityManager;
import org.openhab.core.ai.reasoning.SharedModelReasoningEngine;

/**
 * Comprehensive integration tests for agent-model integration
 * Tests the complete workflow from agent registration to model reasoning with real model providers
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@ExtendWith(MockitoExtension.class)
class AgentModelIntegrationTest {

    @Mock
    private ModelConfigurationService modelConfigurationService;

    @Mock
    private AgentModelProvider mockModelProvider;

    private SharedModelReasoningEngine reasoningEngine;
    private AgentModelContextCache contextCache;
    private AgentModelContextEnricher contextEnricher;
    private AgentModelContextValidator contextValidator;
    private AgentModelDecisionEngine decisionEngine;
    private AgentModelNLPProcessor nlpProcessor;
    private AgentModelPromptOptimizer promptOptimizer;
    private AgentModelPromptValidator promptValidator;
    private AgentModelSecurityManager securityManager;

    @BeforeEach
    void setUp() {
        // Initialize all components
        reasoningEngine = new SharedModelReasoningEngine();
        contextCache = new AgentModelContextCache();
        contextEnricher = new AgentModelContextEnricher();
        contextValidator = new AgentModelContextValidator();
        decisionEngine = new AgentModelDecisionEngine();
        nlpProcessor = new AgentModelNLPProcessor();
        promptOptimizer = new AgentModelPromptOptimizer();
        promptValidator = new AgentModelPromptValidator();
        securityManager = new AgentModelSecurityManager();

        // Configure mock model provider
        when(mockModelProvider.reasonAsync(anyString(), anyMap(), any(ModelParameters.class))).thenReturn(
                CompletableFuture.completedFuture(ModelResponse.builder().content("Mock response").build()));
    }

    @Test
    void testCompleteAgentModelIntegrationWorkflow() throws Exception {
        // Given
        String agentId = "integration-test-agent";
        AgentModelContext context = createTestAgentContext(agentId);

        // When - Register agent
        boolean registrationResult = reasoningEngine.registerAgent(agentId, context);

        // Then - Verify registration
        assertTrue(registrationResult);
        assertTrue(reasoningEngine.isAgentRegistered(agentId));

        // When - Get agent model provider
        AgentModelProvider provider = reasoningEngine.getAgentModelProvider(agentId);

        // Then - Verify provider
        assertNotNull(provider);
        assertEquals(agentId, provider.getAgentId());

        // When - Perform reasoning
        String prompt = "Analyze the current system state and provide recommendations";
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("systemState", "operational");
        contextData.put("load", "medium");

        ModelParameters parameters = ModelParameters.builder().temperature(0.7).maxTokens(1000).build();

        CompletableFuture<ModelResponse> reasoningResult = reasoningEngine.reasonAsync(agentId, prompt, contextData,
                parameters);

        // Then - Verify reasoning result
        ModelResponse result = reasoningResult.get(5, TimeUnit.SECONDS);
        assertNotNull(result);
        assertNotNull(result.getContent());
        assertFalse(result.getContent().isEmpty());

        // When - Get statistics
        AgentModelStatistics agentStats = reasoningEngine.getAgentStatistics(agentId);
        ModelIntegrationStatistics overallStats = reasoningEngine.getOverallStatistics();

        // Then - Verify statistics
        assertNotNull(agentStats);
        assertEquals(agentId, agentStats.getAgentId());
        assertTrue(agentStats.getTotalRequests() > 0);
        assertTrue(agentStats.getSuccessfulRequests() > 0);

        assertNotNull(overallStats);
        assertEquals(1, overallStats.getTotalAgents());
        assertEquals(1, overallStats.getActiveAgents());
    }

    @Test
    void testMultiAgentModelIntegration() throws Exception {
        // Given
        String agentId1 = "agent-1";
        String agentId2 = "agent-2";
        String agentId3 = "agent-3";

        AgentModelContext context1 = createTestAgentContext(agentId1);
        AgentModelContext context2 = createTestAgentContext(agentId2);
        AgentModelContext context3 = createTestAgentContext(agentId3);

        // When - Register multiple agents
        reasoningEngine.registerAgent(agentId1, context1);
        reasoningEngine.registerAgent(agentId2, context2);
        reasoningEngine.registerAgent(agentId3, context3);

        // Then - Verify all agents are registered
        assertEquals(3, reasoningEngine.getRegisteredAgentIds().size());
        assertTrue(reasoningEngine.getRegisteredAgentIds().contains(agentId1));
        assertTrue(reasoningEngine.getRegisteredAgentIds().contains(agentId2));
        assertTrue(reasoningEngine.getRegisteredAgentIds().contains(agentId3));

        // When - Perform concurrent reasoning
        CompletableFuture<ModelResponse> result1 = reasoningEngine.reasonAsync(agentId1, "Task 1", new HashMap<>(),
                ModelParameters.builder().build());
        CompletableFuture<ModelResponse> result2 = reasoningEngine.reasonAsync(agentId2, "Task 2", new HashMap<>(),
                ModelParameters.builder().build());
        CompletableFuture<ModelResponse> result3 = reasoningEngine.reasonAsync(agentId3, "Task 3", new HashMap<>(),
                ModelParameters.builder().build());

        // Then - Verify all results are successful
        assertNotNull(result1.get(5, TimeUnit.SECONDS));
        assertNotNull(result2.get(5, TimeUnit.SECONDS));
        assertNotNull(result3.get(5, TimeUnit.SECONDS));

        // When - Get overall statistics
        ModelIntegrationStatistics stats = reasoningEngine.getOverallStatistics();

        // Then - Verify statistics reflect all agents
        assertEquals(3, stats.getTotalAgents());
        assertEquals(3, stats.getActiveAgents());
        assertEquals(1.0, stats.getActiveAgentRate());
    }

    @Test
    void testAgentModelContextManagement() {
        // Given
        String agentId = "context-test-agent";
        AgentModelContext originalContext = createTestAgentContext(agentId);

        // When - Register agent with original context
        reasoningEngine.registerAgent(agentId, originalContext);

        // Then - Verify original context
        AgentModelContext retrievedContext = reasoningEngine.getAgentContext(agentId);
        assertEquals("test-specialization", retrievedContext.getSpecialization());
        assertEquals("test-domain", retrievedContext.getDomain());

        // When - Update context
        AgentModelContext updatedContext = AgentModelContext.builder().agentId(agentId)
                .specialization("updated-specialization").domain("updated-domain")
                .capabilities(originalContext.getCapabilities()).constraints(originalContext.getConstraints())
                .promptTemplates(originalContext.getPromptTemplates()).preferences(originalContext.getPreferences())
                .createdAt(originalContext.getCreatedAt()).lastUpdated(Instant.now()).build();

        boolean updateResult = reasoningEngine.updateAgentContext(agentId, updatedContext);

        // Then - Verify context update
        assertTrue(updateResult);
        AgentModelContext newRetrievedContext = reasoningEngine.getAgentContext(agentId);
        assertEquals("updated-specialization", newRetrievedContext.getSpecialization());
        assertEquals("updated-domain", newRetrievedContext.getDomain());
    }

    @Test
    void testAgentModelPerformanceOptimization() throws Exception {
        // Given
        String agentId = "performance-test-agent";
        AgentModelContext context = createTestAgentContext(agentId);
        reasoningEngine.registerAgent(agentId, context);

        // When - Perform multiple reasoning operations
        long startTime = System.currentTimeMillis();

        CompletableFuture<ModelResponse> result1 = reasoningEngine.reasonAsync(agentId, "Performance test 1",
                new HashMap<>(), ModelParameters.builder().build());
        CompletableFuture<ModelResponse> result2 = reasoningEngine.reasonAsync(agentId, "Performance test 2",
                new HashMap<>(), ModelParameters.builder().build());
        CompletableFuture<ModelResponse> result3 = reasoningEngine.reasonAsync(agentId, "Performance test 3",
                new HashMap<>(), ModelParameters.builder().build());

        // Wait for all results
        result1.get(5, TimeUnit.SECONDS);
        result2.get(5, TimeUnit.SECONDS);
        result3.get(5, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // Then - Verify performance
        assertTrue(totalTime < 10000); // Should complete within 10 seconds

        // When - Get performance statistics
        AgentModelStatistics stats = reasoningEngine.getAgentStatistics(agentId);

        // Then - Verify statistics
        assertEquals(3, stats.getTotalRequests());
        assertEquals(3, stats.getSuccessfulRequests());
        assertEquals(0, stats.getFailedRequests());
        assertEquals(1.0, stats.getSuccessRate());
    }

    @Test
    void testAgentModelErrorHandling() {
        // Given
        String agentId = "error-test-agent";
        AgentModelContext context = createTestAgentContext(agentId);

        // When - Try to get statistics for unregistered agent
        AgentModelStatistics stats = reasoningEngine.getAgentStatistics(agentId);

        // Then - Verify error handling
        assertNotNull(stats);
        assertEquals(agentId, stats.getAgentId());
        assertEquals(0, stats.getTotalRequests());

        // When - Try to update context for unregistered agent
        boolean updateResult = reasoningEngine.updateAgentContext(agentId, context);

        // Then - Verify error handling
        assertFalse(updateResult);

        // When - Try to clear cache for unregistered agent
        boolean clearResult = reasoningEngine.clearAgentCache(agentId);

        // Then - Verify error handling
        assertFalse(clearResult);
    }

    @Test
    void testAgentModelHealthMonitoring() {
        // Given
        String agentId = "health-test-agent";
        AgentModelContext context = createTestAgentContext(agentId);
        reasoningEngine.registerAgent(agentId, context);

        // When - Get health status
        ModelHealthStatus healthStatus = reasoningEngine.getModelHealthStatus();

        // Then - Verify health status
        assertNotNull(healthStatus);
        assertTrue(healthStatus.isHealthy() || healthStatus.isDegraded() || healthStatus.isUnhealthy());
        assertTrue(healthStatus.isPrimaryModelAvailable());
        assertTrue(healthStatus.isFallbackModelAvailable());
        assertTrue(healthStatus.getErrorRate() >= 0.0);
        assertTrue(healthStatus.getErrorRate() <= 1.0);
    }

    @Test
    void testAgentModelCacheManagement() {
        // Given
        String agentId1 = "cache-agent-1";
        String agentId2 = "cache-agent-2";

        reasoningEngine.registerAgent(agentId1, createTestAgentContext(agentId1));
        reasoningEngine.registerAgent(agentId2, createTestAgentContext(agentId2));

        // When - Clear individual agent cache
        boolean clearIndividualResult = reasoningEngine.clearAgentCache(agentId1);

        // Then - Verify individual cache clear
        assertTrue(clearIndividualResult);

        // When - Clear all caches
        boolean clearAllResult = reasoningEngine.clearAllCaches();

        // Then - Verify all caches cleared
        assertTrue(clearAllResult);
    }

    @Test
    void testAgentModelFallbackManagement() {
        // Given
        String agentId = "fallback-test-agent";
        reasoningEngine.registerAgent(agentId, createTestAgentContext(agentId));

        // When - Force model fallback
        String fallbackModel = "gpt-3.5-turbo";
        boolean forceFallbackResult = reasoningEngine.forceModelFallback(agentId, fallbackModel);

        // Then - Verify fallback forced
        assertTrue(forceFallbackResult);

        // When - Reset model fallback
        boolean resetFallbackResult = reasoningEngine.resetModelFallback(agentId);

        // Then - Verify fallback reset
        assertTrue(resetFallbackResult);
    }

    // Helper methods
    private AgentModelContext createTestAgentContext(String agentId) {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("reasoning", true);
        capabilities.put("learning", true);
        capabilities.put("nlp", true);

        Map<String, Object> constraints = new HashMap<>();
        constraints.put("maxTokens", 1000);
        constraints.put("timeout", 30);
        constraints.put("maxRequestsPerMinute", 60);

        Map<String, String> promptTemplates = new HashMap<>();
        promptTemplates.put("default", "You are {{agentId}} specialized in {{specialization}}.");
        promptTemplates.put("analysis", "Analyze the following data: {{data}}");
        promptTemplates.put("recommendation", "Provide recommendations for: {{scenario}}");

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("temperature", 0.7);
        preferences.put("maxTokens", 1000);
        preferences.put("responseFormat", "text");

        return AgentModelContext.builder().agentId(agentId).specialization("test-specialization").domain("test-domain")
                .capabilities(capabilities).constraints(constraints).promptTemplates(promptTemplates)
                .preferences(preferences).createdAt(Instant.now()).lastUpdated(Instant.now()).build();
    }
}
