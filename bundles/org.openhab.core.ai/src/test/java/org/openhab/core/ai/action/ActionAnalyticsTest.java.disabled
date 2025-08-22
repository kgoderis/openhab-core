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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test cases for {@link ActionAnalytics}.
 *
 * @author Karel Goderis - Initial Contribution
 */
class ActionAnalyticsTest {

    @Test
    void testBuilderCreation() {
        ActionAnalytics analytics = ActionAnalytics.builder().withActionId("test-action").withTotalExecutions(100)
                .withSuccessfulExecutions(90).withFailedExecutions(10).withAverageExecutionTime(Duration.ofMillis(500))
                .withMinExecutionTime(Duration.ofMillis(100)).withMaxExecutionTime(Duration.ofMillis(1000))
                .withExecutionByAgent(Map.of("agent1", 50L, "agent2", 50L))
                .withExecutionByTimeOfDay(Map.of("09:00", 30L, "14:00", 70L))
                .withExecutionByDayOfWeek(Map.of("Monday", 20L, "Tuesday", 80L))
                .withCommonErrorMessages(List.of("Error 1", "Error 2"))
                .withParameterUsage(Map.of("param1", 60L, "param2", 40L))
                .withFirstExecution(Instant.now().minusSeconds(3600)).withLastExecution(Instant.now())
                .withTotalExecutionTime(Duration.ofSeconds(50000)).build();

        assertEquals("test-action", analytics.getActionId());
        assertEquals(100, analytics.getTotalExecutions());
        assertEquals(90, analytics.getSuccessfulExecutions());
        assertEquals(10, analytics.getFailedExecutions());
        assertEquals(0.9, analytics.getSuccessRate(), 0.001);
        assertEquals(0.1, analytics.getFailureRate(), 0.001);
        assertEquals(Duration.ofMillis(500), analytics.getAverageExecutionTime());
        assertEquals(Duration.ofMillis(100), analytics.getMinExecutionTime());
        assertEquals(Duration.ofMillis(1000), analytics.getMaxExecutionTime());
        assertEquals(Map.of("agent1", 50L, "agent2", 50L), analytics.getExecutionByAgent());
        assertEquals(Map.of("09:00", 30L, "14:00", 70L), analytics.getExecutionByTimeOfDay());
        assertEquals(Map.of("Monday", 20L, "Tuesday", 80L), analytics.getExecutionByDayOfWeek());
        assertEquals(List.of("Error 1", "Error 2"), analytics.getCommonErrorMessages());
        assertEquals(Map.of("param1", 60L, "param2", 40L), analytics.getParameterUsage());
        assertEquals("agent1", analytics.getMostActiveAgent());
        assertEquals("14:00", analytics.getPeakUsageTime());
        assertEquals("Error 1", analytics.getMostCommonError());
        assertEquals("param1", analytics.getMostUsedParameter());
    }

    @Test
    void testDefaultValues() {
        ActionAnalytics analytics = ActionAnalytics.builder().withActionId("test-action").build();

        assertEquals("test-action", analytics.getActionId());
        assertEquals(0, analytics.getTotalExecutions());
        assertEquals(0, analytics.getSuccessfulExecutions());
        assertEquals(0, analytics.getFailedExecutions());
        assertEquals(0.0, analytics.getSuccessRate(), 0.001);
        assertEquals(0.0, analytics.getFailureRate(), 0.001);
        assertEquals(Duration.ZERO, analytics.getAverageExecutionTime());
        assertEquals(Duration.ZERO, analytics.getMinExecutionTime());
        assertEquals(Duration.ZERO, analytics.getMaxExecutionTime());
        assertTrue(analytics.getExecutionByAgent().isEmpty());
        assertTrue(analytics.getExecutionByTimeOfDay().isEmpty());
        assertTrue(analytics.getExecutionByDayOfWeek().isEmpty());
        assertTrue(analytics.getCommonErrorMessages().isEmpty());
        assertTrue(analytics.getParameterUsage().isEmpty());
        assertNull(analytics.getMostActiveAgent());
        assertNull(analytics.getPeakUsageTime());
        assertNull(analytics.getMostCommonError());
        assertNull(analytics.getMostUsedParameter());
    }

    @Test
    void testToBuilder() {
        ActionAnalytics original = ActionAnalytics.builder().withActionId("test-action").withTotalExecutions(100)
                .withSuccessfulExecutions(90).withFailedExecutions(10).withAverageExecutionTime(Duration.ofMillis(500))
                .build();

        ActionAnalytics modified = original.toBuilder().withTotalExecutions(200).withSuccessfulExecutions(180)
                .withFailedExecutions(20).build();

        assertEquals("test-action", modified.getActionId());
        assertEquals(200, modified.getTotalExecutions());
        assertEquals(180, modified.getSuccessfulExecutions());
        assertEquals(20, modified.getFailedExecutions());
        assertEquals(0.9, modified.getSuccessRate(), 0.001);
        assertEquals(0.1, modified.getFailureRate(), 0.001);
        assertEquals(Duration.ofMillis(500), modified.getAverageExecutionTime());

        // Original should remain unchanged
        assertEquals(100, original.getTotalExecutions());
        assertEquals(90, original.getSuccessfulExecutions());
        assertEquals(10, original.getFailedExecutions());
    }

