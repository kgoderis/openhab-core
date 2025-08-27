package org.openhab.core.ai.common.monitoring.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for capability interfaces.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class CapabilityInterfacesTest {

    /**
     * Test implementation of CountsMetrics for testing.
     */
    private static class TestCountsMetrics implements CountsMetrics {
        private final long total;
        private final long success;
        private final long failure;

        TestCountsMetrics(long total, long success, long failure) {
            this.total = total;
            this.success = success;
            this.failure = failure;
        }

        @Override
        public long total() {
            return total;
        }

        @Override
        public long success() {
            return success;
        }

        @Override
        public long failure() {
            return failure;
        }
    }

    /**
     * Test implementation of LatencyMetrics for testing.
     */
    private static class TestLatencyMetrics implements LatencyMetrics {
        private final long totalDurationNanos;

        TestLatencyMetrics(long totalDurationNanos) {
            this.totalDurationNanos = totalDurationNanos;
        }

        @Override
        public long totalDurationNanos() {
            return totalDurationNanos;
        }
    }

    @Test
    void testCountsMetricsSuccessRateWithOperations() {
        // Given
        CountsMetrics metrics = new TestCountsMetrics(100, 80, 20);

        // When
        double successRate = metrics.successRate();

        // Then
        assertEquals(0.8, successRate, 0.001);
    }

    @Test
    void testCountsMetricsSuccessRateWithAllSuccess() {
        // Given
        CountsMetrics metrics = new TestCountsMetrics(50, 50, 0);

        // When
        double successRate = metrics.successRate();

        // Then
        assertEquals(1.0, successRate, 0.001);
    }

    @Test
    void testCountsMetricsSuccessRateWithAllFailures() {
        // Given
        CountsMetrics metrics = new TestCountsMetrics(30, 0, 30);

        // When
        double successRate = metrics.successRate();

        // Then
        assertEquals(0.0, successRate, 0.001);
    }

    @Test
    void testCountsMetricsSuccessRateWithZeroOperations() {
        // Given
        CountsMetrics metrics = new TestCountsMetrics(0, 0, 0);

        // When
        double successRate = metrics.successRate();

        // Then
        assertEquals(0.0, successRate, 0.001);
    }

    @Test
    void testCountsMetricsBasicOperations() {
        // Given
        CountsMetrics metrics = new TestCountsMetrics(100, 75, 25);

        // When & Then
        assertEquals(100, metrics.total());
        assertEquals(75, metrics.success());
        assertEquals(25, metrics.failure());
    }

    @Test
    void testLatencyMetricsAverageMsWithOperations() {
        // Given
        LatencyMetrics metrics = new TestLatencyMetrics(1000000000L); // 1 second in nanos

        // When
        double averageMs = metrics.averageMs(10); // 10 operations

        // Then
        assertEquals(100.0, averageMs, 0.001); // 100ms average
    }

    @Test
    void testLatencyMetricsAverageMsWithZeroOperations() {
        // Given
        LatencyMetrics metrics = new TestLatencyMetrics(1000000000L);

        // When
        double averageMs = metrics.averageMs(0);

        // Then
        assertEquals(0.0, averageMs, 0.001);
    }

    @Test
    void testLatencyMetricsTotalDurationMs() {
        // Given
        LatencyMetrics metrics = new TestLatencyMetrics(1500000000L); // 1.5 seconds in nanos

        // When
        double totalMs = metrics.totalDurationMs();

        // Then
        assertEquals(1500.0, totalMs, 0.001);
    }

    @Test
    void testLatencyMetricsBasicOperations() {
        // Given
        LatencyMetrics metrics = new TestLatencyMetrics(500000000L); // 500ms in nanos

        // When & Then
        assertEquals(500000000L, metrics.totalDurationNanos());
    }

    @Test
    void testLatencyMetricsPrecision() {
        // Given - 1 nanosecond
        LatencyMetrics metrics = new TestLatencyMetrics(1L);

        // When
        double totalMs = metrics.totalDurationMs();

        // Then
        assertEquals(0.000001, totalMs, 0.000001); // 1 nanosecond = 0.000001 ms
    }

    @Test
    void testCountsMetricsEdgeCases() {
        // Test with very large numbers
        CountsMetrics largeMetrics = new TestCountsMetrics(Long.MAX_VALUE, Long.MAX_VALUE - 1, 1);
        assertEquals(1.0, largeMetrics.successRate(), 0.001);

        // Test with single operation
        CountsMetrics singleMetrics = new TestCountsMetrics(1, 1, 0);
        assertEquals(1.0, singleMetrics.successRate(), 0.001);

        CountsMetrics singleFailureMetrics = new TestCountsMetrics(1, 0, 1);
        assertEquals(0.0, singleFailureMetrics.successRate(), 0.001);
    }

    @Test
    void testLatencyMetricsEdgeCases() {
        // Test with zero duration
        LatencyMetrics zeroMetrics = new TestLatencyMetrics(0L);
        assertEquals(0.0, zeroMetrics.averageMs(10), 0.001);
        assertEquals(0.0, zeroMetrics.totalDurationMs(), 0.001);

        // Test with very large duration
        LatencyMetrics largeMetrics = new TestLatencyMetrics(Long.MAX_VALUE);
        assertTrue(largeMetrics.totalDurationMs() > 0);
    }

    @Test
    void testCountsMetricsConsistency() {
        // Given
        CountsMetrics metrics = new TestCountsMetrics(100, 80, 20);

        // When & Then - success + failure should equal total
        assertEquals(metrics.total(), metrics.success() + metrics.failure());
    }

    @Test
    void testLatencyMetricsConsistency() {
        // Given
        LatencyMetrics metrics = new TestLatencyMetrics(1000000000L); // 1 second

        // When & Then - totalDurationMs should be consistent with totalDurationNanos
        double expectedMs = (double) metrics.totalDurationNanos() / 1_000_000.0;
        assertEquals(expectedMs, metrics.totalDurationMs(), 0.001);
    }
}
