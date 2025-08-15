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
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.ModelResponse;
import org.openhab.core.ai.model.api.ModelConfigurationService;
import org.openhab.core.ai.reasoning.SharedModelReasoningEngine;

/**
 * Integration tests for decision-making with various scenarios
 * Tests the decision-making capabilities of the agent-model integration
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@ExtendWith(MockitoExtension.class)
class DecisionMakingIntegrationTest {

    @Mock
    private ModelConfigurationService modelConfigurationService;

    @Mock
    private AgentModelProvider mockModelProvider;

    private SharedModelReasoningEngine reasoningEngine;

    @BeforeEach
    void setUp() {
        reasoningEngine = new SharedModelReasoningEngine();

        // Configure mock model provider for decision-making responses
        when(mockModelProvider.reasonAsync(anyString(), anyMap(), any(ModelParameters.class)))
                .thenReturn(CompletableFuture.completedFuture(
                        ModelResponse.builder().content("Decision: Proceed with optimization").build()));
    }

    @Test
    void testBasicDecisionMaking() throws Exception {
        // Given
        String agentId = "decision-agent";
        AgentModelContext context = createDecisionAgentContext(agentId);
        reasoningEngine.registerAgent(agentId, context);

        // When - Make a basic decision
        String prompt = "Should we proceed with the system update?";
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("systemStability", "stable");
        contextData.put("updateRisk", "low");
        contextData.put("userApproval", true);

        ModelParameters parameters = ModelParameters.builder().temperature(0.3).maxTokens(200).build();

        CompletableFuture<ModelResponse> result = reasoningEngine.reasonAsync(agentId, prompt, contextData, parameters);

        // Then - Verify decision result
        ModelResponse response = result.get(5, TimeUnit.SECONDS);
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().toLowerCase().contains("decision"));
    }

    @Test
    void testComplexDecisionMakingWithMultipleFactors() throws Exception {
        // Given
        String agentId = "complex-decision-agent";
        AgentModelContext context = createDecisionAgentContext(agentId);
        reasoningEngine.registerAgent(agentId, context);

        // When - Make a complex decision with multiple factors
        String prompt = "Should we implement the new caching strategy?";
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("currentPerformance", 85.5);
        contextData.put("expectedImprovement", 15.2);
        contextData.put("implementationCost", 5000);
        contextData.put("maintenanceOverhead", "medium");
        contextData.put("compatibilityRisk", "low");
        contextData.put("userImpact", "minimal");
        contextData.put("timeline", "2_weeks");

        ModelParameters parameters = ModelParameters.builder().temperature(0.4).maxTokens(500).build();

        CompletableFuture<ModelResponse> result = reasoningEngine.reasonAsync(agentId, prompt, contextData, parameters);

        // Then - Verify decision result
        ModelResponse response = result.get(5, TimeUnit.SECONDS);
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertFalse(response.getContent().isEmpty());
    }

    @Test
    void testRiskAssessmentDecision() throws Exception {
        // Given
        String agentId = "risk-assessment-agent";
        AgentModelContext context = createRiskAssessmentContext(agentId);
        reasoningEngine.registerAgent(agentId, context);

        // When - Assess risk and make decision
        String prompt = "Assess the risk of deploying this change and recommend action";
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("changeType", "database_schema");
        contextData.put("affectedSystems", 3);
        contextData.put("rollbackPlan", "available");
        contextData.put("testingCoverage", 85.0);
        contextData.put("businessCriticality", "high");
        contextData.put("downtimeTolerance", "low");

        ModelParameters parameters = ModelParameters.builder().temperature(0.2).maxTokens(400).build();

        CompletableFuture<ModelResponse> result = reasoningEngine.reasonAsync(agentId, prompt, contextData, parameters);

        // Then - Verify risk assessment result
        ModelResponse response = result.get(5, TimeUnit.SECONDS);
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().toLowerCase().contains("risk")
                || response.getContent().toLowerCase().contains("recommend"));
    }

    @Test
    void testPriorityBasedDecisionMaking() throws Exception {
        // Given
        String agentId = "priority-agent";
        AgentModelContext context = createPriorityAgentContext(agentId);
        reasoningEngine.registerAgent(agentId, context);

        // When - Make priority-based decisions
        String prompt = "Prioritize these tasks based on business impact and urgency";
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("task1", Map.of("name", "Security Patch", "impact", "critical", "urgency", "high"));
        contextData.put("task2", Map.of("name", "Performance Optimization", "impact", "medium", "urgency", "medium"));
        contextData.put("task3", Map.of("name", "UI Enhancement", "impact", "low", "urgency", "low"));
        contextData.put("availableResources", 2);
        contextData.put("timeConstraint", "1_week");

        ModelParameters parameters = ModelParameters.builder().temperature(0.3).maxTokens(600).build();

        CompletableFuture<ModelResponse> result = reasoningEngine.reasonAsync(agentId, prompt, contextData, parameters);

        // Then - Verify prioritization result
        ModelResponse response = result.get(5, TimeUnit.SECONDS);
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertFalse(response.getContent().isEmpty());
    }

    @Test
    void testResourceAllocationDecision() throws Exception {
        // Given
        String agentId = "resource-agent";
        AgentModelContext context = createResourceAgentContext(agentId);
        reasoningEngine.registerAgent(agentId, context);

        // When - Make resource allocation decision
        String prompt = "How should we allocate resources for the upcoming projects?";
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("totalBudget", 100000);
        contextData.put("availableDevelopers", 5);
        contextData.put("project1",
                Map.of("name", "Core System", "budget", 40000, "developers", 2, "priority", "high"));
        contextData.put("project2",
                Map.of("name", "New Features", "budget", 35000, "developers", 2, "priority", "medium"));
        contextData.put("project3",
                Map.of("name", "Technical Debt", "budget", 25000, "developers", 1, "priority", "low"));

        ModelParameters parameters = ModelParameters.builder().temperature(0.4).maxTokens(800).build();

        CompletableFuture<ModelResponse> result = reasoningEngine.reasonAsync(agentId, prompt, contextData, parameters);

        // Then - Verify resource allocation result
        ModelResponse response = result.get(5, TimeUnit.SECONDS);
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertFalse(response.getContent().isEmpty());
    }

    @Test
    void testConflictResolutionDecision() throws Exception {
        // Given
        String agentId = "conflict-resolution-agent";
        AgentModelContext context = createConflictResolutionContext(agentId);
        reasoningEngine.registerAgent(agentId, context);

        // When - Resolve conflicts and make decision
        String prompt = "Resolve the conflict between security requirements and user experience";
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("securityRequirement", "mandatory_2fa");
        contextData.put("userExperience", "seamless_login");
        contextData.put("userBase", 10000);
        contextData.put("securityCompliance", "required");
        contextData.put("userAdoption", "critical");

        ModelParameters parameters = ModelParameters.builder().temperature(0.5).maxTokens(700).build();

        CompletableFuture<ModelResponse> result = reasoningEngine.reasonAsync(agentId, prompt, contextData, parameters);

        // Then - Verify conflict resolution result
        ModelResponse response = result.get(5, TimeUnit.SECONDS);
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().toLowerCase().contains("compromise")
                || response.getContent().toLowerCase().contains("balance")
                || response.getContent().toLowerCase().contains("solution"));
    }

    @Test
    void testStrategicDecisionMaking() throws Exception {
        // Given
        String agentId = "strategic-agent";
        AgentModelContext context = createStrategicAgentContext(agentId);
        reasoningEngine.registerAgent(agentId, context);

        // When - Make strategic decision
        String prompt = "What should be our technology strategy for the next 2 years?";
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("currentTechStack", "legacy_system");
        contextData.put("marketTrends", "cloud_native");
        contextData.put("competitorAnalysis", "modern_stack");
        contextData.put("budget", 500000);
        contextData.put("teamExpertise", "mixed");
        contextData.put("businessGoals", "scale_10x");

        ModelParameters parameters = ModelParameters.builder().temperature(0.6).maxTokens(1000).build();

        CompletableFuture<ModelResponse> result = reasoningEngine.reasonAsync(agentId, prompt, contextData, parameters);

        // Then - Verify strategic decision result
        ModelResponse response = result.get(5, TimeUnit.SECONDS);
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertFalse(response.getContent().isEmpty());
    }

    @Test
    void testDecisionMakingWithUncertainty() throws Exception {
        // Given
        String agentId = "uncertainty-agent";
        AgentModelContext context = createUncertaintyAgentContext(agentId);
        reasoningEngine.registerAgent(agentId, context);

        // When - Make decision with uncertain data
        String prompt = "Make a decision despite incomplete information";
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("knownFactors", Map.of("budget", 50000, "timeline", "3_months"));
        contextData.put("unknownFactors", Map.of("market_reaction", "unknown", "competitor_response", "unknown"));
        contextData.put("confidenceLevel", 0.6);
        contextData.put("fallbackPlan", "available");

        ModelParameters parameters = ModelParameters.builder().temperature(0.7).maxTokens(500).build();

        CompletableFuture<ModelResponse> result = reasoningEngine.reasonAsync(agentId, prompt, contextData, parameters);

        // Then - Verify decision with uncertainty result
        ModelResponse response = result.get(5, TimeUnit.SECONDS);
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().toLowerCase().contains("uncertain")
                || response.getContent().toLowerCase().contains("confidence")
                || response.getContent().toLowerCase().contains("risk"));
    }

    @Test
    void testMultiCriteriaDecisionMaking() throws Exception {
        // Given
        String agentId = "multi-criteria-agent";
        AgentModelContext context = createMultiCriteriaAgentContext(agentId);
        reasoningEngine.registerAgent(agentId, context);

        // When - Make decision based on multiple criteria
        String prompt = "Evaluate and select the best database solution";
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("option1",
                Map.of("name", "PostgreSQL", "cost", 10000, "performance", 85, "scalability", 90, "support", 95));
        contextData.put("option2",
                Map.of("name", "MongoDB", "cost", 15000, "performance", 90, "scalability", 95, "support", 80));
        contextData.put("option3",
                Map.of("name", "MySQL", "cost", 5000, "performance", 75, "scalability", 70, "support", 90));
        contextData.put("weights", Map.of("cost", 0.3, "performance", 0.3, "scalability", 0.25, "support", 0.15));

        ModelParameters parameters = ModelParameters.builder().temperature(0.3).maxTokens(600).build();

        CompletableFuture<ModelResponse> result = reasoningEngine.reasonAsync(agentId, prompt, contextData, parameters);

        // Then - Verify multi-criteria decision result
        ModelResponse response = result.get(5, TimeUnit.SECONDS);
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertFalse(response.getContent().isEmpty());
    }

    // Helper methods for creating specialized agent contexts
    private AgentModelContext createDecisionAgentContext(String agentId) {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("decision_making", true);
        capabilities.put("analysis", true);
        capabilities.put("evaluation", true);

        Map<String, Object> constraints = new HashMap<>();
        constraints.put("maxTokens", 800);
        constraints.put("timeout", 60);

        Map<String, String> promptTemplates = new HashMap<>();
        promptTemplates.put("default", "You are {{agentId}}, a decision-making specialist.");
        promptTemplates.put("decision", "Evaluate the options and make a decision: {{options}}");

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("temperature", 0.4);
        preferences.put("maxTokens", 800);

        return AgentModelContext.builder().agentId(agentId).specialization("decision-making")
                .domain("business-strategy").capabilities(capabilities).constraints(constraints)
                .promptTemplates(promptTemplates).preferences(preferences).createdAt(Instant.now())
                .lastUpdated(Instant.now()).build();
    }

    private AgentModelContext createRiskAssessmentContext(String agentId) {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("risk_assessment", true);
        capabilities.put("decision_making", true);
        capabilities.put("safety_analysis", true);

        Map<String, Object> constraints = new HashMap<>();
        constraints.put("maxTokens", 600);
        constraints.put("timeout", 45);

        Map<String, String> promptTemplates = new HashMap<>();
        promptTemplates.put("default", "You are {{agentId}}, a risk assessment specialist.");
        promptTemplates.put("risk", "Assess the risk level: {{scenario}}");

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("temperature", 0.3);
        preferences.put("maxTokens", 600);

        return AgentModelContext.builder().agentId(agentId).specialization("risk-assessment")
                .domain("safety-management").capabilities(capabilities).constraints(constraints)
                .promptTemplates(promptTemplates).preferences(preferences).createdAt(Instant.now())
                .lastUpdated(Instant.now()).build();
    }

    private AgentModelContext createPriorityAgentContext(String agentId) {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("prioritization", true);
        capabilities.put("resource_management", true);
        capabilities.put("planning", true);

        Map<String, Object> constraints = new HashMap<>();
        constraints.put("maxTokens", 700);
        constraints.put("timeout", 50);

        Map<String, String> promptTemplates = new HashMap<>();
        promptTemplates.put("default", "You are {{agentId}}, a prioritization specialist.");
        promptTemplates.put("priority", "Prioritize the tasks: {{tasks}}");

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("temperature", 0.3);
        preferences.put("maxTokens", 700);

        return AgentModelContext.builder().agentId(agentId).specialization("prioritization")
                .domain("project-management").capabilities(capabilities).constraints(constraints)
                .promptTemplates(promptTemplates).preferences(preferences).createdAt(Instant.now())
                .lastUpdated(Instant.now()).build();
    }

    private AgentModelContext createResourceAgentContext(String agentId) {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("resource_allocation", true);
        capabilities.put("budget_management", true);
        capabilities.put("optimization", true);

        Map<String, Object> constraints = new HashMap<>();
        constraints.put("maxTokens", 900);
        constraints.put("timeout", 60);

        Map<String, String> promptTemplates = new HashMap<>();
        promptTemplates.put("default", "You are {{agentId}}, a resource allocation specialist.");
        promptTemplates.put("allocation", "Allocate resources for: {{projects}}");

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("temperature", 0.4);
        preferences.put("maxTokens", 900);

        return AgentModelContext.builder().agentId(agentId).specialization("resource-allocation")
                .domain("resource-management").capabilities(capabilities).constraints(constraints)
                .promptTemplates(promptTemplates).preferences(preferences).createdAt(Instant.now())
                .lastUpdated(Instant.now()).build();
    }

    private AgentModelContext createConflictResolutionContext(String agentId) {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("conflict_resolution", true);
        capabilities.put("negotiation", true);
        capabilities.put("compromise", true);

        Map<String, Object> constraints = new HashMap<>();
        constraints.put("maxTokens", 800);
        constraints.put("timeout", 60);

        Map<String, String> promptTemplates = new HashMap<>();
        promptTemplates.put("default", "You are {{agentId}}, a conflict resolution specialist.");
        promptTemplates.put("resolution", "Resolve the conflict: {{conflict}}");

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("temperature", 0.5);
        preferences.put("maxTokens", 800);

        return AgentModelContext.builder().agentId(agentId).specialization("conflict-resolution").domain("mediation")
                .capabilities(capabilities).constraints(constraints).promptTemplates(promptTemplates)
                .preferences(preferences).createdAt(Instant.now()).lastUpdated(Instant.now()).build();
    }

    private AgentModelContext createStrategicAgentContext(String agentId) {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("strategic_planning", true);
        capabilities.put("market_analysis", true);
        capabilities.put("long_term_vision", true);

        Map<String, Object> constraints = new HashMap<>();
        constraints.put("maxTokens", 1200);
        constraints.put("timeout", 90);

        Map<String, String> promptTemplates = new HashMap<>();
        promptTemplates.put("default", "You are {{agentId}}, a strategic planning specialist.");
        promptTemplates.put("strategy", "Develop strategy for: {{context}}");

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("temperature", 0.6);
        preferences.put("maxTokens", 1200);

        return AgentModelContext.builder().agentId(agentId).specialization("strategic-planning")
                .domain("business-strategy").capabilities(capabilities).constraints(constraints)
                .promptTemplates(promptTemplates).preferences(preferences).createdAt(Instant.now())
                .lastUpdated(Instant.now()).build();
    }

    private AgentModelContext createUncertaintyAgentContext(String agentId) {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("uncertainty_handling", true);
        capabilities.put("probabilistic_reasoning", true);
        capabilities.put("risk_tolerance", true);

        Map<String, Object> constraints = new HashMap<>();
        constraints.put("maxTokens", 600);
        constraints.put("timeout", 45);

        Map<String, String> promptTemplates = new HashMap<>();
        promptTemplates.put("default", "You are {{agentId}}, an uncertainty handling specialist.");
        promptTemplates.put("uncertainty", "Handle uncertainty in: {{scenario}}");

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("temperature", 0.7);
        preferences.put("maxTokens", 600);

        return AgentModelContext.builder().agentId(agentId).specialization("uncertainty-handling")
                .domain("risk-management").capabilities(capabilities).constraints(constraints)
                .promptTemplates(promptTemplates).preferences(preferences).createdAt(Instant.now())
                .lastUpdated(Instant.now()).build();
    }

    private AgentModelContext createMultiCriteriaAgentContext(String agentId) {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("multi_criteria_analysis", true);
        capabilities.put("evaluation", true);
        capabilities.put("comparison", true);

        Map<String, Object> constraints = new HashMap<>();
        constraints.put("maxTokens", 700);
        constraints.put("timeout", 50);

        Map<String, String> promptTemplates = new HashMap<>();
        promptTemplates.put("default", "You are {{agentId}}, a multi-criteria analysis specialist.");
        promptTemplates.put("evaluation", "Evaluate options: {{options}}");

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("temperature", 0.3);
        preferences.put("maxTokens", 700);

        return AgentModelContext.builder().agentId(agentId).specialization("multi-criteria-analysis")
                .domain("decision-analysis").capabilities(capabilities).constraints(constraints)
                .promptTemplates(promptTemplates).preferences(preferences).createdAt(Instant.now())
                .lastUpdated(Instant.now()).build();
    }
}
