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
package org.openhab.core.ai.reasoning.api;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test cases for {@link MultiStepReasoningResult}.
 *
 * @author Karel Goderis - Initial Contribution
 */
class MultiStepReasoningResultTest {

    @Test
    void testBuilderCreation() {
        MultiStepReasoningResult result = MultiStepReasoningResult.builder().withReasoningId("reasoning-123")
                .withSessionId("session-456").withSuccessful(true).withCompleted(true)
                .withFinalAnswer("The answer is 42").withFinalReasoning("I calculated this step by step")
                .withSteps(
                        List.of(new MultiStepReasoningResult.ReasoningStep(1, "Step 1", "Calculate", "Result", 100L)))
                .withToolCalls(List.of()).withMetadata(Map.of("key", "value")).withConfidence(0.95)
                .withTotalDurationMs(500L).withStartTime(Instant.now()).withEndTime(Instant.now().plusSeconds(1))
                .build();

        assertEquals("reasoning-123", result.getReasoningId());
        assertEquals("session-456", result.getSessionId());
        assertTrue(result.isSuccessful());
        assertTrue(result.isCompleted());
        assertEquals("The answer is 42", result.getFinalAnswer());
        assertEquals("I calculated this step by step", result.getFinalReasoning());
        assertEquals(1, result.getSteps().size());
        assertEquals(0, result.getToolCalls().size());
        assertEquals("value", result.getMetadata().get("key"));
        assertEquals(0.95, result.getConfidence(), 0.001);
        assertEquals(500L, result.getTotalDurationMs());
    }

    @Test
    void testDefaultValues() {
        MultiStepReasoningResult result = MultiStepReasoningResult.builder().withReasoningId("reasoning-123")
                .withSessionId("session-456").build();

        assertFalse(result.isSuccessful());
        assertFalse(result.isCompleted());
        assertEquals("", result.getFinalAnswer());
        assertEquals("", result.getFinalReasoning());
        assertTrue(result.getSteps().isEmpty());
        assertTrue(result.getToolCalls().isEmpty());
        assertTrue(result.getMetadata().isEmpty());
        assertEquals(1.0, result.getConfidence(), 0.001);
        assertEquals(0L, result.getTotalDurationMs());
        assertNotNull(result.getStartTime());
        assertNotNull(result.getEndTime());
    }

    @Test
    void testToBuilder() {
        MultiStepReasoningResult original = MultiStepReasoningResult.builder().withReasoningId("reasoning-123")
                .withSessionId("session-456").withSuccessful(true).withFinalAnswer("Original answer")
                .withConfidence(0.8).build();

        MultiStepReasoningResult modified = original.toBuilder().withSuccessful(false)
                .withFinalAnswer("Modified answer").withConfidence(0.9).build();

        assertEquals("reasoning-123", modified.getReasoningId());
        assertEquals("session-456", modified.getSessionId());
        assertFalse(modified.isSuccessful());
        assertEquals("Modified answer", modified.getFinalAnswer());
        assertEquals(0.9, modified.getConfidence(), 0.001);

        // Original should remain unchanged
        assertTrue(original.isSuccessful());
        assertEquals("Original answer", original.getFinalAnswer());
        assertEquals(0.8, original.getConfidence(), 0.001);
    }

    @Test
    void testValidation() {
        // Test blank reasoningId
        assertThrows(IllegalArgumentException.class, () -> {
            MultiStepReasoningResult.builder().withReasoningId("").withSessionId("session-456").build();
        });

        // Test blank sessionId
        assertThrows(IllegalArgumentException.class, () -> {
            MultiStepReasoningResult.builder().withReasoningId("reasoning-123").withSessionId("").build();
        });

        // Test confidence < 0.0
        assertThrows(IllegalArgumentException.class, () -> {
            MultiStepReasoningResult.builder().withReasoningId("reasoning-123").withSessionId("session-456")
                    .withConfidence(-0.1).build();
        });

        // Test confidence > 1.0
        assertThrows(IllegalArgumentException.class, () -> {
            MultiStepReasoningResult.builder().withReasoningId("reasoning-123").withSessionId("session-456")
                    .withConfidence(1.1).build();
        });

        // Test negative totalDurationMs
        assertThrows(IllegalArgumentException.class, () -> {
            MultiStepReasoningResult.builder().withReasoningId("reasoning-123").withSessionId("session-456")
                    .withTotalDurationMs(-1L).build();
        });

        // Test endTime before startTime
        assertThrows(IllegalArgumentException.class, () -> {
            Instant startTime = Instant.now();
            Instant endTime = startTime.minusSeconds(1);
            MultiStepReasoningResult.builder().withReasoningId("reasoning-123").withSessionId("session-456")
                    .withStartTime(startTime).withEndTime(endTime).build();
        });
    }

