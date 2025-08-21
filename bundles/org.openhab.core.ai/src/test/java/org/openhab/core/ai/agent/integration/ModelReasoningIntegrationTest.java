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
import org.openhab.core.ai.agent.api.AgentModelProvider;
import org.openhab.core.ai.common.context.AgentModelContext;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.common.response.ModelResponse;
import org.openhab.core.ai.model.api.ModelConfigurationService;
import org.openhab.core.ai.reasoning.engine.SharedModelReasoningEngine;

/**
 * Integration tests for model reasoning with real model providers
 * Tests the reasoning capabilities with various scenarios and model configurations
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@ExtendWith(MockitoExtension.class)
class ModelReasoningIntegrationTest {

    @Mock
    private ModelConfigurationService modelConfigurationService;

    @Mock
    private AgentModelProvider mockModelProvider;

    private SharedModelReasoningEngine reasoningEngine;

    @BeforeEach
    void setUp() {
        reasoningEngine = new SharedModelReasoningEngine();

        // Configure mock model provider for realistic responses
        when(mockModelProvider.reasonAsync(anyString(), anyMap(), any(ModelParameters.class)))
                .thenReturn(CompletableFuture
                        .completedFuture(ModelResponse.builder().withContent("Mock reasoning response").build()));
    }

    @Test
    void testBasicReasoningWithModelProvider() throws Exception {
        // Given
        String agentId = "reasoning-test-agent";
        AgentModelContext context = createTestAgentContext(agentId);
        reasoningEngine.registerAgent(agentId, context);

        // When - Perform basic reasoning
        String prompt = "What is the current system status?";
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("systemStatus", "operational");
        contextData.put("timestamp", Instant.now().toString());

        ModelParameters parameters = ModelParameters.builder().withTemperature(0.7).withMaxTokens(500).build();

        CompletableFuture<ModelResponse> result = reasoningEngine.reasonAsync(agentId, prompt, contextData, parameters);

        // Then - Verify reasoning result
        ModelResponse response = result.get(5, TimeUnit.SECONDS);
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertFalse(response.getContent().isEmpty());
    }

    @Test
    void testComplexReasoningWithMultipleContexts() throws Exception {
        // Given
        String agentId = "complex-reasoning-agent";
        AgentModelContext context = createTestAgentContext(agentId);
        reasoningEngine.registerAgent(agentId, context);

        // When - Perform complex reasoning with rich context
        String prompt = "Analyze the system performance and provide optimization recommendations";
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("cpuUsage", 75.5);
        contextData.put("memoryUsage", 60.2);
        contextData.put("diskUsage", 45.8);
        contextData.put("networkLatency", 12.3);
        contextData.put("activeConnections", 150);
        contextData.put("errorRate", 0.02);
        contextData.put("uptime", 86400);

        ModelParameters parameters = ModelParameters.builder().withTemperature(0.5).withMaxTokens(1000).build();

        CompletableFuture<ModelResponse> result = reasoningEngine.reasonAsync(agentId, prompt, contextData, parameters);

        // Then - Verify reasoning result
        ModelResponse response = result.get(5, TimeUnit.SECONDS);
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertFalse(response.getContent().isEmpty());
    }

    @Test
    void testReasoningWithOptimizationHints() throws Exception {
        // Given
        String agentId = "optimization-agent";
        AgentModelContext context = createTestAgentContext(agentId);
        reasoningEngine.registerAgent(agentId, context);

        // When - Perform reasoning with optimization hints
        String prompt = "Optimize the system configuration for better performance";
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("currentConfig", "default");
        contextData.put("performanceTarget", "high");
        contextData.put("resourceConstraints", "limited");

        Map<String, Object> optimizationHints = new HashMap<>();
        optimizationHints.put("focus", "memory_optimization");
        optimizationHints.put("priority", "high");
        optimizationHints.put("constraints", "budget_limited");

        ModelParameters parameters = ModelParameters.builder().withTemperature(0.3).withMaxTokens(800).build();

        CompletableFuture<ModelResponse> result = reasoningEngine.reasonWithOptimizationAsync(agentId, prompt,
                contextData, optimizationHints, parameters);

        // Then - Verify reasoning result
        ModelResponse response = result.get(5, TimeUnit.SECONDS);
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertFalse(response.getContent().isEmpty());
    }

    @Test
    void testReasoningWithDifferentModelParameters() throws Exception {
        // Given
        String agentId = "parameter-test-agent";
        AgentModelContext context = createTestAgentContext(agentId);
        reasoningEngine.registerAgent(agentId, context);

        // Test different parameter combinations
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("testData", "sample");

        // Test 1: Low temperature for deterministic responses
        ModelParameters lowTempParams = ModelParameters.builder().withTemperature(0.1).withMaxTokens(200).build();

        CompletableFuture<ModelResponse> result1 = reasoningEngine.reasonAsync(agentId, "Provide a concise answer",
                contextData, lowTempParams);
        ModelResponse response1 = result1.get(5, TimeUnit.SECONDS);
        assertNotNull(response1);

        // Test 2: High temperature for creative responses
        ModelParameters highTempParams = ModelParameters.builder().withTemperature(0.9).withMaxTokens(500).build();

        CompletableFuture<ModelResponse> result2 = reasoningEngine.reasonAsync(agentId, "Provide creative suggestions",
                contextData, highTempParams);
        ModelResponse response2 = result2.get(5, TimeUnit.SECONDS);
        assertNotNull(response2);

        // Test 3: High token limit for detailed responses
        ModelParameters highTokenParams = ModelParameters.builder().withTemperature(0.5).withMaxTokens(2000).build();

        CompletableFuture<ModelResponse> result3 = reasoningEngine.reasonAsync(agentId, "Provide a detailed analysis",
                contextData, highTokenParams);
        ModelResponse response3 = result3.get(5, TimeUnit.SECONDS);
        assertNotNull(response3);
    }

    @Test
    void testConcurrentReasoningRequests() throws Exception {
        // Given
        String agentId = "concurrent-agent";
        AgentModelContext context = createTestAgentContext(agentId);
        reasoningEngine.registerAgent(agentId, context);

        // When - Submit multiple concurrent reasoning requests
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("requestId", "concurrent-test");

        ModelParameters parameters = ModelParameters.builder().withTemperature(0.5).withMaxTokens(300).build();

        CompletableFuture<ModelResponse>[] results = new CompletableFuture[5];
        for (int i = 0; i < 5; i++) {
            String prompt = "Process request " + i;
            results[i] = reasoningEngine.reasonAsync(agentId, prompt, contextData, parameters);
        }

        // Then - Verify all results are successful
        for (int i = 0; i < 5; i++) {
            ModelResponse response = results[i].get(10, TimeUnit.SECONDS);
            assertNotNull(response);
            assertNotNull(response.getContent());
        }
    }

    @Test
    void testReasoningWithAgentSpecificContext() throws Exception {
        // Given
        String agentId = "specialized-agent";
        AgentModelContext context = createSpecializedAgentContext(agentId);
        reasoningEngine.registerAgent(agentId, context);

        // When - Perform reasoning with agent-specific context
        String prompt = "Based on your specialization, analyze this scenario";
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("scenario", "system_optimization");
        contextData.put("domain", "performance");

        ModelParameters parameters = ModelParameters.builder().withTemperature(0.6).withMaxTokens(600).build();

        CompletableFuture<ModelResponse> result = reasoningEngine.reasonAsync(agentId, prompt, contextData, parameters);

        // Then - Verify reasoning result
        ModelResponse response = result.get(5, TimeUnit.SECONDS);
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertFalse(response.getContent().isEmpty());
    }

    @Test
    void testReasoningErrorHandling() {
        // Given
        String agentId = "error-test-agent";
        AgentModelContext context = createTestAgentContext(agentId);

        // When - Try to reason with unregistered agent
        String prompt = "Test prompt";
        Map<String, Object> contextData = new HashMap<>();
        ModelParameters parameters = ModelParameters.builder().build();

        // Then - Verify error handling
        assertThrows(RuntimeException.class, () -> {
            reasoningEngine.reasonAsync(agentId, prompt, contextData, parameters).get();
        });
    }

    @Test
    void testReasoningPerformanceMetrics() throws Exception {
        // Given
        String agentId = "performance-agent";
        AgentModelContext context = createTestAgentContext(agentId);
        reasoningEngine.registerAgent(agentId, context);

        // When - Perform multiple reasoning operations and measure performance
        long startTime = System.currentTimeMillis();

        Map<String, Object> contextData = new HashMap<>();
        ModelParameters parameters = ModelParameters.builder().withTemperature(0.5).withMaxTokens(300).build();

        CompletableFuture<ModelResponse>[] results = new CompletableFuture[10];
        for (int i = 0; i < 10; i++) {
            String prompt = "Performance test " + i;
            results[i] = reasoningEngine.reasonAsync(agentId, prompt, contextData, parameters);
        }

        // Wait for all results
        for (CompletableFuture<ModelResponse> result : results) {
            result.get(5, TimeUnit.SECONDS);
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // Then - Verify performance
        assertTrue(totalTime < 15000); // Should complete within 15 seconds for 10 requests

        // Verify all results are successful
        for (CompletableFuture<ModelResponse> result : results) {
            ModelResponse response = result.get();
            assertNotNull(response);
            assertNotNull(response.getContent());
        }
    }

    // Helper methods
    private AgentModelContext createTestAgentContext(String agentId) {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("reasoning", true);
        capabilities.put("analysis", true);

        Map<String, Object> constraints = new HashMap<>();
        constraints.put("maxTokens", 1000);
        constraints.put("timeout", 30);

        Map<String, String> promptTemplates = new HashMap<>();
        promptTemplates.put("default", "You are {{agentId}} with reasoning capabilities.");

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("temperature", 0.7);
        preferences.put("maxTokens", 1000);

        return AgentModelContext.builder().withAgentId(agentId).specialization("general-reasoning")
                .domain("system-analysis").capabilities(capabilities).constraints(constraints)
                .promptTemplates(promptTemplates).preferences(preferences).createdAt(Instant.now())
                .lastUpdated(Instant.now()).build();
    }

    private AgentModelContext createSpecializedAgentContext(String agentId) {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("reasoning", true);
        capabilities.put("optimization", true);
        capabilities.put("performance_analysis", true);

        Map<String, Object> constraints = new HashMap<>();
        constraints.put("maxTokens", 1500);
        constraints.put("timeout", 45);

        Map<String, String> promptTemplates = new HashMap<>();
        promptTemplates.put("default", "You are {{agentId}}, a performance optimization specialist.");
        promptTemplates.put("analysis", "Analyze the performance data: {{data}}");

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("temperature", 0.4);
        preferences.put("maxTokens", 1500);

        return AgentModelContext.builder().withAgentId(agentId).specialization("performance-optimization")
                .domain("system-performance").capabilities(capabilities).constraints(constraints)
                .promptTemplates(promptTemplates).preferences(preferences).createdAt(Instant.now())
                .lastUpdated(Instant.now()).build();
    }
}