    @Test
    void testValidation() {
        // Test blank actionId
        assertThrows(IllegalArgumentException.class, () -> {
            ActionAnalytics.builder().withActionId("").build();
        });

        // Test negative totalExecutions
        assertThrows(IllegalArgumentException.class, () -> {
            ActionAnalytics.builder().withActionId("test").withTotalExecutions(-1).build();
        });

        // Test negative successfulExecutions
        assertThrows(IllegalArgumentException.class, () -> {
            ActionAnalytics.builder().withActionId("test").withSuccessfulExecutions(-1).build();
        });

        // Test negative failedExecutions
        assertThrows(IllegalArgumentException.class, () -> {
            ActionAnalytics.builder().withActionId("test").withFailedExecutions(-1).build();
        });

        // Test totalExecutions != successfulExecutions + failedExecutions
        assertThrows(IllegalArgumentException.class, () -> {
            ActionAnalytics.builder().withActionId("test").withTotalExecutions(100).withSuccessfulExecutions(90)
                    .withFailedExecutions(5).build();
        });

        // Test minExecutionTime > maxExecutionTime
        assertThrows(IllegalArgumentException.class, () -> {
            ActionAnalytics.builder().withActionId("test").withMinExecutionTime(Duration.ofMillis(1000))
                    .withMaxExecutionTime(Duration.ofMillis(500)).build();
        });

        // Test firstExecution after lastExecution
        assertThrows(IllegalArgumentException.class, () -> {
            ActionAnalytics.builder().withActionId("test").withFirstExecution(Instant.now())
                    .withLastExecution(Instant.now().minusSeconds(3600)).build();
        });
    }

    @Test
    void testImmutability() {
        Map<String, Long> originalAgentMap = Map.of("agent1", 50L);
        List<String> originalErrorList = List.of("Error 1");

        ActionAnalytics analytics = ActionAnalytics.builder().withActionId("test-action")
                .withExecutionByAgent(originalAgentMap).withCommonErrorMessages(originalErrorList).build();

        // Verify collections are immutable
        assertThrows(UnsupportedOperationException.class, () -> {
            analytics.getExecutionByAgent().put("agent2", 50L);
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            analytics.getCommonErrorMessages().add("Error 2");
        });
    }

    @Test
    void testNullHandling() {
        // Test null actionId
        assertThrows(NullPointerException.class, () -> {
            ActionAnalytics.builder().withActionId(null).build();
        });

        // Test null Duration parameters
        assertThrows(NullPointerException.class, () -> {
            ActionAnalytics.builder().withActionId("test").withAverageExecutionTime(null).build();
        });

        // Test null Map parameters
        assertThrows(NullPointerException.class, () -> {
            ActionAnalytics.builder().withActionId("test").withExecutionByAgent(null).build();
        });

        // Test null List parameters
        assertThrows(NullPointerException.class, () -> {
            ActionAnalytics.builder().withActionId("test").withCommonErrorMessages(null).build();
        });

        // Test null Instant parameters
        assertThrows(NullPointerException.class, () -> {
            ActionAnalytics.builder().withActionId("test").withFirstExecution(null).build();
        });
    }

    @Test
    void testEqualityAndHashCode() {
        ActionAnalytics analytics1 = ActionAnalytics.builder().withActionId("test-action").withTotalExecutions(100)
                .withSuccessfulExecutions(90).withFailedExecutions(10).build();

        ActionAnalytics analytics2 = ActionAnalytics.builder().withActionId("test-action").withTotalExecutions(100)
                .withSuccessfulExecutions(90).withFailedExecutions(10).build();

        assertEquals(analytics1, analytics2);
        assertEquals(analytics1.hashCode(), analytics2.hashCode());
    }

    @Test
    void testToString() {
        ActionAnalytics analytics = ActionAnalytics.builder().withActionId("test-action").withTotalExecutions(100)
                .withSuccessfulExecutions(90).withFailedExecutions(10).build();

        String toString = analytics.toString();
        assertTrue(toString.contains("test-action"));
        assertTrue(toString.contains("100"));
        assertTrue(toString.contains("90.00%"));
    }

    @Test
    void testBuilderReuse() {
        ActionAnalytics.Builder builder = ActionAnalytics.builder().withActionId("test-action").withTotalExecutions(100)
                .withSuccessfulExecutions(90).withFailedExecutions(10);

        ActionAnalytics analytics1 = builder.build();
        ActionAnalytics analytics2 = builder.build();

        assertEquals(analytics1, analytics2);
    }
}
