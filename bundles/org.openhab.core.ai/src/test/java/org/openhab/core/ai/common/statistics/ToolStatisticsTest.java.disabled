package org.openhab.core.ai.common.statistics;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for ToolStatistics.
 *
 * @author Karel Goderis - Initial Contribution
 */
class ToolStatisticsTest {

    @Test
    void testBuilder() {
        Instant now = Instant.now();
        ToolStatistics stats = ToolStatistics.builder().withId("test-tool").withToolId("tool-123")
                .withTotalExecutions(100).withSuccessfulExecutions(90).withFailedExecutions(10)
                .withTotalExecutionTimeMs(5000).withAverageExecutionTimeMs(50).withMinExecutionTimeMs(10)
                .withMaxExecutionTimeMs(200).withLastExecutionTime(now).withLastSuccessTime(now)
                .withLastFailureTime(now).withLastError("Test error").build();

        assertEquals("test-tool", stats.getId());
        assertEquals("tool-123", stats.getToolId());
        assertEquals(100, stats.getTotalExecutions());
        assertEquals(90, stats.getSuccessfulExecutions());
        assertEquals(10, stats.getFailedExecutions());
        assertEquals(5000, stats.getTotalExecutionTimeMs());
        assertEquals(50, stats.getAverageExecutionTimeMs());
        assertEquals(10, stats.getMinExecutionTimeMs());
        assertEquals(200, stats.getMaxExecutionTimeMs());
        assertEquals(now, stats.getLastExecutionTime());
        assertEquals(now, stats.getLastSuccessTime());
        assertEquals(now, stats.getLastFailureTime());
        assertEquals("Test error", stats.getLastError());
        assertEquals(StatisticsType.EXECUTION, stats.getType());
    }

    @Test
    void testToBuilder() {
        ToolStatistics original = ToolStatistics.builder().withId("test-tool").withToolId("tool-123")
                .withTotalExecutions(100).withSuccessfulExecutions(90).build();

        ToolStatistics copy = original.toBuilder().withFailedExecutions(15).build();

        assertEquals("test-tool", copy.getId());
        assertEquals("tool-123", copy.getToolId());
        assertEquals(100, copy.getTotalExecutions());
        assertEquals(90, copy.getSuccessfulExecutions());
        assertEquals(15, copy.getFailedExecutions());
    }

    @Test
    void testSuccessRate() {
        ToolStatistics stats = ToolStatistics.builder().withId("test").withToolId("tool-123").withTotalExecutions(100)
                .withSuccessfulExecutions(90).withFailedExecutions(10).build();

        assertEquals(90.0, stats.getSuccessRate(), 0.01);
    }

    @Test
    void testSuccessRateZero() {
        ToolStatistics stats = ToolStatistics.builder().withId("test").withToolId("tool-123").withTotalExecutions(0)
                .withSuccessfulExecutions(0).withFailedExecutions(0).build();

        assertEquals(0.0, stats.getSuccessRate(), 0.01);
    }

    @Test
    void testFailureRate() {
        ToolStatistics stats = ToolStatistics.builder().withId("test").withToolId("tool-123").withTotalExecutions(100)
                .withSuccessfulExecutions(90).withFailedExecutions(10).build();

        assertEquals(10.0, stats.getFailureRate(), 0.01);
    }

    @Test
    void testValidation() {
        ToolStatistics.Builder builder = ToolStatistics.builder().withId("").withToolId("");

        assertFalse(builder.isValid());
        assertNotNull(builder.getValidationErrors());
        assertTrue(builder.getValidationErrors().contains("id cannot be blank"));
        assertTrue(builder.getValidationErrors().contains("toolId cannot be blank"));
    }

