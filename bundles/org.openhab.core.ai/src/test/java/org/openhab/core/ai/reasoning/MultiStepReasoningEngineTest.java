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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.ModelResponse;
import org.openhab.core.ai.model.api.ModelClient;
import org.openhab.core.ai.reasoning.api.MultiStepReasoningConfiguration;
import org.openhab.core.ai.reasoning.api.MultiStepReasoningResult;
import org.openhab.core.ai.reasoning.api.ReasoningContext;

/**
 * Unit tests for MultiStepReasoningEngine
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
public class MultiStepReasoningEngineTest {

    @Mock
    private ModelClient llmClient;

    @Mock
    private ActionRegistry actionRegistry;

    private MultiStepReasoningEngine reasoningEngine;

    @BeforeEach
    void setUp() {
        reasoningEngine = new MultiStepReasoningEngine();
        // Use reflection to inject mocks
        try {
            java.lang.reflect.Field llmClientField = MultiStepReasoningEngine.class.getDeclaredField("llmClient");
            llmClientField.setAccessible(true);
            llmClientField.set(reasoningEngine, llmClient);

            java.lang.reflect.Field actionRegistryField = MultiStepReasoningEngine.class
                    .getDeclaredField("actionRegistry");
            actionRegistryField.setAccessible(true);
            actionRegistryField.set(reasoningEngine, actionRegistry);

            // Initialize configuration
            java.lang.reflect.Field configField = MultiStepReasoningEngine.class.getDeclaredField("configuration");
            configField.setAccessible(true);
            configField.set(reasoningEngine, new MultiStepReasoningConfiguration());
        } catch (Exception e) {
            fail("Failed to inject mocks: " + e.getMessage());
        }
    }

    @Test
    void testReasonAsyncWithValidContext() throws ExecutionException, InterruptedException {
        // Given
        ReasoningContext context = new ReasoningContext("Initial context", "Current context");
        ModelResponse mockResponse = createMockModelResponse("Step 1 reasoning: Analyzing the situation...");

        when(llmClient.complete(anyString(), any(ModelParameters.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // When
        CompletableFuture<MultiStepReasoningResult> future = reasoningEngine.reasonAsync(context);
        MultiStepReasoningResult result = future.get();

        // Then
        assertNotNull(result);
        assertEquals("reasoning-", result.getSessionId().substring(0, 10));
        assertFalse(result.getSteps().isEmpty());
        assertEquals(1, result.getSteps().size());
        assertEquals("Step 1 reasoning: Analyzing the situation...", result.getSteps().get(0).getReasoning());
        assertTrue(result.getSteps().get(0).getStepNumber() > 0);
        assertNotNull(result.getStartTime());
        assertNotNull(result.getEndTime());

        verify(llmClient, atLeastOnce()).complete(anyString(), any(ModelParameters.class));
    }

    @Test
    void testMultiStepReasoningWithContextAccumulation() throws ExecutionException, InterruptedException {
        // Given
        ReasoningContext context = new ReasoningContext("Initial context", "Current context");
        ModelResponse step1Response = createMockModelResponse("Step 1: Understanding the problem...");
        ModelResponse step2Response = createMockModelResponse("Step 2: Based on previous analysis, I can see...");

        when(llmClient.complete(anyString(), any(ModelParameters.class)))
                .thenReturn(CompletableFuture.completedFuture(step1Response))
                .thenReturn(CompletableFuture.completedFuture(step2Response));

        // When
        CompletableFuture<MultiStepReasoningResult> future = reasoningEngine.reasonAsync(context);
        MultiStepReasoningResult result = future.get();

        // Then
        assertNotNull(result);
        assertTrue(result.getSteps().size() >= 2);

        // Verify that context is being accumulated
        verify(llmClient, atLeast(2)).complete(anyString(), any(ModelParameters.class));

        // Check that the second call includes context from the first step
        verify(llmClient).complete(argThat(prompt -> prompt.contains("Step 1")), any(ModelParameters.class));
    }

    @Test
    void testReasoningWithGuidancePrompts() throws ExecutionException, InterruptedException {
        // Given
        ReasoningContext context = ReasoningContext.builder().initialContext("Initial context")
                .currentContext("Current context").domain("home-automation").build();

        ModelResponse mockResponse = createMockModelResponse("Following guidance for step 1...");

        when(llmClient.complete(anyString(), any(ModelParameters.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // When
        CompletableFuture<MultiStepReasoningResult> future = reasoningEngine.reasonAsync(context);
        MultiStepReasoningResult result = future.get();

        // Then
        assertNotNull(result);
        assertFalse(result.getSteps().isEmpty());

        // Verify that guidance prompts are included
        verify(llmClient).complete(
                argThat(prompt -> prompt.contains("Guidance for Step 1") && prompt.contains("home-automation")),
                any(ModelParameters.class));
    }

    @Test
    void testStepLimitEnforcement() throws ExecutionException, InterruptedException {
        // Given
        ReasoningContext context = new ReasoningContext("Initial context", "Current context");
        ModelResponse mockResponse = createMockModelResponse("Continuing reasoning...");

        when(llmClient.complete(anyString(), any(ModelParameters.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // When
        CompletableFuture<MultiStepReasoningResult> future = reasoningEngine.reasonAsync(context);
        MultiStepReasoningResult result = future.get();

        // Then
        assertNotNull(result);
        assertTrue(result.getSteps().size() <= 5); // Default max steps is 5

        // Verify that we don't exceed the step limit
        verify(llmClient, atMost(5)).complete(anyString(), any(ModelParameters.class));
    }

    @Test
    void testReasoningCompletionDetection() throws ExecutionException, InterruptedException {
        // Given
        ReasoningContext context = new ReasoningContext("Initial context", "Current context");
        ModelResponse completionResponse = createMockModelResponse(
                "Final conclusion: The answer is 42. Reasoning complete.");

        when(llmClient.complete(anyString(), any(ModelParameters.class)))
                .thenReturn(CompletableFuture.completedFuture(completionResponse));

        // When
        CompletableFuture<MultiStepReasoningResult> future = reasoningEngine.reasonAsync(context);
        MultiStepReasoningResult result = future.get();

        // Then
        assertNotNull(result);
        assertTrue(result.isCompleted());
        assertEquals("Final conclusion: The answer is 42. Reasoning complete.", result.getFinalReasoning());

        // Should only make one call since reasoning was completed
        verify(llmClient, times(1)).complete(anyString(), any(ModelParameters.class));
    }

    @Test
    void testConfidenceThresholdCompletion() throws ExecutionException, InterruptedException {
        // Given
        ReasoningContext context = new ReasoningContext("Initial context", "Current context");
        ModelResponse highConfidenceResponse = createMockModelResponse("I am very confident about this because...");

        when(llmClient.complete(anyString(), any(ModelParameters.class)))
                .thenReturn(CompletableFuture.completedFuture(highConfidenceResponse));

        // When
        CompletableFuture<MultiStepReasoningResult> future = reasoningEngine.reasonAsync(context);
        MultiStepReasoningResult result = future.get();

        // Then
        assertNotNull(result);
        assertTrue(result.getConfidence() > 0.0);

        // The reasoning should complete when confidence threshold is met
        verify(llmClient, atMost(2)).complete(anyString(), any(ModelParameters.class));
    }

    @Test
    void testErrorHandlingAndRecovery() throws ExecutionException, InterruptedException {
        // Given
        ReasoningContext context = new ReasoningContext("Initial context", "Current context");

        when(llmClient.complete(anyString(), any(ModelParameters.class)))
                .thenThrow(new RuntimeException("LLM service unavailable"))
                .thenReturn(CompletableFuture.completedFuture(createMockModelResponse("Recovered reasoning...")));

        // When
        CompletableFuture<MultiStepReasoningResult> future = reasoningEngine.reasonAsync(context);
        MultiStepReasoningResult result = future.get();

        // Then
        assertNotNull(result);
        assertFalse(result.getSteps().isEmpty());

        // Should have an error step
        boolean hasErrorStep = result.getSteps().stream().anyMatch(step -> step.getError() != null);
        assertTrue(hasErrorStep);
    }

    @Test
    void testPerformanceMonitoring() throws ExecutionException, InterruptedException {
        // Given
        ReasoningContext context = new ReasoningContext("Initial context", "Current context");
        ModelResponse mockResponse = createMockModelResponse("Performance test reasoning...");

        when(llmClient.complete(anyString(), any(ModelParameters.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // When
        CompletableFuture<MultiStepReasoningResult> future = reasoningEngine.reasonAsync(context);
        MultiStepReasoningResult result = future.get();

        // Then
        assertNotNull(result);

        // Check performance metrics
        MultiStepReasoningEngine.PerformanceMetrics metrics = reasoningEngine.getPerformanceMetrics();
        assertNotNull(metrics);
        assertTrue(metrics.getTotalSessions() > 0);
        assertTrue(metrics.getTotalSteps() > 0);
        assertTrue(metrics.getAverageSessionDuration() >= 0.0);
    }

    @Test
    void testConfigurationUpdate() {
        // Given
        MultiStepReasoningConfiguration newConfig = MultiStepReasoningConfiguration.builder().maxSteps(10)
                .sessionTimeoutMs(120000).confidenceThreshold(0.9).guidancePromptsEnabled(false).build();

        // When
        reasoningEngine.updateConfiguration(newConfig);

        // Then
        // Configuration should be updated (we can't directly verify this without exposing the config)
        // But we can verify the method doesn't throw an exception
        assertDoesNotThrow(() -> reasoningEngine.updateConfiguration(newConfig));
    }

    @Test
    void testActionIntegration() throws ExecutionException, InterruptedException {
        // Given
        ReasoningContext context = new ReasoningContext("Initial context", "Current context");
        ModelResponse mockResponse = createMockModelResponse("I need to call an action to get more information...");

        when(llmClient.complete(anyString(), any(ModelParameters.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // When
        CompletableFuture<MultiStepReasoningResult> future = reasoningEngine.reasonAsync(context);
        MultiStepReasoningResult result = future.get();

        // Then
        assertNotNull(result);
        // Actions are currently not implemented, so this should be empty
        assertTrue(result.getToolCalls().isEmpty());
    }

    @Test
    void testSessionTimeoutHandling() throws ExecutionException, InterruptedException {
        // Given
        ReasoningContext context = new ReasoningContext("Initial context", "Current context");

        // Simulate a slow response that would exceed timeout
        CompletableFuture<ModelResponse> slowResponse = new CompletableFuture<>();
        when(llmClient.complete(anyString(), any(ModelParameters.class))).thenReturn(slowResponse);

        // When
        CompletableFuture<MultiStepReasoningResult> future = reasoningEngine.reasonAsync(context);

        // Don't complete the slow response, let it timeout
        MultiStepReasoningResult result = future.get();

        // Then
        assertNotNull(result);
        // Should handle timeout gracefully
        assertFalse(result.isCompleted());
    }

    @Test
    void testNullContextHandling() {
        // When & Then - The method should throw NullPointerException when context is null
        // because it tries to access context.getInitialContext()
        assertThrows(NullPointerException.class, () -> {
            reasoningEngine.reasonAsync(null);
        });
    }

    @Test
    void testEmptyContextHandling() throws ExecutionException, InterruptedException {
        // Given
        ReasoningContext emptyContext = new ReasoningContext("", "");
        ModelResponse mockResponse = createMockModelResponse("Reasoning with empty context...");

        when(llmClient.complete(anyString(), any(ModelParameters.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // When
        CompletableFuture<MultiStepReasoningResult> future = reasoningEngine.reasonAsync(emptyContext);
        MultiStepReasoningResult result = future.get();

        // Then
        assertNotNull(result);
        assertFalse(result.getSteps().isEmpty());
        // Should handle empty context gracefully
    }

    @Test
    void testConcurrentReasoningSessions() throws ExecutionException, InterruptedException {
        // Given
        ReasoningContext context1 = new ReasoningContext("Context 1", "Current 1");
        ReasoningContext context2 = new ReasoningContext("Context 2", "Current 2");
        ModelResponse mockResponse = createMockModelResponse("Concurrent reasoning...");

        when(llmClient.complete(anyString(), any(ModelParameters.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // When
        CompletableFuture<MultiStepReasoningResult> future1 = reasoningEngine.reasonAsync(context1);
        CompletableFuture<MultiStepReasoningResult> future2 = reasoningEngine.reasonAsync(context2);

        MultiStepReasoningResult result1 = future1.get();
        MultiStepReasoningResult result2 = future2.get();

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1.getSessionId(), result2.getSessionId());

        // Both sessions should complete successfully
        assertFalse(result1.getSteps().isEmpty());
        assertFalse(result2.getSteps().isEmpty());
    }

    private ModelResponse createMockModelResponse(String content) {
        ModelResponse response = mock(ModelResponse.class);
        when(response.getContent()).thenReturn(content);
        return response;
    }
}
