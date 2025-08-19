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
import org.openhab.core.ai.action.api.ActionError;
import org.openhab.core.ai.action.api.ActionKeys;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionSecurityValidator;
import org.openhab.core.ai.agent.delegation.api.AgentActionDelegationService;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.openhab.core.ai.model.api.ModelProviderType;

/**
 * Unit tests for DefaultActionExecutionService
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class DefaultActionExecutionServiceTest {

    private DefaultActionExecutionService defaultActionExecutionService;

    @Mock
    private AgentActionDelegationService agentDelegationService;

    @Mock
    private ActionRegistry actionRegistry;

    @Mock
    private ActionSecurityValidator securityValidator;

    @BeforeEach
    void setUp() {
        defaultActionExecutionService = new DefaultActionExecutionService();

        // Use reflection to inject mocks
        try {
            java.lang.reflect.Field delegationField = DefaultActionExecutionService.class
                    .getDeclaredField("agentDelegationService");
            delegationField.setAccessible(true);
            delegationField.set(defaultActionExecutionService, agentDelegationService);

            java.lang.reflect.Field registryField = DefaultActionExecutionService.class
                    .getDeclaredField("actionRegistry");
            registryField.setAccessible(true);
            registryField.set(defaultActionExecutionService, actionRegistry);

            java.lang.reflect.Field validatorField = DefaultActionExecutionService.class
                    .getDeclaredField("securityValidator");
            validatorField.setAccessible(true);
            validatorField.set(defaultActionExecutionService, securityValidator);
        } catch (Exception e) {
            fail("Failed to inject mocks: " + e.getMessage());
        }

        // Reset metrics for clean test isolation
        defaultActionExecutionService.resetMetrics();
    }

    @Test
    void testExecuteActionSuccess() {
        // Given
        ExecutionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));
        ActionResult expectedResult = ActionResult.success("Success", 100);

        when(securityValidator.isAvailable()).thenReturn(true);
        when(securityValidator.validateAction(actionContext)).thenReturn(true);
        when(agentDelegationService.delegateAction(actionContext))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        CompletableFuture<ActionResult> future = defaultActionExecutionService.executeAction(actionContext,
                ModelProviderType.OPENAI);

        // Then
        ActionResult result = future.join();
        assertTrue(result.isSuccess());
        assertEquals("Success", result.getData());
        assertEquals(100, result.getExecutionTimeMs());

        verify(securityValidator).validateAction(actionContext);
        // With retry logic enabled (default 3 retries), the delegateAction will be called multiple times
        verify(agentDelegationService, atLeastOnce()).delegateAction(actionContext);
    }

    @Test
    void testExecuteActionSecurityValidationFailure() {
        // Given
        ExecutionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));

        when(securityValidator.isAvailable()).thenReturn(true);
        when(securityValidator.validateAction(actionContext)).thenReturn(false);

        // When
        CompletableFuture<ActionResult> future = defaultActionExecutionService.executeAction(actionContext,
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
    void testExecuteActionWithEmptyActionName() {
        // Given
        ExecutionContext actionContext = createTestActionContext("", Map.of("param", "value"));

        // When
        CompletableFuture<ActionResult> future = defaultActionExecutionService.executeAction(actionContext,
                ModelProviderType.OPENAI);

        // Then
        ActionResult result = future.join();
        assertFalse(result.isSuccess());
        assertEquals("Action execution failed", result.getMessage());
        assertNotNull(result.getError());
        assertEquals("VALIDATION_ERROR", result.getError().getErrorCode());
        assertEquals("Action name cannot be null or empty", result.getError().getErrorMessage());

        verify(agentDelegationService, never()).delegateAction(any());
    }

    @Test
    void testExecuteActionWithNullActionName() {
        // Given
        ExecutionContext actionContext = createTestActionContext(null, Map.of("param", "value"));

        // When
        CompletableFuture<ActionResult> future = defaultActionExecutionService.executeAction(actionContext,
                ModelProviderType.OPENAI);

        // Then
        ActionResult result = future.join();
        assertFalse(result.isSuccess());
        assertEquals("Action execution failed", result.getMessage());
        assertNotNull(result.getError());
        assertEquals("VALIDATION_ERROR", result.getError().getErrorCode());
        assertEquals("Action name cannot be null or empty", result.getError().getErrorMessage());

        verify(agentDelegationService, never()).delegateAction(any());
    }

    @Test
    void testExecuteActionWithSecurityValidatorNotAvailable() {
        // Given
        ExecutionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));
        ActionResult expectedResult = ActionResult.success("Success", 100);

        when(securityValidator.isAvailable()).thenReturn(false);
        when(agentDelegationService.delegateAction(actionContext))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        CompletableFuture<ActionResult> future = defaultActionExecutionService.executeAction(actionContext,
                ModelProviderType.OPENAI);

        // Then
        ActionResult result = future.join();
        assertTrue(result.isSuccess());
        assertEquals("Success", result.getData());

        verify(securityValidator, never()).validateAction(any());
        verify(agentDelegationService).delegateAction(actionContext);
    }

    @Test
    void testExecuteActionsInParallel() {
        // Given
        ExecutionContext context1 = createTestActionContext("action1", Map.of("param1", "value1"),
                "test-correlation-id-1");
        ExecutionContext context2 = createTestActionContext("action2", Map.of("param2", "value2"),
                "test-correlation-id-2");
        ActionResult result1 = ActionResult.success("Result1", 100);
        ActionResult result2 = ActionResult.success("Result2", 200);

        when(securityValidator.isAvailable()).thenReturn(true);
        when(securityValidator.validateAction(context1)).thenReturn(true);
        when(securityValidator.validateAction(context2)).thenReturn(true);
        when(agentDelegationService.delegateAction(any(ExecutionContext.class))).thenAnswer(invocation -> {
            ExecutionContext context = invocation.getArgument(0);
            String actionName = context.getValue(ActionKeys.ACTION_NAME.getKey(), String.class);
            if ("action1".equals(actionName)) {
                return CompletableFuture.completedFuture(result1);
            } else if ("action2".equals(actionName)) {
                return CompletableFuture.completedFuture(result2);
            }
            return CompletableFuture.completedFuture(
                    ActionResult.error("Unknown action", new ActionError("UNKNOWN", "Unknown action"), 0));
        });

        // When
        CompletableFuture<List<ActionResult>> future = defaultActionExecutionService
                .executeActionsParallel(List.of(context1, context2), ModelProviderType.OPENAI);

        // Then
        List<ActionResult> results = future.join();
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(ActionResult::isSuccess));
        // Order might not be guaranteed in parallel execution
        assertTrue(results.stream().anyMatch(r -> "Result1".equals(r.getData())));
        assertTrue(results.stream().anyMatch(r -> "Result2".equals(r.getData())));

        // With retry logic enabled (default 3 retries), each action might be called multiple times
        verify(agentDelegationService, atLeastOnce()).delegateAction(context1);
        verify(agentDelegationService, atLeastOnce()).delegateAction(context2);
    }

    @Test
    void testExecuteActionWithRetryLogic() {
        // Given
        ExecutionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));
        ActionResult failureResult = ActionResult.error("Failed",
                new ActionError("EXECUTION_ERROR", "Temporary failure"), 50);
        ActionResult successResult = ActionResult.success("Success", 100);

        when(securityValidator.isAvailable()).thenReturn(true);
        when(securityValidator.validateAction(actionContext)).thenReturn(true);
        when(agentDelegationService.delegateAction(actionContext))
                .thenReturn(CompletableFuture.completedFuture(failureResult))
                .thenReturn(CompletableFuture.completedFuture(successResult));

        // When
        CompletableFuture<ActionResult> future = defaultActionExecutionService.executeActionWithRetry(actionContext,
                ModelProviderType.OPENAI, 1);

        // Then
        ActionResult result = future.join();
        assertTrue(result.isSuccess());
        assertEquals("Success", result.getData());

        verify(agentDelegationService, times(2)).delegateAction(actionContext);
    }

    @Test
    void testExecuteActionWithException() {
        // Given
        ExecutionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));
        RuntimeException exception = new RuntimeException("Test exception");

        when(securityValidator.isAvailable()).thenReturn(true);
        when(securityValidator.validateAction(actionContext)).thenReturn(true);
        when(agentDelegationService.delegateAction(actionContext))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // When
        CompletableFuture<ActionResult> future = defaultActionExecutionService.executeAction(actionContext,
                ModelProviderType.OPENAI);

        // Then
        ActionResult result = future.join();
        assertFalse(result.isSuccess());
        assertEquals("Action execution failed", result.getMessage());
        assertNotNull(result.getError());
        assertEquals("EXECUTION_ERROR", result.getError().getErrorCode());
        assertEquals("java.lang.RuntimeException: Test exception", result.getError().getErrorMessage());

        // With retry logic enabled (default 3 retries), the delegateAction will be called multiple times
        verify(agentDelegationService, atLeastOnce()).delegateAction(actionContext);
    }

    @Test
    void testPerformanceMetrics() {
        // Given
        ExecutionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));
        ActionResult expectedResult = ActionResult.success("Success", 100);

        when(securityValidator.isAvailable()).thenReturn(true);
        when(securityValidator.validateAction(actionContext)).thenReturn(true);
        when(agentDelegationService.delegateAction(actionContext))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // When
        defaultActionExecutionService.executeAction(actionContext, ModelProviderType.OPENAI).join();
        Map<String, Object> metrics = defaultActionExecutionService.getPerformanceMetrics();

        // Then
        assertEquals(1L, metrics.get("totalExecutions"));
        assertEquals(1L, metrics.get("successfulExecutions"));
        assertEquals(0L, metrics.get("failedExecutions"));
        assertTrue((Double) metrics.get("successRate") >= 0.0);
        assertTrue((Double) metrics.get("averageExecutionTime") >= 0.0);
    }

    @Test
    void testConfigurationUpdate() {
        // Given
        DefaultActionExecutionService.Configuration config = DefaultActionExecutionService.Configuration.builder()
                .maxRetryAttempts(5).retryDelay(Duration.ofSeconds(2)).enableCaching(false)
                .enableSecurityValidation(false).cacheExpiration(Duration.ofMinutes(10));

        // When
        defaultActionExecutionService.updateConfiguration(config);

        // Then
        // Configuration is updated internally, we can verify by checking behavior
        ExecutionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));
        ActionResult expectedResult = ActionResult.success("Success", 100);

        when(agentDelegationService.delegateAction(actionContext))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        CompletableFuture<ActionResult> future = defaultActionExecutionService.executeAction(actionContext,
                ModelProviderType.OPENAI);
        ActionResult result = future.join();

        assertTrue(result.isSuccess());
        // Security validation should be skipped due to configuration
        verify(securityValidator, never()).validateAction(any());
    }

    @Test
    void testClearCache() {
        // When
        boolean result = defaultActionExecutionService.clearCache();

        // Then
        assertTrue(result);
        // Cache is cleared internally, no exceptions should be thrown
        assertDoesNotThrow(() -> defaultActionExecutionService.clearCache());
    }

    @Test
    void testResetMetrics() {
        // Given
        ExecutionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));
        ActionResult expectedResult = ActionResult.success("Success", 100);

        when(securityValidator.isAvailable()).thenReturn(true);
        when(securityValidator.validateAction(actionContext)).thenReturn(true);
        when(agentDelegationService.delegateAction(actionContext))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

        // Execute an action to populate metrics
        defaultActionExecutionService.executeAction(actionContext, ModelProviderType.OPENAI).join();

        // When
        defaultActionExecutionService.resetMetrics();

        // Then
        Map<String, Object> metrics = defaultActionExecutionService.getPerformanceMetrics();
        assertEquals(0L, metrics.get("totalExecutions"));
        assertEquals(0L, metrics.get("successfulExecutions"));
        assertEquals(0L, metrics.get("failedExecutions"));
        assertEquals(0.0, metrics.get("successRate"));
        assertEquals(0.0, metrics.get("averageExecutionTime"));
    }

    @Test
    void testUnsupportedProviderType() {
        // Given
        ExecutionContext actionContext = createTestActionContext("test.action", Map.of("param", "value"));

        // When
        CompletableFuture<ActionResult> future = defaultActionExecutionService.executeAction(actionContext, null);

        // Then
        ActionResult result = future.join();
        assertFalse(result.isSuccess());
        assertEquals("Action execution failed", result.getMessage());
        assertNotNull(result.getError());
        assertEquals("VALIDATION_ERROR", result.getError().getErrorCode());
        assertEquals("Provider type cannot be null", result.getError().getErrorMessage());
    }

    private ExecutionContext createTestActionContext(String actionName, Map<String, Object> parameters) {
        return createTestActionContext(actionName, parameters, "test-correlation-id");
    }

    private ExecutionContext createTestActionContext(String actionName, Map<String, Object> parameters,
            String correlationId) {
        return ExecutionContext.builder().correlationId(correlationId).protocol("test-protocol")
                .withActionName(actionName).withParameters(parameters).build();
    }
}