    @Test
    void testValidationNegativeValues() {
        ToolStatistics.Builder builder = ToolStatistics.builder().withId("test").withToolId("tool-123")
                .withTotalExecutions(-1).withSuccessfulExecutions(-1).withFailedExecutions(-1);

        assertFalse(builder.isValid());
        assertNotNull(builder.getValidationErrors());
        assertTrue(builder.getValidationErrors().contains("totalExecutions must be non-negative"));
        assertTrue(builder.getValidationErrors().contains("successfulExecutions must be non-negative"));
        assertTrue(builder.getValidationErrors().contains("failedExecutions must be non-negative"));
    }

    @Test
    void testValidationInconsistentCounts() {
        ToolStatistics.Builder builder = ToolStatistics.builder().withId("test").withToolId("tool-123")
                .withTotalExecutions(100).withSuccessfulExecutions(60).withFailedExecutions(50);

        assertFalse(builder.isValid());
        assertNotNull(builder.getValidationErrors());
        assertTrue(builder.getValidationErrors()
                .contains("successfulExecutions + failedExecutions cannot exceed totalExecutions"));
    }

    @Test
    void testEqualsAndHashCode() {
        ToolStatistics stats1 = ToolStatistics.builder().withId("test").withToolId("tool-123").withTotalExecutions(100)
                .build();

        ToolStatistics stats2 = ToolStatistics.builder().withId("test").withToolId("tool-123").withTotalExecutions(100)
                .build();

        assertEquals(stats1, stats2);
        assertEquals(stats1.hashCode(), stats2.hashCode());
    }

    @Test
    void testNotEquals() {
        ToolStatistics stats1 = ToolStatistics.builder().withId("test1").withToolId("tool-123").withTotalExecutions(100)
                .build();

        ToolStatistics stats2 = ToolStatistics.builder().withId("test2").withToolId("tool-123").withTotalExecutions(100)
                .build();

        assertNotEquals(stats1, stats2);
        assertNotEquals(stats1.hashCode(), stats2.hashCode());
    }

    @Test
    void testToString() {
        ToolStatistics stats = ToolStatistics.builder().withId("test").withToolId("tool-123").withTotalExecutions(100)
                .withSuccessfulExecutions(90).withFailedExecutions(10).withAverageExecutionTimeMs(50).build();

        String result = stats.toString();
        assertTrue(result.contains("test"));
        assertTrue(result.contains("tool-123"));
        assertTrue(result.contains("100"));
        assertTrue(result.contains("90"));
        assertTrue(result.contains("10"));
        assertTrue(result.contains("90.00%"));
        assertTrue(result.contains("50"));
    }

    @Test
    void testMetrics() {
        ToolStatistics stats = ToolStatistics.builder().withId("test").withToolId("tool-123").withTotalExecutions(100)
                .withSuccessfulExecutions(90).withFailedExecutions(10).withTotalExecutionTimeMs(5000)
                .withAverageExecutionTimeMs(50).build();

        assertEquals("tool-123", stats.getMetric("toolId"));
        assertEquals(100L, stats.getMetric("totalExecutions"));
        assertEquals(90L, stats.getMetric("successfulExecutions"));
        assertEquals(10L, stats.getMetric("failedExecutions"));
        assertEquals(5000L, stats.getMetric("totalExecutionTimeMs"));
        assertEquals(50L, stats.getMetric("averageExecutionTimeMs"));
        assertEquals(90.0, (Double) stats.getMetric("successRate"), 0.01);
        assertEquals(10.0, (Double) stats.getMetric("failureRate"), 0.01);
        assertTrue(stats.hasMetric("toolId"));
        assertFalse(stats.hasMetric("nonexistent"));
    }

    @Test
    void testReset() {
        ToolStatistics.Builder builder = ToolStatistics.builder().withId("test").withToolId("tool-123")
                .withTotalExecutions(100);

        builder.reset();

        ToolStatistics stats = builder.build();
        assertEquals("", stats.getId());
        assertEquals("", stats.getToolId());
        assertEquals(0, stats.getTotalExecutions());
    }
}
