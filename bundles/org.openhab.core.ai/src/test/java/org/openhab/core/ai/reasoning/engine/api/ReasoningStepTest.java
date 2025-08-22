package org.openhab.core.ai.reasoning.engine.api;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ReasoningStep} and its nested Builder.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ReasoningStepTest {

    @Test
    void testBuilderCreation() {
        ReasoningStep step = ReasoningStep.builder().withReasoning("Test step").build();

        assertNotNull(step.getSessionId());
        assertTrue(step.getSessionId().startsWith("step-"));
        assertEquals("Test step", step.getReasoning());
        assertEquals(1, step.getStepNumber());
        assertTrue(step.getToolCalls().isEmpty());
        assertEquals(1.0, step.getConfidence());
        assertTrue(step.isComplete());
        assertNotNull(step.getStartTime());
        assertNotNull(step.getEndTime());
        assertNull(step.getError());
    }

    @Test
    void testCustomValues() {
        List<org.openhab.core.ai.common.context.ExecutionContext> toolCalls = new ArrayList<>();
        // Note: ExecutionContext is an interface, so we'd need a concrete implementation
        // For testing purposes, we'll use null and test the null handling

        ReasoningStep step = ReasoningStep.builder().withSessionId("custom-step-1").withReasoning("Custom step")
                .withStepNumber(5).withToolCalls(toolCalls).withConfidence(0.8).withComplete(false)
                .withStartTime(java.time.Instant.now()).withEndTime(java.time.Instant.now()).withError("Test error")
                .build();

        assertEquals("custom-step-1", step.getSessionId());
        assertEquals("Custom step", step.getReasoning());
        assertEquals(5, step.getStepNumber());
        assertNotNull(step.getToolCalls());
        assertEquals(0.8, step.getConfidence());
        assertFalse(step.isComplete());
        assertNotNull(step.getStartTime());
        assertNotNull(step.getEndTime());
        assertEquals("Test error", step.getError());
    }

    @Test
    void testToBuilder() {
        ReasoningStep original = ReasoningStep.builder().withSessionId("original-step")
                .withReasoning("Original reasoning").withStepNumber(3).withConfidence(0.7).withComplete(false)
                .withError("Original error").build();

        ReasoningStep modified = original.toBuilder().withSessionId("modified-step").withReasoning("Modified reasoning")
                .withComplete(true).build();

        assertEquals("modified-step", modified.getSessionId());
        assertEquals("Modified reasoning", modified.getReasoning());
        assertEquals(3, modified.getStepNumber()); // Preserved
        assertEquals(0.7, modified.getConfidence()); // Preserved
        assertTrue(modified.isComplete()); // Modified
        assertEquals("Original error", modified.getError()); // Preserved
    }

    @Test
    void testValidation() {
        // Test invalid session ID (blank)
        assertThrows(IllegalArgumentException.class, () -> {
            ReasoningStep.builder().withSessionId("").withReasoning("Valid reasoning").build();
        });

        // Test null session ID
        assertThrows(NullPointerException.class, () -> {
            ReasoningStep.builder().withSessionId(null).withReasoning("Valid reasoning").build();
        });

        // Test invalid reasoning (blank)
        assertThrows(IllegalArgumentException.class, () -> {
            ReasoningStep.builder().withSessionId("valid-step").withReasoning("").build();
        });

        // Test null reasoning
        assertThrows(NullPointerException.class, () -> {
            ReasoningStep.builder().withSessionId("valid-step").withReasoning(null).build();
        });

        // Test invalid confidence (negative)
        assertThrows(IllegalArgumentException.class, () -> {
            ReasoningStep.builder().withSessionId("valid-step").withReasoning("Valid reasoning").withConfidence(-0.1)
                    .build();
        });

        // Test invalid confidence (greater than 1.0)
        assertThrows(IllegalArgumentException.class, () -> {
            ReasoningStep.builder().withSessionId("valid-step").withReasoning("Valid reasoning").withConfidence(1.1)
                    .build();
        });

        // Test invalid step number (zero)
        assertThrows(IllegalArgumentException.class, () -> {
            ReasoningStep.builder().withSessionId("valid-step").withReasoning("Valid reasoning").withStepNumber(0)
                    .build();
        });
    }

    @Test
    void testValidBoundaryValues() {
        // Test boundary values that should be valid
        ReasoningStep step = ReasoningStep.builder().withSessionId("a").withReasoning("b").withConfidence(0.0)
                .withStepNumber(1).build();

        assertEquals("a", step.getSessionId());
        assertEquals("b", step.getReasoning());
        assertEquals(0.0, step.getConfidence());
        assertEquals(1, step.getStepNumber());
    }

    @Test
    void testImmutability() {
        List<org.openhab.core.ai.common.context.ExecutionContext> originalToolCalls = new ArrayList<>();
        // Note: ExecutionContext is an interface, so we'd need a concrete implementation
        // For testing purposes, we'll test with null toolCalls

        ReasoningStep step = ReasoningStep.builder().withToolCalls(null).withReasoning("Test").build();

        // The step should handle null toolCalls gracefully
        assertTrue(step.getToolCalls().isEmpty());
    }

    @Test
    void testNullHandling() {
        // Test null values that should be allowed
        ReasoningStep step = ReasoningStep.builder().withReasoning("Test").withToolCalls(null).withError(null).build();

        assertNull(step.getError());
        assertTrue(step.getToolCalls().isEmpty()); // null toolCalls should result in empty list
    }

    @Test
    void testEquality() {
        ReasoningStep step1 = ReasoningStep.builder().withSessionId("step-1").withReasoning("Test step").build();

        ReasoningStep step2 = ReasoningStep.builder().withSessionId("step-1").withReasoning("Test step").build();

        // Note: Since ReasoningStep doesn't override equals/hashCode,
        // these will not be equal. This test verifies the current behavior.
        assertNotEquals(step1, step2);
    }

    @Test
    void testToString() {
        ReasoningStep step = ReasoningStep.builder().withSessionId("test-step").withReasoning("Test reasoning").build();

        String toString = step.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ReasoningStep"));
    }

    @Test
    void testErrorHandling() {
        ReasoningStep errorStep = ReasoningStep.builder().withSessionId("error-step").withReasoning("Failed step")
                .withComplete(false).withError("Something went wrong").build();

        assertFalse(errorStep.isComplete());
        assertEquals("Something went wrong", errorStep.getError());
    }

    @Test
    void testBuilderReuse() {
        // Test that builder methods return the same builder instance for chaining
        ReasoningStep.Builder builder = ReasoningStep.builder();
        assertSame(builder, builder.withSessionId("test"));
        assertSame(builder, builder.withReasoning("test"));
        assertSame(builder, builder.withStepNumber(1));
        assertSame(builder, builder.withConfidence(0.5));
        assertSame(builder, builder.withComplete(true));
        assertSame(builder, builder.withToolCalls(null));
        assertSame(builder, builder.withError("test"));
    }
}
