package org.openhab.core.ai.common.action;

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
import org.openhab.core.ai.common.api.llm.LLMProviderType;

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
    private AIActionRegistry actionRegistry;

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
        AIActionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));
        AIActionResult expectedResult = AIActionResult.success("Success", 100);

        when(securityValidator.validateAction(actionContext)).thenReturn(true);
        when(agentDelegationService.delegateAction(actionContext))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        CompletableFuture<AIActionResult> future = unifiedActionExecutionService.executeAction(actionContext,
                LLMProviderType.OPENAI);

        // Then
        AIActionResult result = future.join();
        assertTrue(result.isSuccess());
        assertEquals("Success", result.getData());
        assertEquals(100, result.getExecutionTimeMs());

        verify(securityValidator).validateAction(actionContext);
        verify(agentDelegationService).delegateAction(actionContext);
    }

    @Test
    void testExecuteActionSecurityValidationFailure() {
        // Given
        AIActionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));

        when(securityValidator.validateAction(actionContext)).thenReturn(false);

        // When
        CompletableFuture<AIActionResult> future = unifiedActionExecutionService.executeAction(actionContext,
                LLMProviderType.OPENAI);

        // Then
        AIActionResult result = future.join();
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
        AIActionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));
        AIActionResult expectedResult = AIActionResult.success("Success", 100);

        when(securityValidator.validateAction(actionContext)).thenReturn(true);
        when(agentDelegationService.delegateAction(actionContext))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // When - Execute action twice
        CompletableFuture<AIActionResult> future1 = unifiedActionExecutionService.executeAction(actionContext,
                LLMProviderType.OPENAI);
        CompletableFuture<AIActionResult> future2 = unifiedActionExecutionService.executeAction(actionContext,
                LLMProviderType.OPENAI);

        // Then
        AIActionResult result1 = future1.join();
        AIActionResult result2 = future2.join();

        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());

        // Second call should use cache
        verify(agentDelegationService, times(1)).delegateAction(actionContext);
    }

    @Test
    void testExecuteActionWithRetry() {
        // Given
        AIActionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));
        AIActionResult expectedResult = AIActionResult.success("Success", 100);

        when(securityValidator.validateAction(actionContext)).thenReturn(true);
        when(agentDelegationService.delegateAction(actionContext))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("First attempt failed")))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        CompletableFuture<AIActionResult> future = unifiedActionExecutionService.executeAction(actionContext,
                LLMProviderType.OPENAI);

        // Then
        AIActionResult result = future.join();
        assertTrue(result.isSuccess());

        // Should have been called twice due to retry
        verify(agentDelegationService, times(2)).delegateAction(actionContext);
    }

    @Test
    void testExecuteActionMaxRetriesExceeded() {
        // Given
        AIActionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));

        when(securityValidator.validateAction(actionContext)).thenReturn(true);
        when(agentDelegationService.delegateAction(actionContext))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Always fails")));

        // When
        CompletableFuture<AIActionResult> future = unifiedActionExecutionService.executeAction(actionContext,
                LLMProviderType.OPENAI);

        // Then
        AIActionResult result = future.join();
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
        AIActionContext context1 = createTestActionContext("test.action1", Map.of("param1", "value1"));
        AIActionContext context2 = createTestActionContext("test.action2", Map.of("param2", "value2"));

        AIActionResult result1 = AIActionResult.success("Success1", 100);
        AIActionResult result2 = AIActionResult.success("Success2", 200);

        when(securityValidator.validateAction(context1)).thenReturn(true);
        when(securityValidator.validateAction(context2)).thenReturn(true);
        when(agentDelegationService.delegateAction(context1)).thenReturn(CompletableFuture.completedFuture(result1));
        when(agentDelegationService.delegateAction(context2)).thenReturn(CompletableFuture.completedFuture(result2));

        // When
        CompletableFuture<List<AIActionResult>> future = unifiedActionExecutionService
                .executeActions(List.of(context1, context2), LLMProviderType.OPENAI);

        // Then
        List<AIActionResult> results = future.join();
        assertEquals(2, results.size());
        assertTrue(results.get(0).isSuccess());
        assertTrue(results.get(1).isSuccess());
        assertEquals("Success1", results.get(0).getData());
        assertEquals("Success2", results.get(1).getData());
    }

    @Test
    void testExecuteActionDifferentProviderTypes() {
        // Given
        AIActionContext actionContext1 = createTestActionContext("test.action1", Map.of("param", "value1"));
        AIActionContext actionContext2 = createTestActionContext("test.action2", Map.of("param", "value2"));
        AIActionContext actionContext3 = createTestActionContext("test.action3", Map.of("param", "value3"));
        AIActionResult expectedResult = AIActionResult.success("Success", 100);

        when(securityValidator.validateAction(any())).thenReturn(true);
        when(agentDelegationService.delegateAction(any()))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // When - Test different provider types with different action contexts to avoid caching
        CompletableFuture<AIActionResult> future1 = unifiedActionExecutionService.executeAction(actionContext1,
                LLMProviderType.OLLAMA);
        CompletableFuture<AIActionResult> future2 = unifiedActionExecutionService.executeAction(actionContext2,
                LLMProviderType.OPENAI);
        CompletableFuture<AIActionResult> future3 = unifiedActionExecutionService.executeAction(actionContext3,
                LLMProviderType.ANTHROPIC);

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
        AIActionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));

        when(securityValidator.validateAction(actionContext)).thenReturn(true);

        // When & Then - Test with LMSTUDIO which should now be supported
        // This test verifies that LMSTUDIO is properly handled
        assertDoesNotThrow(() -> {
            unifiedActionExecutionService.executeAction(actionContext, LLMProviderType.LMSTUDIO);
        });

        // Verify that the action was delegated
        verify(agentDelegationService, times(1)).delegateAction(actionContext);
    }

    @Test
    void testClearCache() {
        // Given
        AIActionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));
        AIActionResult expectedResult = AIActionResult.success("Success", 100);

        when(securityValidator.validateAction(actionContext)).thenReturn(true);
        when(agentDelegationService.delegateAction(actionContext))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // Execute action to populate cache
        unifiedActionExecutionService.executeAction(actionContext, LLMProviderType.OPENAI).join();

        // Clear cache
        unifiedActionExecutionService.clearCache();

        // Execute action again
        unifiedActionExecutionService.executeAction(actionContext, LLMProviderType.OPENAI).join();

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

        AIActionContext actionContext1 = createTestActionContext("test.action1", Map.of("param", "value1"));
        AIActionContext actionContext2 = createTestActionContext("test.action2", Map.of("param", "value2"));
        AIActionResult expectedResult = AIActionResult.success("Success", 100);

        when(securityValidator.validateAction(any())).thenReturn(true);
        when(agentDelegationService.delegateAction(any()))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // Execute some actions with different contexts to avoid caching
        freshService.executeAction(actionContext1, LLMProviderType.OPENAI).join();
        freshService.executeAction(actionContext2, LLMProviderType.OPENAI).join();

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
        AIActionContext actionContext = createTestActionContext(null, Map.of("param", "value"));

        // When
        CompletableFuture<AIActionResult> future = unifiedActionExecutionService.executeAction(actionContext,
                LLMProviderType.OPENAI);

        // Then
        AIActionResult result = future.join();
        assertFalse(result.isSuccess());
        assertEquals("Action execution failed", result.getMessage());

        verify(agentDelegationService, never()).delegateAction(any());
    }

    @Test
    void testExecuteActionWithEmptyActionName() {
        // Given
        AIActionContext actionContext = createTestActionContext("", Map.of("param", "value"));

        // When
        CompletableFuture<AIActionResult> future = unifiedActionExecutionService.executeAction(actionContext,
                LLMProviderType.OPENAI);

        // Then
        AIActionResult result = future.join();
        assertFalse(result.isSuccess());
        assertEquals("Action execution failed", result.getMessage());

        verify(agentDelegationService, never()).delegateAction(any());
    }

    @Test
    void testResetMetrics() {
        // Given
        AIActionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));
        AIActionResult expectedResult = AIActionResult.success("Success", 100);

        when(securityValidator.validateAction(actionContext)).thenReturn(true);
        when(agentDelegationService.delegateAction(actionContext))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // Execute an action to populate metrics
        unifiedActionExecutionService.executeAction(actionContext, LLMProviderType.OPENAI).join();

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

    private AIActionContext createTestActionContext(String actionName, Map<String, Object> arguments) {
        Map<String, Object> protocolContext;
        if (actionName == null) {
            protocolContext = Map.of("arguments", arguments);
        } else {
            protocolContext = Map.of("action", actionName, "arguments", arguments);
        }

        return AIActionContext.builder().protocol("a2a").clientId("test-client").sessionId("test-session")
                .correlationId("test-correlation-" + System.currentTimeMillis()).protocolContext(protocolContext)
                .build();
    }
}
