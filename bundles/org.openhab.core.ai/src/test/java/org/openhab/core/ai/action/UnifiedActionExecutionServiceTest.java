package org.openhab.core.ai.action;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.api.agent.AgentActionDelegationService;
import org.openhab.core.ai.api.model.ModelProviderType;

/**
 * Unit tests for UnifiedActionExecutionService
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class UnifiedActionExecutionServiceTest {

    private UnifiedActionExecutionService unifiedActionExecutionService;

    @Mock
    private AgentActionDelegationService agentDelegationService;

    @Mock
    private ActionRegistry actionRegistry;

    @Mock
    private ActionSecurityValidator securityValidator;

    @BeforeEach
    void setUp() {
        unifiedActionExecutionService = new UnifiedActionExecutionService();

        // Use reflection to inject mocks
        try {
            java.lang.reflect.Field delegationField = UnifiedActionExecutionService.class
                    .getDeclaredField("agentDelegationService");
            delegationField.setAccessible(true);
            delegationField.set(unifiedActionExecutionService, agentDelegationService);

            java.lang.reflect.Field registryField = UnifiedActionExecutionService.class
                    .getDeclaredField("actionRegistry");
            registryField.setAccessible(true);
            registryField.set(unifiedActionExecutionService, actionRegistry);

            java.lang.reflect.Field validatorField = UnifiedActionExecutionService.class
                    .getDeclaredField("securityValidator");
            validatorField.setAccessible(true);
            validatorField.set(unifiedActionExecutionService, securityValidator);
        } catch (Exception e) {
            fail("Failed to inject mocks: " + e.getMessage());
        }

        // Reset metrics for clean test isolation
        unifiedActionExecutionService.resetMetrics();
    }

    @Test
    void testExecuteActionSuccess() {
        // Given
        ActionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));
        ActionResult expectedResult = ActionResult.success("Success", 100);

        when(securityValidator.validateAction(actionContext)).thenReturn(true);
        when(agentDelegationService.delegateAction(actionContext))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        CompletableFuture<ActionResult> future = unifiedActionExecutionService.executeAction(actionContext,
                ModelProviderType.OPENAI);

        // Then
        ActionResult result = future.join();
        assertTrue(result.isSuccess());
        assertEquals("Success", result.getData());
        assertEquals(100, result.getExecutionTimeMs());

        verify(securityValidator).validateAction(actionContext);
        verify(agentDelegationService).delegateAction(actionContext);
    }

    @Test
    void testExecuteActionSecurityValidationFailure() {
        // Given
        ActionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));

        when(securityValidator.validateAction(actionContext)).thenReturn(false);

        // When
        CompletableFuture<ActionResult> future = unifiedActionExecutionService.executeAction(actionContext,
                ModelProviderType.OPENAI);

        // Then
        ActionResult result = future.join();
        assertFalse(result.isSuccess());
        assertEquals("Action execution blocked by security validation", result.getMessage());
        assertNotNull(result.getError());
        assertEquals("SECURITY_VIOLATION", result.getError().getErrorCode());

        verify(securityValidator).validateAction(actionContext);
        verify(agentDelegationService, never()).delegateAction(any());
    }

    @Test
    void testExecuteActionWithCaching() {
        // Given
        ActionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));
        ActionResult expectedResult = ActionResult.success("Success", 100);

        when(securityValidator.validateAction(actionContext)).thenReturn(true);
        when(agentDelegationService.delegateAction(actionContext))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // When - Execute action twice
        CompletableFuture<ActionResult> future1 = unifiedActionExecutionService.executeAction(actionContext,
                ModelProviderType.OPENAI);
        CompletableFuture<ActionResult> future2 = unifiedActionExecutionService.executeAction(actionContext,
                ModelProviderType.OPENAI);

        // Then
        ActionResult result1 = future1.join();
        ActionResult result2 = future2.join();

        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());

        // Second call should use cache
        verify(agentDelegationService, times(1)).delegateAction(actionContext);
    }

    @Test
    void testExecuteActionWithRetry() {
        // Given
        ActionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));
        ActionResult expectedResult = ActionResult.success("Success", 100);

        when(securityValidator.validateAction(actionContext)).thenReturn(true);
        when(agentDelegationService.delegateAction(actionContext))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("First attempt failed")))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        CompletableFuture<ActionResult> future = unifiedActionExecutionService.executeAction(actionContext,
                ModelProviderType.OPENAI);

        // Then
        ActionResult result = future.join();
        assertTrue(result.isSuccess());

        // Should have been called twice due to retry
        verify(agentDelegationService, times(2)).delegateAction(actionContext);
    }

    @Test
    void testExecuteActionMaxRetriesExceeded() {
        // Given
        ActionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));

        when(securityValidator.validateAction(actionContext)).thenReturn(true);
        when(agentDelegationService.delegateAction(actionContext))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Always fails")));

        // When
        CompletableFuture<ActionResult> future = unifiedActionExecutionService.executeAction(actionContext,
                ModelProviderType.OPENAI);

        // Then
        ActionResult result = future.join();
        assertFalse(result.isSuccess());
        assertEquals("Action execution failed", result.getMessage());
        assertNotNull(result.getError());
        assertEquals("EXECUTION_ERROR", result.getError().getErrorCode());

        // Should have been called multiple times due to retries
        verify(agentDelegationService, atLeast(3)).delegateAction(actionContext);
    }

    @Test
    void testExecuteActionsParallel() {
        // Given
        ActionContext context1 = createTestActionContext("test.action1", Map.of("param1", "value1"));
        ActionContext context2 = createTestActionContext("test.action2", Map.of("param2", "value2"));

        ActionResult result1 = ActionResult.success("Success1", 100);
        ActionResult result2 = ActionResult.success("Success2", 200);

        when(securityValidator.validateAction(context1)).thenReturn(true);
        when(securityValidator.validateAction(context2)).thenReturn(true);
        when(agentDelegationService.delegateAction(context1)).thenReturn(CompletableFuture.completedFuture(result1));
        when(agentDelegationService.delegateAction(context2)).thenReturn(CompletableFuture.completedFuture(result2));

        // When
        CompletableFuture<List<ActionResult>> future = unifiedActionExecutionService
                .executeActions(List.of(context1, context2), ModelProviderType.OPENAI);

        // Then
        List<ActionResult> results = future.join();
        assertEquals(2, results.size());
        assertTrue(results.get(0).isSuccess());
        assertTrue(results.get(1).isSuccess());
        assertEquals("Success1", results.get(0).getData());
        assertEquals("Success2", results.get(1).getData());
    }

    @Test
    void testExecuteActionDifferentProviderTypes() {
        // Given
        ActionContext actionContext1 = createTestActionContext("test.action1", Map.of("param", "value1"));
        ActionContext actionContext2 = createTestActionContext("test.action2", Map.of("param", "value2"));
        ActionContext actionContext3 = createTestActionContext("test.action3", Map.of("param", "value3"));
        ActionResult expectedResult = ActionResult.success("Success", 100);

        when(securityValidator.validateAction(any())).thenReturn(true);
        when(agentDelegationService.delegateAction(any()))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // When - Test different provider types with different action contexts to avoid caching
        CompletableFuture<ActionResult> future1 = unifiedActionExecutionService.executeAction(actionContext1,
                ModelProviderType.OLLAMA);
        CompletableFuture<ActionResult> future2 = unifiedActionExecutionService.executeAction(actionContext2,
                ModelProviderType.OPENAI);
        CompletableFuture<ActionResult> future3 = unifiedActionExecutionService.executeAction(actionContext3,
                ModelProviderType.ANTHROPIC);

        // Then
        assertTrue(future1.join().isSuccess());
        assertTrue(future2.join().isSuccess());
        assertTrue(future3.join().isSuccess());

        // All should delegate to agent delegation service
        verify(agentDelegationService, times(3)).delegateAction(any());
    }

    @Test
    void testExecuteActionUnsupportedProviderType() {
        // Given
        ActionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));

        when(securityValidator.validateAction(actionContext)).thenReturn(true);

        // When & Then - Test with LMSTUDIO which should now be supported
        // This test verifies that LMSTUDIO is properly handled
        assertDoesNotThrow(() -> {
            unifiedActionExecutionService.executeAction(actionContext, ModelProviderType.LMSTUDIO);
        });

        // Verify that the action was delegated
        verify(agentDelegationService, times(1)).delegateAction(actionContext);
    }

    @Test
    void testClearCache() {
        // Given
        ActionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));
        ActionResult expectedResult = ActionResult.success("Success", 100);

        when(securityValidator.validateAction(actionContext)).thenReturn(true);
        when(agentDelegationService.delegateAction(actionContext))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // Execute action to populate cache
        unifiedActionExecutionService.executeAction(actionContext, ModelProviderType.OPENAI).join();

        // Clear cache
        unifiedActionExecutionService.clearCache();

        // Execute action again
        unifiedActionExecutionService.executeAction(actionContext, ModelProviderType.OPENAI).join();

        // Then - Should be called twice because cache was cleared
        verify(agentDelegationService, times(2)).delegateAction(actionContext);
    }

    @Test
    void testGetPerformanceMetrics() {
        // Given - Create a fresh service instance for this test
        UnifiedActionExecutionService freshService = new UnifiedActionExecutionService();

        // Inject mocks into the fresh service
        try {
            java.lang.reflect.Field delegationField = UnifiedActionExecutionService.class
                    .getDeclaredField("agentDelegationService");
            delegationField.setAccessible(true);
            delegationField.set(freshService, agentDelegationService);

            java.lang.reflect.Field registryField = UnifiedActionExecutionService.class
                    .getDeclaredField("actionRegistry");
            registryField.setAccessible(true);
            registryField.set(freshService, actionRegistry);

            java.lang.reflect.Field validatorField = UnifiedActionExecutionService.class
                    .getDeclaredField("securityValidator");
            validatorField.setAccessible(true);
            validatorField.set(freshService, securityValidator);
        } catch (Exception e) {
            fail("Failed to inject mocks: " + e.getMessage());
        }

        ActionContext actionContext1 = createTestActionContext("test.action1", Map.of("param", "value1"));
        ActionContext actionContext2 = createTestActionContext("test.action2", Map.of("param", "value2"));
        ActionResult expectedResult = ActionResult.success("Success", 100);

        when(securityValidator.validateAction(any())).thenReturn(true);
        when(agentDelegationService.delegateAction(any()))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // Execute some actions with different contexts to avoid caching
        freshService.executeAction(actionContext1, ModelProviderType.OPENAI).join();
        freshService.executeAction(actionContext2, ModelProviderType.OPENAI).join();

        // When
        UnifiedActionExecutionService.PerformanceMetrics metrics = freshService.getPerformanceMetrics();

        // Then
        assertEquals(2, metrics.getTotalExecutions());
        assertEquals(2, metrics.getSuccessfulExecutions());
        assertEquals(0, metrics.getFailedExecutions());

        // Use more lenient assertions
        assertTrue(metrics.getSuccessRate() >= 0.9,
                "Success rate should be >= 0.9, but was " + metrics.getSuccessRate());
        assertTrue(metrics.getAverageExecutionTime() >= 0,
                "Average execution time should be >= 0, but was " + metrics.getAverageExecutionTime());
    }

    @Test
    void testUpdateConfiguration() {
        // Given
        UnifiedActionExecutionService.Configuration config = UnifiedActionExecutionService.Configuration.builder()
                .maxRetryAttempts(5).retryDelay(Duration.ofSeconds(2)).enableCaching(false)
                .enableSecurityValidation(false).cacheExpiration(Duration.ofMinutes(10));

        // When
        unifiedActionExecutionService.updateConfiguration(config);

        // Then - Configuration should be updated (we can't easily verify the internal state,
        // but we can verify the method doesn't throw an exception)
        assertDoesNotThrow(() -> {
            unifiedActionExecutionService.updateConfiguration(config);
        });
    }

    @Test
    void testExecuteActionWithNullActionName() {
        // Given
        ActionContext actionContext = createTestActionContext(null, Map.of("param", "value"));

        // When
        CompletableFuture<ActionResult> future = unifiedActionExecutionService.executeAction(actionContext,
                ModelProviderType.OPENAI);

        // Then
        ActionResult result = future.join();
        assertFalse(result.isSuccess());
        assertEquals("Action execution failed", result.getMessage());

        verify(agentDelegationService, never()).delegateAction(any());
    }

    @Test
    void testExecuteActionWithEmptyActionName() {
        // Given
        ActionContext actionContext = createTestActionContext("", Map.of("param", "value"));

        // When
        CompletableFuture<ActionResult> future = unifiedActionExecutionService.executeAction(actionContext,
                ModelProviderType.OPENAI);

        // Then
        ActionResult result = future.join();
        assertFalse(result.isSuccess());
        assertEquals("Action execution failed", result.getMessage());

        verify(agentDelegationService, never()).delegateAction(any());
    }

    @Test
    void testResetMetrics() {
        // Given
        ActionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));
        ActionResult expectedResult = ActionResult.success("Success", 100);

        when(securityValidator.validateAction(actionContext)).thenReturn(true);
        when(agentDelegationService.delegateAction(actionContext))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // Execute an action to populate metrics
        unifiedActionExecutionService.executeAction(actionContext, ModelProviderType.OPENAI).join();

        // Verify metrics are populated
        UnifiedActionExecutionService.PerformanceMetrics metricsBefore = unifiedActionExecutionService
                .getPerformanceMetrics();
        assertEquals(1, metricsBefore.getTotalExecutions());

        // When - Reset metrics
        unifiedActionExecutionService.resetMetrics();

        // Then - Metrics should be reset
        UnifiedActionExecutionService.PerformanceMetrics metricsAfter = unifiedActionExecutionService
                .getPerformanceMetrics();
        assertEquals(0, metricsAfter.getTotalExecutions());
        assertEquals(0, metricsAfter.getSuccessfulExecutions());
        assertEquals(0, metricsAfter.getFailedExecutions());
        assertEquals(0, metricsAfter.getTotalExecutionTime());
        assertEquals(0, metricsAfter.getTotalRetryAttempts());
    }

    private ActionContext createTestActionContext(String actionName, Map<String, Object> arguments) {
        Map<String, Object> protocolContext;
        if (actionName == null) {
            protocolContext = Map.of("arguments", arguments);
        } else {
            protocolContext = Map.of("action", actionName, "arguments", arguments);
        }

        return ActionContext.builder().protocol("a2a").clientId("test-client").sessionId("test-session")
                .correlationId("test-correlation-" + System.currentTimeMillis()).protocolContext(protocolContext)
                .build();
    }
}
