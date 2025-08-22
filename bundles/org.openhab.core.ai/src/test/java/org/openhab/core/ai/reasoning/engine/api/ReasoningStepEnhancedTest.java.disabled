package org.openhab.core.ai.reasoning.engine.api;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test for the enhanced ReasoningStep class with all new fields.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ReasoningStepEnhancedTest {

    @Test
    void testEnhancedReasoningStepBuilder() {
        // Test building a complete reasoning step with all new fields
        ReasoningStep step = ReasoningStep.builder().withSessionId("test-session-123").withStepNumber(1)
                .withReasoning("This is a test reasoning step").withStepType(ReasoningStepType.ANALYSIS)
                .withStatus(ReasoningStepStatus.COMPLETED).withModelId("gpt-4").withModelVersion("1.0")
                .withModelParameters(Map.of("temperature", 0.7, "max_tokens", 1000))
                .withInputContext(Map.of("user_query", "What is the weather?"))
                .withOutputContext(Map.of("response", "The weather is sunny"))
                .withIntermediateResult("Analyzing weather data...").withParentStepIds(java.util.List.of("step-0"))
                .withResourceUsage(new ResourceUsage(100, 50, 0.02, 1024, 150, "openai"))
                .withValidationInfo(new ValidationInfo(true, "Valid step", "validator-1", Instant.now(), 0.95, "auto"))
                .withIterationNumber(1).withContextChanges(Map.of("added_context", "weather_data"))
                .withStepMetadata(Map.of("priority", "high", "tags", "weather,analysis")).withConfidence(0.9)
                .withStartTime(Instant.now().minusSeconds(10)).withEndTime(Instant.now()).build();

        // Verify all fields are set correctly
        assertEquals("test-session-123", step.getSessionId());
        assertEquals(1, step.getStepNumber());
        assertEquals("This is a test reasoning step", step.getReasoning());
        assertEquals(ReasoningStepType.ANALYSIS, step.getStepType());
        assertEquals(ReasoningStepStatus.COMPLETED, step.getStatus());
        assertEquals("gpt-4", step.getModelId());
        assertEquals("1.0", step.getModelVersion());
        assertEquals(Map.of("temperature", 0.7, "max_tokens", 1000), step.getModelParameters());
        assertEquals(Map.of("user_query", "What is the weather?"), step.getInputContext());
        assertEquals(Map.of("response", "The weather is sunny"), step.getOutputContext());
        assertEquals("Analyzing weather data...", step.getIntermediateResult());
        assertEquals(java.util.List.of("step-0"), step.getParentStepIds());
        assertNotNull(step.getResourceUsage());
        assertEquals(100, step.getResourceUsage().inputTokens());
        assertEquals(50, step.getResourceUsage().outputTokens());
        assertEquals(0.02, step.getResourceUsage().costUsd());
        assertNotNull(step.getValidationInfo());
        assertTrue(step.getValidationInfo().isValid());
        assertEquals(1, step.getIterationNumber());
        assertEquals(Map.of("added_context", "weather_data"), step.getContextChanges());
        assertEquals(Map.of("priority", "high", "tags", "weather,analysis"), step.getStepMetadata());
        assertEquals(0.9, step.getConfidence());
        assertTrue(step.isComplete());
        assertNull(step.getError());
    }

    @Test
    void testDurationCalculation() {
        Instant start = Instant.now().minusSeconds(5);
        Instant end = Instant.now();

        ReasoningStep step = ReasoningStep.builder().withSessionId("test-session").withStepNumber(1)
                .withReasoning("Test reasoning").withStartTime(start).withEndTime(end).build();

        long duration = step.getDurationMs();
        assertTrue(duration >= 4900 && duration <= 5100); // Should be approximately 5 seconds
    }

    @Test
    void testEqualsAndHashCode() {
        ReasoningStep step1 = ReasoningStep.builder().withSessionId("session-1").withStepNumber(1)
                .withReasoning("Test reasoning").build();

        ReasoningStep step2 = ReasoningStep.builder().withSessionId("session-1").withStepNumber(1)
                .withReasoning("Different reasoning").build();

        ReasoningStep step3 = ReasoningStep.builder().withSessionId("session-2").withStepNumber(1)
                .withReasoning("Test reasoning").build();

        // Same sessionId and stepNumber should be equal
        assertEquals(step1, step2);
        assertEquals(step1.hashCode(), step2.hashCode());

        // Different sessionId should not be equal
        assertNotEquals(step1, step3);
        assertNotEquals(step1.hashCode(), step3.hashCode());
    }

    @Test
    void testToString() {
        ReasoningStep step = ReasoningStep.builder().withSessionId("test-session").withStepNumber(1)
                .withStepType(ReasoningStepType.ANALYSIS).withStatus(ReasoningStepStatus.COMPLETED)
                .withReasoning("Test reasoning").withConfidence(0.8).build();

        String toString = step.toString();
        assertTrue(toString.contains("test-session"));
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("ANALYSIS"));
        assertTrue(toString.contains("COMPLETED"));
        assertTrue(toString.contains("0.80"));
    }

    @Test
    void testToBuilder() {
        ReasoningStep original = ReasoningStep.builder().withSessionId("original-session").withStepNumber(1)
                .withReasoning("Original reasoning").withStepType(ReasoningStepType.ANALYSIS).build();

        ReasoningStep modified = original.toBuilder().withStepType(ReasoningStepType.EXECUTION)
                .withStatus(ReasoningStepStatus.IN_PROGRESS).build();

        // Original should be unchanged
        assertEquals(ReasoningStepType.ANALYSIS, original.getStepType());
        assertEquals(ReasoningStepStatus.COMPLETED, original.getStatus()); // Default status

        // Modified should have new values
        assertEquals(ReasoningStepType.EXECUTION, modified.getStepType());
        assertEquals(ReasoningStepStatus.IN_PROGRESS, modified.getStatus());

        // Other fields should be preserved
        assertEquals(original.getSessionId(), modified.getSessionId());
        assertEquals(original.getStepNumber(), modified.getStepNumber());
        assertEquals(original.getReasoning(), modified.getReasoning());
    }
}
