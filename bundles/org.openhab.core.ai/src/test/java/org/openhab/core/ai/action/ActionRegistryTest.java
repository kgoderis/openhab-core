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
package org.openhab.core.ai.action;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.action.api.Action;
import org.osgi.framework.BundleContext;

/**
 * Test class for the enhanced ActionRegistry.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class ActionRegistryTest {

    @Mock
    private BundleContext bundleContext;

    @Mock
    private Action mockAction;

    private ActionRegistry actionRegistry;

    @BeforeEach
    void setUp() {
        actionRegistry = new ActionRegistry();
        actionRegistry.activate(bundleContext);

        // Configure mock action behavior with all required methods using lenient stubbing
        lenient().when(mockAction.getActionId()).thenReturn("test-action");
        lenient().when(mockAction.getActionName()).thenReturn("Test Action");
        lenient().when(mockAction.getDescription()).thenReturn("A test action for unit testing");
        lenient().when(mockAction.getCategory()).thenReturn("test");
        lenient().when(mockAction.getVersion()).thenReturn("1.0.0");
        lenient().when(mockAction.getParameterSchema()).thenReturn(Map.of());
        lenient().when(mockAction.getReturnSchema()).thenReturn(Map.of());
        lenient().when(mockAction.getCapabilities()).thenReturn(Map.of());
        lenient().when(mockAction.isReady()).thenReturn(true);
    }

    @Test
    void testAgentSpecificActionRegistration() {
        // Given
        String actionId = "test-action";
        String agentId = "test-agent";

        // When
        boolean result = actionRegistry.registerActionForAgent(actionId, agentId);

        // Then
        assertFalse(result, "Should return false for unregistered action");

        // Given - register the action first
        actionRegistry.registerAction(mockAction);

        // When
        result = actionRegistry.registerActionForAgent(actionId, agentId);

        // Then
        assertTrue(result, "Should return true for registered action");
        Map<String, Action> agentActions = actionRegistry.getActionsForAgent(agentId);
        assertTrue(agentActions.containsKey(actionId), "Agent should have the action registered");
        Set<String> actionAgents = actionRegistry.getAgentsForAction(actionId);
        assertTrue(actionAgents.contains(agentId), "Action should be registered for the agent");
    }

    @Test
    void testActionSkillMapping() {
        // Given
        String actionId = "test-action";
        String skillId = "test-skill";
        actionRegistry.registerAction(mockAction);

        // When
        boolean result = actionRegistry.mapActionToSkill(actionId, skillId);

        // Then
        assertTrue(result, "Should successfully map action to skill");
        String mappedSkillId = actionRegistry.getSkillForAction(actionId);
        assertEquals(skillId, mappedSkillId, "Should return the correct skill ID");
        List<String> skillActions = actionRegistry.getActionsForSkill(skillId);
        assertTrue(skillActions.contains(actionId), "Skill should have the action mapped");
    }

    @Test
    void testPerformanceMonitoring() {
        // Given
        String actionId = "test-action";
        String agentId = "test-agent";
        long executionTimeMs = 100;
        boolean success = true;
        String error = null;

        // When
        actionRegistry.recordExecution(actionId, agentId, executionTimeMs, success, error);

        // Then
        ActionPerformanceMetrics overallMetrics = actionRegistry.getOverallPerformanceMetrics();
        assertEquals(1, overallMetrics.getTotalExecutions(), "Should record total executions");
        assertEquals(1, overallMetrics.getSuccessfulExecutions(), "Should record successful executions");
        assertEquals(0, overallMetrics.getFailedExecutions(), "Should not record failed executions");
        assertEquals(100, overallMetrics.getTotalExecutionTimeMs(), "Should record execution time");
    }

    @Test
    void testSecurityValidation() {
        // Given
        String actionId = "test-action";
        String agentId = "test-agent";
        String origin = "test-origin";

        // When - no security policy set
        boolean result = actionRegistry.validateExecution(actionId, agentId, origin);

        // Then
        assertTrue(result, "Should allow execution when no security policy is set");

        // Given - set a restrictive security policy
        ActionSecurityPolicy policy = ActionSecurityPolicy.builder().actionId(actionId)
                .allowedAgents(Set.of("other-agent")).allowedOrigins(Set.of("other-origin")).build();
        actionRegistry.setSecurityPolicy(actionId, policy);

        // When
        result = actionRegistry.validateExecution(actionId, agentId, origin);

        // Then
        assertFalse(result, "Should deny execution when agent and origin are not allowed");

        // Given - set a permissive security policy
        ActionSecurityPolicy permissivePolicy = ActionSecurityPolicy.builder().actionId(actionId)
                .allowedAgents(Set.of(agentId)).allowedOrigins(Set.of(origin)).build();
        actionRegistry.setSecurityPolicy(actionId, permissivePolicy);

        // When
        result = actionRegistry.validateExecution(actionId, agentId, origin);

        // Then
        assertTrue(result, "Should allow execution when agent and origin are allowed");
    }

    @Test
    void testCaching() {
        // Given
        String actionId = "test-action";
        Map<String, Object> parameters = Map.of("param1", "value1");
        Object result = "test-result";

        // When
        actionRegistry.cacheResult(actionId, parameters, result);

        // Then
        Object cachedResult = actionRegistry.getCachedResult(actionId, parameters);
        assertEquals(result, cachedResult, "Should return cached result");

        // When - different parameters
        Map<String, Object> differentParameters = Map.of("param1", "value2");
        Object differentCachedResult = actionRegistry.getCachedResult(actionId, differentParameters);

        // Then
        assertNull(differentCachedResult, "Should not return cached result for different parameters");
    }

    @Test
    void testVersioning() {
        // Given
        String actionId = "test-action";
        ActionVersionInfo versionInfo = ActionVersionInfo.builder().actionId(actionId).version("1.0.0")
                .compatibleVersions(Set.of("1.0.0", "1.1.0")).deprecated(false).build();

        // When
        actionRegistry.setVersionInfo(actionId, versionInfo);

        // Then
        ActionVersionInfo retrievedVersionInfo = actionRegistry.getVersionInfo(actionId);
        assertNotNull(retrievedVersionInfo, "Should return version info");
        assertEquals("1.0.0", retrievedVersionInfo.getVersion(), "Should return correct version");
        assertFalse(retrievedVersionInfo.isDeprecated(), "Should not be deprecated");
        assertTrue(retrievedVersionInfo.isCompatibleWith("1.1.0"), "Should be compatible with 1.1.0");
    }

    @Test
    void testAnalytics() {
        // Given
        String actionId = "test-action";
        ActionAnalytics analytics = ActionAnalytics.builder().actionId(actionId).totalExecutions(100)
                .successfulExecutions(90).failedExecutions(10).averageExecutionTime(Duration.ofMillis(50)).build();

        // When
        // Note: Analytics are typically set by the system, not directly by tests
        // This test verifies the analytics structure

        // Then
        assertEquals(100, analytics.getTotalExecutions(), "Should have correct total executions");
        assertEquals(90, analytics.getSuccessfulExecutions(), "Should have correct successful executions");
        assertEquals(10, analytics.getFailedExecutions(), "Should have correct failed executions");
        assertEquals(0.9, analytics.getSuccessRate(), 0.01, "Should have correct success rate");
        assertEquals(0.1, analytics.getFailureRate(), 0.01, "Should have correct failure rate");
        assertEquals(Duration.ofMillis(50), analytics.getAverageExecutionTime(),
                "Should have correct average execution time");
    }

    @Test
    void testConfigurationUpdate() {
        // Given
        boolean enableCaching = false;
        boolean enableSecurityValidation = false;
        boolean enablePerformanceMonitoring = false;
        boolean enableAnalytics = false;
        Duration cacheExpiration = Duration.ofMinutes(60);
        int maxCacheSize = 2000;
        int maxExecutionHistory = 20000;

        // When
        actionRegistry.updateConfiguration(enableCaching, enableSecurityValidation, enablePerformanceMonitoring,
                enableAnalytics, cacheExpiration, maxCacheSize, maxExecutionHistory);

        // Then
        // Test that caching is disabled
        String actionId = "test-action";
        Map<String, Object> parameters = Map.of("param1", "value1");
        Object result = "test-result";
        actionRegistry.cacheResult(actionId, parameters, result);
        Object cachedResult = actionRegistry.getCachedResult(actionId, parameters);
        assertNull(cachedResult, "Should not cache when caching is disabled");

        // Test that security validation is disabled
        boolean validationResult = actionRegistry.validateExecution(actionId, "agent", "origin");
        assertTrue(validationResult, "Should allow execution when security validation is disabled");
    }

    @Test
    void testCacheStatistics() {
        // Given
        String actionId = "test-action";
        Map<String, Object> parameters = Map.of("param1", "value1");
        Object result = "test-result";

        // When
        actionRegistry.cacheResult(actionId, parameters, result);
        actionRegistry.getCachedResult(actionId, parameters); // Should be a hit
        actionRegistry.getCachedResult(actionId, Map.of("param1", "value2")); // Should be a miss

        // Then
        Map<String, Object> stats = actionRegistry.getCacheStatistics();
        assertNotNull(stats, "Should return cache statistics");
        assertTrue((Long) stats.get("cacheHits") > 0, "Should have cache hits");
        assertTrue((Long) stats.get("cacheMisses") > 0, "Should have cache misses");
        assertTrue((Integer) stats.get("cacheSize") > 0, "Should have cache entries");
    }

    @Test
    void testExecutionHistory() {
        // Given
        String actionId = "test-action";
        String agentId = "test-agent";

        // When - record some executions
        actionRegistry.recordExecution(actionId, agentId, 100, true, null);
        actionRegistry.recordExecution(actionId, agentId, 200, false, "Test error");

        // Then
        List<ActionExecutionEvent> history = actionRegistry.getExecutionHistory(actionId, 10);
        assertNotNull(history, "Should return execution history");
        assertTrue(history.size() > 0, "Should have execution events");

        // Verify the events have correct data
        for (ActionExecutionEvent event : history) {
            assertEquals(actionId, event.getActionId(), "Event should have correct action ID");
            assertEquals(agentId, event.getAgentId(), "Event should have correct agent ID");
        }
    }

    @Test
    void testUnregisterActionForAgent() {
        // Given
        String actionId = "test-action";
        String agentId = "test-agent";
        actionRegistry.registerAction(mockAction);
        actionRegistry.registerActionForAgent(actionId, agentId);

        // When
        boolean result = actionRegistry.unregisterActionForAgent(actionId, agentId);

        // Then
        assertTrue(result, "Should successfully unregister action for agent");
        Map<String, Action> agentActions = actionRegistry.getActionsForAgent(agentId);
        assertFalse(agentActions.containsKey(actionId), "Agent should not have the action registered");
        Set<String> actionAgents = actionRegistry.getAgentsForAction(actionId);
        assertFalse(actionAgents.contains(agentId), "Action should not be registered for the agent");
    }

    @Test
    void testClearCache() {
        // Given
        String actionId1 = "test-action-1";
        String actionId2 = "test-action-2";
        Map<String, Object> parameters = Map.of("param1", "value1");
        Object result = "test-result";

        actionRegistry.cacheResult(actionId1, parameters, result);
        actionRegistry.cacheResult(actionId2, parameters, result);

        // When
        actionRegistry.clearCache(actionId1);

        // Then
        Object cachedResult1 = actionRegistry.getCachedResult(actionId1, parameters);
        Object cachedResult2 = actionRegistry.getCachedResult(actionId2, parameters);
        assertNull(cachedResult1, "Should not return cached result for cleared action");
        assertEquals(result, cachedResult2, "Should still return cached result for other action");
    }

    @Test
    void testClearAllCaches() {
        // Given
        String actionId = "test-action";
        Map<String, Object> parameters = Map.of("param1", "value1");
        Object result = "test-result";

        actionRegistry.cacheResult(actionId, parameters, result);

        // When
        actionRegistry.clearAllCaches();

        // Then
        Object cachedResult = actionRegistry.getCachedResult(actionId, parameters);
        assertNull(cachedResult, "Should not return any cached results after clearing all caches");
    }
}
