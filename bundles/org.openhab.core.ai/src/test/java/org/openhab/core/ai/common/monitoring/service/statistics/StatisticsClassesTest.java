package org.openhab.core.ai.common.monitoring.service.statistics;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.ClientPerformanceSnapshot;

/**
 * Comprehensive unit tests for statistics classes.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class StatisticsClassesTest {

    @Test
    void testClientPerformanceStatisticsConstructor() {
        // Given
        List<ClientPerformanceSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofHours(1);
        long timestampMs = System.currentTimeMillis();

        // When
        ClientPerformanceStatistics statistics = new ClientPerformanceStatistics(snapshots, timeRange, timestampMs);

        // Then
        assertNotNull(statistics);
        assertEquals(snapshots, statistics.snapshots());
        assertEquals(timeRange, statistics.timeRange());
        assertEquals(timestampMs, statistics.timestampMs());
    }

    @Test
    void testClientPerformanceStatisticsCountsMetrics() {
        // Given
        List<ClientPerformanceSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofHours(1);
        ClientPerformanceStatistics statistics = new ClientPerformanceStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then
        assertEquals(0, statistics.total());
        assertEquals(0, statistics.success());
        assertEquals(0, statistics.failure());
        assertEquals(0.0, statistics.successRate(), 0.001);
    }

    @Test
    void testClientPerformanceStatisticsLatencyMetrics() {
        // Given
        List<ClientPerformanceSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofHours(1);
        ClientPerformanceStatistics statistics = new ClientPerformanceStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then
        assertEquals(0L, statistics.totalDurationNanos());
        assertEquals(0.0, statistics.averageMs(0), 0.001);
    }

    @Test
    void testClientPerformanceStatisticsTrendMetrics() {
        // Given
        List<ClientPerformanceSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofDays(1);
        ClientPerformanceStatistics statistics = new ClientPerformanceStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then
        assertEquals(0.0, statistics.trendPercentage(), 0.001);
        assertEquals("stable", statistics.trendDirection());
        assertEquals(0.0, statistics.changeRate(), 0.001);
    }

    @Test
    void testClientPerformanceStatisticsPercentileMetrics() {
        // Given
        List<ClientPerformanceSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofHours(1);
        ClientPerformanceStatistics statistics = new ClientPerformanceStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then
        assertEquals(0.0, statistics.percentile50(), 0.001);
        assertEquals(0.0, statistics.percentile90(), 0.001);
        assertEquals(0.0, statistics.percentile95(), 0.001);
        assertEquals(0.0, statistics.percentile99(), 0.001);
    }

    @Test
    void testClientPerformanceStatisticsFromSnapshots() {
        // Given
        List<ClientPerformanceSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofHours(1);

        // When
        ClientPerformanceStatistics statistics = ClientPerformanceStatistics.fromSnapshots(snapshots, timeRange);

        // Then
        assertNotNull(statistics);
        assertEquals(snapshots, statistics.snapshots());
        assertEquals(timeRange, statistics.timeRange());
        assertTrue(statistics.timestampMs() > 0);
    }

    @Test
    void testClientPerformanceStatisticsFromClientData() {
        // Given
        long totalRequests = 100;
        long successfulRequests = 80;
        long failedRequests = 20;
        long totalDurationNanos = 1000000000L; // 1 second
        Duration timeRange = Duration.ofHours(1);

        // When
        ClientPerformanceStatistics statistics = ClientPerformanceStatistics.fromClientData(totalRequests,
                successfulRequests, failedRequests, totalDurationNanos, timeRange);

        // Then
        assertNotNull(statistics);
        assertEquals(timeRange, statistics.timeRange());
        assertTrue(statistics.timestampMs() > 0);
    }

    @Test
    void testStatisticsSnapshotInterface() {
        // Given
        List<ClientPerformanceSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofHours(1);
        ClientPerformanceStatistics statistics = new ClientPerformanceStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then
        assertTrue(statistics instanceof CountsMetrics);
        assertTrue(statistics instanceof LatencyMetrics);
        assertTrue(statistics instanceof TrendMetrics);
        assertTrue(statistics instanceof PercentileMetrics);
    }

    @Test
    void testStatisticsWithEmptySnapshots() {
        // Given
        List<ClientPerformanceSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofHours(1);
        ClientPerformanceStatistics statistics = new ClientPerformanceStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then
        assertEquals(0, statistics.total());
        assertEquals(0, statistics.success());
        assertEquals(0, statistics.failure());
        assertEquals(0.0, statistics.successRate(), 0.001);
        assertEquals(0L, statistics.totalDurationNanos());
        assertEquals(0.0, statistics.averageMs(0), 0.001);
        assertEquals(0.0, statistics.trendPercentage(), 0.001);
        assertEquals("stable", statistics.trendDirection());
        assertEquals(0.0, statistics.changeRate(), 0.001);
        assertEquals(0.0, statistics.percentile50(), 0.001);
        assertEquals(0.0, statistics.percentile90(), 0.001);
        assertEquals(0.0, statistics.percentile95(), 0.001);
        assertEquals(0.0, statistics.percentile99(), 0.001);
    }

    @Test
    void testStatisticsRecordEquality() {
        // Given
        List<ClientPerformanceSnapshot> snapshots1 = List.of();
        List<ClientPerformanceSnapshot> snapshots2 = List.of();
        Duration timeRange = Duration.ofHours(1);
        long timestampMs = System.currentTimeMillis();

        ClientPerformanceStatistics statistics1 = new ClientPerformanceStatistics(snapshots1, timeRange, timestampMs);
        ClientPerformanceStatistics statistics2 = new ClientPerformanceStatistics(snapshots2, timeRange, timestampMs);

        // When & Then
        assertEquals(statistics1, statistics2);
        assertEquals(statistics1.hashCode(), statistics2.hashCode());
    }

    @Test
    void testStatisticsRecordInequality() {
        // Given
        List<ClientPerformanceSnapshot> snapshots1 = List.of();
        List<ClientPerformanceSnapshot> snapshots2 = List.of();
        Duration timeRange1 = Duration.ofHours(1);
        Duration timeRange2 = Duration.ofHours(2);
        long timestampMs = System.currentTimeMillis();

        ClientPerformanceStatistics statistics1 = new ClientPerformanceStatistics(snapshots1, timeRange1, timestampMs);
        ClientPerformanceStatistics statistics2 = new ClientPerformanceStatistics(snapshots2, timeRange2, timestampMs);

        // When & Then
        assertNotEquals(statistics1, statistics2);
        assertNotEquals(statistics1.hashCode(), statistics2.hashCode());
    }

    @Test
    void testStatisticsToString() {
        // Given
        List<ClientPerformanceSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofHours(1);
        long timestampMs = System.currentTimeMillis();
        ClientPerformanceStatistics statistics = new ClientPerformanceStatistics(snapshots, timeRange, timestampMs);

        // When
        String result = statistics.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("ClientPerformanceStatistics"));
        assertTrue(result.contains("snapshots="));
        assertTrue(result.contains("timeRange="));
        assertTrue(result.contains("timestampMs="));
    }

    @Test
    void testStatisticsEdgeCases() {
        // Test with maximum values
        List<ClientPerformanceSnapshot> snapshots = List.of();
        Duration maxTimeRange = Duration.ofDays(Long.MAX_VALUE / 86400000000000L); // Maximum safe duration
        long maxTimestampMs = Long.MAX_VALUE;

        ClientPerformanceStatistics maxStatistics = new ClientPerformanceStatistics(snapshots, maxTimeRange,
                maxTimestampMs);

        assertNotNull(maxStatistics);
        assertEquals(maxTimeRange, maxStatistics.timeRange());
        assertEquals(maxTimestampMs, maxStatistics.timestampMs());

        // Test with minimum values
        Duration minTimeRange = Duration.ZERO;
        long minTimestampMs = 0L;

        ClientPerformanceStatistics minStatistics = new ClientPerformanceStatistics(snapshots, minTimeRange,
                minTimestampMs);

        assertNotNull(minStatistics);
        assertEquals(minTimeRange, minStatistics.timeRange());
        assertEquals(minTimestampMs, minStatistics.timestampMs());
    }

    @Test
    void testStatisticsNullHandling() {
        // Given
        Duration timeRange = Duration.ofHours(1);
        long timestampMs = System.currentTimeMillis();

        // When & Then - should not throw NullPointerException
        assertDoesNotThrow(() -> {
            ClientPerformanceStatistics statistics = new ClientPerformanceStatistics(null, timeRange, timestampMs);
            assertNotNull(statistics);
        });
    }

    @Test
    void testStatisticsImmutability() {
        // Given
        List<ClientPerformanceSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofHours(1);
        long timestampMs = System.currentTimeMillis();
        ClientPerformanceStatistics statistics = new ClientPerformanceStatistics(snapshots, timeRange, timestampMs);

        // When & Then - record should be immutable
        assertThrows(UnsupportedOperationException.class, () -> {
            statistics.snapshots().add(null); // Should fail if snapshots is unmodifiable
        });
    }

    @Test
    void testStatisticsPerformance() {
        // Given
        List<ClientPerformanceSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofHours(1);
        long timestampMs = System.currentTimeMillis();

        // When
        long startTime = System.nanoTime();
        ClientPerformanceStatistics statistics = new ClientPerformanceStatistics(snapshots, timeRange, timestampMs);
        long endTime = System.nanoTime();

        // Then - should be fast (less than 1ms)
        long durationNanos = endTime - startTime;
        assertTrue(durationNanos < 1_000_000, "Statistics creation took too long: " + durationNanos + " ns");
        assertNotNull(statistics);
    }

    @Test
    void testStatisticsThreadSafety() {
        // Given
        List<ClientPerformanceSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofHours(1);
        long timestampMs = System.currentTimeMillis();
        ClientPerformanceStatistics statistics = new ClientPerformanceStatistics(snapshots, timeRange, timestampMs);

        // When & Then - record should be thread-safe (immutable)
        assertDoesNotThrow(() -> {
            Thread[] threads = new Thread[10];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 1000; j++) {
                        assertEquals(0, statistics.total());
                        assertEquals(0, statistics.success());
                        assertEquals(0, statistics.failure());
                        assertEquals(0.0, statistics.successRate(), 0.001);
                    }
                });
            }

            // Start all threads
            for (Thread thread : threads) {
                thread.start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }
        });
    }
}
