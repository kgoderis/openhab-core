package org.openhab.core.ai.common.metrics;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for MemoryPerformanceMetrics.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class MemoryPerformanceMetricsTest {

    @Test
    void testBuilder() {
        Instant now = Instant.now();
        MemoryPerformanceMetrics metrics = MemoryPerformanceMetrics.builder().withTotalCount(100).withSuccessCount(90)
                .withFailureCount(10).withTotalDurationMs(5000).withLastExecution(now).withTotalStores(30)
                .withTotalRetrievals(40).withTotalConsolidations(20).withTotalPatternRecognitions(10)
                .withShortTermMemoryCount(50).withLongTermMemoryCount(30).withPatternCount(20).build();

        assertEquals(100, metrics.getTotalOperations());
        assertEquals(90, metrics.getSuccessfulOperations());
        assertEquals(10, metrics.getFailedOperations());
        assertEquals(5000, metrics.getTotalProcessingTime());
        assertEquals(now, metrics.getLastOperationTime());
        assertEquals(30, metrics.getTotalStores());
        assertEquals(40, metrics.getTotalRetrievals());
        assertEquals(20, metrics.getTotalConsolidations());
        assertEquals(10, metrics.getTotalPatternRecognitions());
        assertEquals(50, metrics.getShortTermMemoryCount());
        assertEquals(30, metrics.getLongTermMemoryCount());
        assertEquals(20, metrics.getPatternCount());
    }

    @Test
    void testToBuilder() {
        MemoryPerformanceMetrics original = MemoryPerformanceMetrics.builder().withTotalCount(100).withSuccessCount(90)
                .withTotalStores(30).withTotalRetrievals(40).withShortTermMemoryCount(50).build();

        MemoryPerformanceMetrics copy = original.toBuilder().withTotalCount(200).withTotalStores(60).build();

        assertEquals(200, copy.getTotalOperations());
        assertEquals(90, copy.getSuccessfulOperations()); // unchanged
        assertEquals(60, copy.getTotalStores());
        assertEquals(40, copy.getTotalRetrievals()); // unchanged
        assertEquals(50, copy.getShortTermMemoryCount()); // unchanged
    }

    @Test
    void testValidation() {
        // Test negative values
        assertThrows(IllegalArgumentException.class, () -> {
            MemoryPerformanceMetrics.builder().withTotalStores(-1).build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            MemoryPerformanceMetrics.builder().withTotalRetrievals(-1).build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            MemoryPerformanceMetrics.builder().withShortTermMemoryCount(-1).build();
        });
    }

    @Test
    void testDefaultValues() {
        MemoryPerformanceMetrics metrics = MemoryPerformanceMetrics.builder().build();

        assertEquals(0, metrics.getTotalOperations());
        assertEquals(0, metrics.getSuccessfulOperations());
        assertEquals(0, metrics.getFailedOperations());
        assertEquals(0, metrics.getTotalProcessingTime());
        assertEquals(0, metrics.getTotalStores());
        assertEquals(0, metrics.getTotalRetrievals());
        assertEquals(0, metrics.getTotalConsolidations());
        assertEquals(0, metrics.getTotalPatternRecognitions());
        assertEquals(0, metrics.getShortTermMemoryCount());
        assertEquals(0, metrics.getLongTermMemoryCount());
        assertEquals(0, metrics.getPatternCount());
    }

    @Test
    void testAverageResponseTime() {
        MemoryPerformanceMetrics metrics = MemoryPerformanceMetrics.builder().withTotalCount(100)
                .withTotalDurationMs(5000).build();

        assertEquals(50.0, metrics.getAverageResponseTime(), 0.001);
    }

    @Test
    void testSuccessRate() {
        MemoryPerformanceMetrics metrics = MemoryPerformanceMetrics.builder().withTotalCount(100).withSuccessCount(90)
                .withFailureCount(10).build();

        assertEquals(90.0, metrics.getSuccessRate(), 0.001);
    }

    @Test
    void testFailureRate() {
        MemoryPerformanceMetrics metrics = MemoryPerformanceMetrics.builder().withTotalCount(100).withSuccessCount(90)
                .withFailureCount(10).build();

        assertEquals(10.0, metrics.getFailureRate(), 0.001);
    }
}