    @Test
    void testNullHandling() {
        // Test null reasoningId
        assertThrows(NullPointerException.class, () -> {
            MultiStepReasoningResult.builder().withReasoningId(null).withSessionId("session-456").build();
        });

        // Test null sessionId
        assertThrows(NullPointerException.class, () -> {
            MultiStepReasoningResult.builder().withReasoningId("reasoning-123").withSessionId(null).build();
        });

        // Test null finalAnswer
        assertThrows(NullPointerException.class, () -> {
            MultiStepReasoningResult.builder().withReasoningId("reasoning-123").withSessionId("session-456")
                    .withFinalAnswer(null).build();
        });

        // Test null finalReasoning
        assertThrows(NullPointerException.class, () -> {
            MultiStepReasoningResult.builder().withReasoningId("reasoning-123").withSessionId("session-456")
                    .withFinalReasoning(null).build();
        });

        // Test null steps
        assertThrows(NullPointerException.class, () -> {
            MultiStepReasoningResult.builder().withReasoningId("reasoning-123").withSessionId("session-456")
                    .withSteps(null).build();
        });

        // Test null toolCalls
        assertThrows(NullPointerException.class, () -> {
            MultiStepReasoningResult.builder().withReasoningId("reasoning-123").withSessionId("session-456")
                    .withToolCalls(null).build();
        });

        // Test null metadata
        assertThrows(NullPointerException.class, () -> {
            MultiStepReasoningResult.builder().withReasoningId("reasoning-123").withSessionId("session-456")
                    .withMetadata(null).build();
        });

        // Test null startTime
        assertThrows(NullPointerException.class, () -> {
            MultiStepReasoningResult.builder().withReasoningId("reasoning-123").withSessionId("session-456")
                    .withStartTime(null).build();
        });

        // Test null endTime
        assertThrows(NullPointerException.class, () -> {
            MultiStepReasoningResult.builder().withReasoningId("reasoning-123").withSessionId("session-456")
                    .withEndTime(null).build();
        });
    }

    @Test
    void testImmutability() {
        List<MultiStepReasoningResult.ReasoningStep> steps = List
                .of(new MultiStepReasoningResult.ReasoningStep(1, "Step 1", "Calculate", "Result", 100L));
        Map<String, Object> metadata = Map.of("key", "value");
        List<org.openhab.core.ai.action.api.ActionResult> toolCalls = List.of();

        MultiStepReasoningResult result = MultiStepReasoningResult.builder().withReasoningId("reasoning-123")
                .withSessionId("session-456").withSteps(steps).withMetadata(metadata).withToolCalls(toolCalls).build();

        // Verify collections are immutable
        assertThrows(UnsupportedOperationException.class, () -> {
            result.getSteps().add(new MultiStepReasoningResult.ReasoningStep(2, "Step 2", "Calculate", "Result", 100L));
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            result.getMetadata().put("newKey", "newValue");
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            result.getToolCalls().add(null);
        });
    }

    @Test
    void testEqualityAndHashCode() {
        MultiStepReasoningResult result1 = MultiStepReasoningResult.builder().withReasoningId("reasoning-123")
                .withSessionId("session-456").withSuccessful(true).withFinalAnswer("Answer").build();

        MultiStepReasoningResult result2 = MultiStepReasoningResult.builder().withReasoningId("reasoning-123")
                .withSessionId("session-456").withSuccessful(true).withFinalAnswer("Answer").build();

        MultiStepReasoningResult result3 = MultiStepReasoningResult.builder().withReasoningId("different-reasoning")
                .withSessionId("session-456").withSuccessful(true).withFinalAnswer("Answer").build();

        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
        assertNotEquals(result1, result3);
        assertNotEquals(result1.hashCode(), result3.hashCode());
    }

    @Test
    void testBuilderReuse() {
        MultiStepReasoningResult.Builder builder = MultiStepReasoningResult.builder().withReasoningId("reasoning-123")
                .withSessionId("session-456");

        MultiStepReasoningResult result1 = builder.build();
        assertNotNull(result1);

        // Reset and build another object
        MultiStepReasoningResult result2 = builder.withReasoningId("new-reasoning").withSessionId("new-session")
                .withSuccessful(true).build();

        assertNotNull(result2);
        assertNotEquals(result1.getReasoningId(), result2.getReasoningId());
        assertNotEquals(result1.getSessionId(), result2.getSessionId());
        assertNotEquals(result1.isSuccessful(), result2.isSuccessful());
    }

    @Test
    void testWithError() {
        MultiStepReasoningResult result = MultiStepReasoningResult.builder().withReasoningId("reasoning-123")
                .withSessionId("session-456").withError("Something went wrong").build();

        assertEquals("Something went wrong", result.getErrorMessage());
        assertFalse(result.isSuccessful());
    }

    @Test
    void testReasoningStep() {
        MultiStepReasoningResult.ReasoningStep step = new MultiStepReasoningResult.ReasoningStep(1, "Test step",
                "Test action", "Test result", 150L);

        assertEquals(1, step.getStepNumber());
        assertEquals("Test step", step.getDescription());
        assertEquals("Test action", step.getAction());
        assertEquals("Test result", step.getResult());
        assertEquals(150L, step.getDurationMs());
    }
}
