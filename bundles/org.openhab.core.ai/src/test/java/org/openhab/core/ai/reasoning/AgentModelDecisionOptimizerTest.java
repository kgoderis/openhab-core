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
package org.openhab.core.ai.reasoning;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.reasoning.api.ConfigurationManager;
import org.openhab.core.ai.reasoning.api.ErrorHandler;
import org.openhab.core.ai.reasoning.api.MemoryManager;
import org.openhab.core.ai.reasoning.api.ReasoningContext;
import org.openhab.core.ai.reasoning.api.ReasoningEngine;
import org.openhab.core.ai.reasoning.api.ReasoningStep;
import org.openhab.core.ai.reasoning.api.SecurityManager;

/**
 * Unit tests for AgentModelDecisionOptimizer.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class AgentModelDecisionOptimizerTest {

    @Mock
    private SecurityManager securityManager;

    @Mock
    private ErrorHandler errorHandler;

    @Mock
    private ConfigurationManager configurationManager;

    @Mock
    private MemoryManager memoryManager;

    @Mock
    private ReasoningEngine reasoningEngine;

    private AgentModelDecisionOptimizer optimizer;

    @BeforeEach
    void setUp() {
        optimizer = new AgentModelDecisionOptimizer(securityManager, errorHandler, configurationManager, memoryManager,
                reasoningEngine);
    }

    @Test
    void testOptimizeDecision_Success() throws Exception {
        // Arrange
        ReasoningContext context = createTestContext();
        List<ReasoningStep> decisionSteps = createTestDecisionSteps();

        SecurityManager.SecurityValidationResult securityResult = new SecurityManager.SecurityValidationResult("test",
                true, new SecurityManager.SecurityIssue[0], System.currentTimeMillis());

        when(securityManager.validateSecurity(any(SecurityManager.SecurityRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(securityResult));

        // Act
        List<ReasoningStep> result = optimizer.optimizeDecision(context, decisionSteps).get();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(decisionSteps.size(), result.size());

        verify(securityManager).validateSecurity(any(SecurityManager.SecurityRequest.class));
    }

    @Test
    void testOptimizeDecision_SecurityFailure() {
        // Arrange
        ReasoningContext context = createTestContext();
        List<ReasoningStep> decisionSteps = createTestDecisionSteps();

        SecurityManager.SecurityIssue[] issues = {
                new SecurityManager.SecurityIssue(SecurityManager.SecurityIssueType.AUTHORIZATION_FAILED,
                        "Access denied", SecurityManager.SecurityLevel.HIGH) };

        SecurityManager.SecurityValidationResult securityResult = new SecurityManager.SecurityValidationResult("test",
                false, issues, System.currentTimeMillis());

        when(securityManager.validateSecurity(any(SecurityManager.SecurityRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(securityResult));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            optimizer.optimizeDecision(context, decisionSteps).get();
        });

        // Check if it's an ExecutionException and get the cause
        String message = exception.getMessage();
        Throwable cause = exception.getCause();
        while (cause != null) {
            message = cause.getMessage();
            cause = cause.getCause();
        }
        System.out.println("Exception message: " + message);
        assertTrue(message.contains("Access denied for decision optimization"));
        verify(securityManager).validateSecurity(any(SecurityManager.SecurityRequest.class));
    }

    @Test
    void testOptimizeDecision_NullContext() {
        // Arrange
        List<ReasoningStep> decisionSteps = createTestDecisionSteps();

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            optimizer.optimizeDecision(null, decisionSteps).get();
        });

        assertTrue(exception.getMessage().contains("Decision optimization failed"));
    }

    @Test
    void testOptimizeDecision_NullDecisionSteps() {
        // Arrange
        ReasoningContext context = createTestContext();

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            optimizer.optimizeDecision(context, null).get();
        });

        assertTrue(exception.getMessage().contains("Decision optimization failed"));
    }

    @Test
    void testOptimizeDecision_EmptyDecisionSteps() {
        // Arrange
        ReasoningContext context = createTestContext();
        List<ReasoningStep> decisionSteps = List.of();

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            optimizer.optimizeDecision(context, decisionSteps).get();
        });

        assertTrue(exception.getMessage().contains("Decision optimization failed"));
    }

    @Test
    void testGetPerformanceMetrics() {
        // Act
        Map<String, Long> metrics = optimizer.getPerformanceMetrics();

        // Assert
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("totalOptimizations"));
        assertTrue(metrics.containsKey("successfulOptimizations"));
        assertTrue(metrics.containsKey("failedOptimizations"));

        assertEquals(0L, metrics.get("totalOptimizations"));
        assertEquals(0L, metrics.get("successfulOptimizations"));
        assertEquals(0L, metrics.get("failedOptimizations"));
    }

    @Test
    void testPerformanceMetrics_AfterSuccessfulOptimization() throws Exception {
        // Arrange
        ReasoningContext context = createTestContext();
        List<ReasoningStep> decisionSteps = createTestDecisionSteps();

        SecurityManager.SecurityValidationResult securityResult = new SecurityManager.SecurityValidationResult("test",
                true, new SecurityManager.SecurityIssue[0], System.currentTimeMillis());

        when(securityManager.validateSecurity(any(SecurityManager.SecurityRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(securityResult));

        // Act
        optimizer.optimizeDecision(context, decisionSteps).get();
        Map<String, Long> metrics = optimizer.getPerformanceMetrics();

        // Assert
        assertEquals(1L, metrics.get("totalOptimizations"));
        assertEquals(1L, metrics.get("successfulOptimizations"));
        assertEquals(0L, metrics.get("failedOptimizations"));
    }

    @Test
    void testPerformanceMetrics_AfterFailedOptimization() throws InterruptedException {
        // Arrange
        ReasoningContext context = createTestContext();
        List<ReasoningStep> decisionSteps = createTestDecisionSteps();

        SecurityManager.SecurityIssue[] issues = {
                new SecurityManager.SecurityIssue(SecurityManager.SecurityIssueType.AUTHORIZATION_FAILED,
                        "Access denied", SecurityManager.SecurityLevel.HIGH) };

        SecurityManager.SecurityValidationResult securityResult = new SecurityManager.SecurityValidationResult("test",
                false, issues, System.currentTimeMillis());

        when(securityManager.validateSecurity(any(SecurityManager.SecurityRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(securityResult));

        // Act
        try {
            optimizer.optimizeDecision(context, decisionSteps).get();
        } catch (Exception e) {
            // Expected to fail
        }

        // Wait a bit for async operations to complete
        Thread.sleep(100);
        Map<String, Long> metrics = optimizer.getPerformanceMetrics();

        // Assert
        System.out.println("Metrics: " + metrics);
        assertEquals(1L, metrics.get("totalOptimizations"));
        assertEquals(0L, metrics.get("successfulOptimizations"));
        assertEquals(1L, metrics.get("failedOptimizations"));
    }

    // Helper methods

    private ReasoningContext createTestContext() {
        return ReasoningContext.builder().initialContext("test-initial-context").currentContext("test-current-context")
                .sessionId("test-session").userId("test-agent").domain("test-model").build();
    }

    private List<ReasoningStep> createTestDecisionSteps() {
        ActionContext actionContext1 = ActionContext.builder().protocol("mcp").clientId("client1").sessionId("session1")
                .correlationId("corr1").build();

        ActionContext actionContext2 = ActionContext.builder().protocol("a2a").clientId("client2").sessionId("session2")
                .correlationId("corr2").build();

        ReasoningStep step1 = ReasoningStep.builder().sessionId("test-session").stepNumber(1)
                .reasoning("First reasoning step").toolCalls(List.of(actionContext1)).confidence(0.8).isComplete(true)
                .startTime(Instant.now()).endTime(Instant.now()).build();

        ReasoningStep step2 = ReasoningStep.builder().sessionId("test-session").stepNumber(2)
                .reasoning("Second reasoning step").toolCalls(List.of(actionContext2)).confidence(0.9).isComplete(true)
                .startTime(Instant.now()).endTime(Instant.now()).build();

        return List.of(step1, step2);
    }
}
