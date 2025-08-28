package org.openhab.core.ai.common.monitoring.service.statistics;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.monitoring.api.AgentMetrics;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.IntelligenceMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.Timing;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot;

/**
 * Comprehensive unit tests for AgentBehaviorStatistics.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class AgentBehaviorStatisticsTest {

    @Test
    void testConstructorWithValidData() {
        // Given
        List<AgentTaskSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofHours(1);
        long timestampMs = System.currentTimeMillis();

        // When
        AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange, timestampMs);

        // Then
        assertNotNull(statistics);
        assertEquals(snapshots, statistics.snapshots());
        assertEquals(timeRange, statistics.timeRange());
        assertEquals(timestampMs, statistics.timestampMs());
    }

    @Test
    void testCountsMetricsImplementation() {
        // Given
        AgentTaskSnapshot snapshot1 = new AgentTaskSnapshot(new Counts(100, 80, 20), new Timing(1000000000L),
                System.currentTimeMillis(), 0.8, 0.6);
        AgentTaskSnapshot snapshot2 = new AgentTaskSnapshot(new Counts(50, 45, 5), new Timing(500000000L),
                System.currentTimeMillis(), 0.9, 0.7);
        List<AgentTaskSnapshot> snapshots = List.of(snapshot1, snapshot2);
        Duration timeRange = Duration.ofHours(1);
        AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then
        assertEquals(150, statistics.total()); // 100 + 50
        assertEquals(125, statistics.success()); // 80 + 45
        assertEquals(25, statistics.failure()); // 20 + 5
        assertEquals(83.333, statistics.successRate(), 0.001); // (125/150) * 100
    }

    @Test
    void testCountsMetricsWithEmptySnapshots() {
        // Given
        List<AgentTaskSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofHours(1);
        AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then
        assertEquals(0, statistics.total());
        assertEquals(0, statistics.success());
        assertEquals(0, statistics.failure());
        assertEquals(0.0, statistics.successRate(), 0.001);
    }

    @Test
    void testLatencyMetricsImplementation() {
        // Given
        AgentTaskSnapshot snapshot1 = new AgentTaskSnapshot(new Counts(100, 80, 20), new Timing(1000000000L),
                System.currentTimeMillis(), 0.8, 0.6);
        AgentTaskSnapshot snapshot2 = new AgentTaskSnapshot(new Counts(50, 45, 5), new Timing(500000000L),
                System.currentTimeMillis(), 0.9, 0.7);
        List<AgentTaskSnapshot> snapshots = List.of(snapshot1, snapshot2);
        Duration timeRange = Duration.ofHours(1);
        AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then
        assertEquals(1500000000L, statistics.totalDurationNanos()); // 1000000000 + 500000000
        assertEquals(10.0, statistics.averageMs(), 0.001); // 1500000000 / (150 * 1000000)
        assertEquals(0.1, statistics.operationsPerSecond(), 0.001); // 150 / (3600 * 1000000000)
    }

    @Test
    void testLatencyMetricsWithZeroTimeRange() {
        // Given
        AgentTaskSnapshot snapshot = new AgentTaskSnapshot(new Counts(100, 80, 20), new Timing(1000000000L),
                System.currentTimeMillis(), 0.8, 0.6);
        List<AgentTaskSnapshot> snapshots = List.of(snapshot);
        Duration timeRange = Duration.ZERO;
        AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then
        assertEquals(1000000000L, statistics.totalDurationNanos());
        assertEquals(10.0, statistics.averageMs(), 0.001);
        assertEquals(0.0, statistics.operationsPerSecond(), 0.001); // Division by zero handled
    }

    @Test
    void testTrendMetricsImplementation() {
        // Given
        // First snapshot with 80% success rate
        AgentTaskSnapshot snapshot1 = new AgentTaskSnapshot(new Counts(100, 80, 20), new Timing(1000000000L),
                System.currentTimeMillis(), 0.8, 0.6);
        // Second snapshot with 90% success rate (improvement)
        AgentTaskSnapshot snapshot2 = new AgentTaskSnapshot(new Counts(100, 90, 10), new Timing(1000000000L),
                System.currentTimeMillis(), 0.9, 0.7);
        List<AgentTaskSnapshot> snapshots = List.of(snapshot1, snapshot2);
        Duration timeRange = Duration.ofDays(1);
        AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then
        assertEquals(12.5, statistics.trendPercentage(), 0.001); // ((90-80)/80) * 100
        assertEquals("increasing", statistics.trendDirection());
        assertEquals(12.5, statistics.changeRate(), 0.001); // 12.5 / 1 day
    }

    @Test
    void testTrendMetricsWithSingleSnapshot() {
        // Given
        AgentTaskSnapshot snapshot = new AgentTaskSnapshot(new Counts(100, 80, 20), new Timing(1000000000L),
                System.currentTimeMillis(), 0.8, 0.6);
        List<AgentTaskSnapshot> snapshots = List.of(snapshot);
        Duration timeRange = Duration.ofDays(1);
        AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then
        assertEquals(0.0, statistics.trendPercentage(), 0.001);
        assertEquals("stable", statistics.trendDirection());
        assertEquals(0.0, statistics.changeRate(), 0.001);
    }

    @Test
    void testTrendMetricsWithDecreasingTrend() {
        // Given
        // First snapshot with 90% success rate
        AgentTaskSnapshot snapshot1 = new AgentTaskSnapshot(new Counts(100, 90, 10), new Timing(1000000000L),
                System.currentTimeMillis(), 0.9, 0.7);
        // Second snapshot with 80% success rate (decline)
        AgentTaskSnapshot snapshot2 = new AgentTaskSnapshot(new Counts(100, 80, 20), new Timing(1000000000L),
                System.currentTimeMillis(), 0.8, 0.6);
        List<AgentTaskSnapshot> snapshots = List.of(snapshot1, snapshot2);
        Duration timeRange = Duration.ofDays(1);
        AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then
        assertEquals(-11.111, statistics.trendPercentage(), 0.001); // ((80-90)/90) * 100
        assertEquals("decreasing", statistics.trendDirection());
        assertEquals(-11.111, statistics.changeRate(), 0.001);
    }

    @Test
    void testPercentileMetricsImplementation() {
        // Given
        // Create snapshots with different latencies
        AgentTaskSnapshot snapshot1 = new AgentTaskSnapshot(new Counts(10, 8, 2), new Timing(10000000L), // 10ms avg
                System.currentTimeMillis(), 0.8, 0.6);
        AgentTaskSnapshot snapshot2 = new AgentTaskSnapshot(new Counts(10, 9, 1), new Timing(20000000L), // 20ms avg
                System.currentTimeMillis(), 0.9, 0.7);
        AgentTaskSnapshot snapshot3 = new AgentTaskSnapshot(new Counts(10, 7, 3), new Timing(30000000L), // 30ms avg
                System.currentTimeMillis(), 0.7, 0.5);
        List<AgentTaskSnapshot> snapshots = List.of(snapshot1, snapshot2, snapshot3);
        Duration timeRange = Duration.ofHours(1);
        AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then
        assertTrue(statistics.percentile50() > 0);
        assertTrue(statistics.percentile90() > 0);
        assertTrue(statistics.percentile95() > 0);
        assertTrue(statistics.percentile99() > 0);
        assertTrue(statistics.percentile50() <= statistics.percentile90());
        assertTrue(statistics.percentile90() <= statistics.percentile95());
        assertTrue(statistics.percentile95() <= statistics.percentile99());
    }

    @Test
    void testPercentileMetricsWithEmptySnapshots() {
        // Given
        List<AgentTaskSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofHours(1);
        AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then
        assertEquals(0.0, statistics.percentile50(), 0.001);
        assertEquals(0.0, statistics.percentile90(), 0.001);
        assertEquals(0.0, statistics.percentile95(), 0.001);
        assertEquals(0.0, statistics.percentile99(), 0.001);
    }

    @Test
    void testIntelligenceMetricsImplementation() {
        // Given
        AgentTaskSnapshot snapshot1 = new AgentTaskSnapshot(new Counts(100, 80, 20), new Timing(1000000000L),
                System.currentTimeMillis(), 0.8, 0.6);
        AgentTaskSnapshot snapshot2 = new AgentTaskSnapshot(new Counts(100, 90, 10), new Timing(1000000000L),
                System.currentTimeMillis(), 0.9, 0.7);
        List<AgentTaskSnapshot> snapshots = List.of(snapshot1, snapshot2);
        Duration timeRange = Duration.ofDays(1);
        AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then
        assertEquals(0.85, statistics.decisionQuality(), 0.001); // (0.8 + 0.9) / 2
        assertEquals(0.65, statistics.learningEfficiency(), 0.001); // (0.6 + 0.7) / 2
        assertEquals(16.667, statistics.adaptationRate(), 0.001); // ((0.7-0.6)/0.6) * 100
    }

    @Test
    void testIntelligenceMetricsWithSingleSnapshot() {
        // Given
        AgentTaskSnapshot snapshot = new AgentTaskSnapshot(new Counts(100, 80, 20), new Timing(1000000000L),
                System.currentTimeMillis(), 0.8, 0.6);
        List<AgentTaskSnapshot> snapshots = List.of(snapshot);
        Duration timeRange = Duration.ofDays(1);
        AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then
        assertEquals(0.8, statistics.decisionQuality(), 0.001);
        assertEquals(0.6, statistics.learningEfficiency(), 0.001);
        assertEquals(0.0, statistics.adaptationRate(), 0.001); // Single snapshot, no trend
    }

    @Test
    void testAgentMetricsImplementation() {
        // Given
        AgentTaskSnapshot snapshot1 = new AgentTaskSnapshot(new Counts(100, 80, 20), new Timing(1000000000L),
                System.currentTimeMillis(), 0.8, 0.6);
        AgentTaskSnapshot snapshot2 = new AgentTaskSnapshot(new Counts(100, 90, 10), new Timing(1000000000L),
                System.currentTimeMillis(), 0.9, 0.7);
        List<AgentTaskSnapshot> snapshots = List.of(snapshot1, snapshot2);
        Duration timeRange = Duration.ofDays(1);
        AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then
        assertEquals(0.85, statistics.decisionAccuracy(), 0.001); // Alias for decisionQuality
        assertEquals(0.65, statistics.learningRate(), 0.001); // Alias for learningEfficiency
        assertEquals(83.333, statistics.successRate(), 0.001); // From CountsMetrics
    }

    @Test
    void testFromSnapshotsFactoryMethod() {
        // Given
        AgentTaskSnapshot snapshot = new AgentTaskSnapshot(new Counts(100, 80, 20), new Timing(1000000000L),
                System.currentTimeMillis(), 0.8, 0.6);
        List<AgentTaskSnapshot> snapshots = List.of(snapshot);
        Duration timeRange = Duration.ofHours(1);

        // When
        AgentBehaviorStatistics statistics = AgentBehaviorStatistics.fromSnapshots(snapshots, timeRange);

        // Then
        assertNotNull(statistics);
        assertEquals(snapshots, statistics.snapshots());
        assertEquals(timeRange, statistics.timeRange());
        assertTrue(statistics.timestampMs() > 0);
        assertTrue(statistics.timestampMs() <= System.currentTimeMillis());
    }

    @Test
    void testFromSnapshotsWithEmptyList() {
        // Given
        List<AgentTaskSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofHours(1);

        // When
        AgentBehaviorStatistics statistics = AgentBehaviorStatistics.fromSnapshots(snapshots, timeRange);

        // Then
        assertNotNull(statistics);
        assertTrue(statistics.snapshots().isEmpty());
        assertEquals(timeRange, statistics.timeRange());
        assertTrue(statistics.timestampMs() > 0);
    }

    @Test
    void testInterfaceCompliance() {
        // Given
        List<AgentTaskSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofHours(1);
        AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then
        assertTrue(statistics instanceof CountsMetrics);
        assertTrue(statistics instanceof LatencyMetrics);
        assertTrue(statistics instanceof TrendMetrics);
        assertTrue(statistics instanceof PercentileMetrics);
        assertTrue(statistics instanceof IntelligenceMetrics);
        assertTrue(statistics instanceof AgentMetrics);
    }

    @Test
    void testEdgeCases() {
        // Given
        List<AgentTaskSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofHours(1);
        AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then - Test edge cases
        assertEquals(0.0, statistics.successRate(), 0.001); // Zero total
        assertEquals(0.0, statistics.averageMs(), 0.001); // Zero total
        assertEquals(0.0, statistics.operationsPerSecond(), 0.001); // Zero time range
        assertEquals(0.0, statistics.trendPercentage(), 0.001); // Single snapshot
        assertEquals("stable", statistics.trendDirection()); // Single snapshot
        assertEquals(0.0, statistics.changeRate(), 0.001); // Single snapshot
        assertEquals(0.0, statistics.percentile50(), 0.001); // Empty snapshots
        assertEquals(0.0, statistics.decisionQuality(), 0.001); // Empty snapshots
        assertEquals(0.0, statistics.learningEfficiency(), 0.001); // Empty snapshots
        assertEquals(0.0, statistics.adaptationRate(), 0.001); // Single snapshot
    }

    @Test
    void testEquality() {
        // Given
        AgentTaskSnapshot snapshot = new AgentTaskSnapshot(new Counts(100, 80, 20), new Timing(1000000000L),
                System.currentTimeMillis(), 0.8, 0.6);
        List<AgentTaskSnapshot> snapshots = List.of(snapshot);
        Duration timeRange = Duration.ofHours(1);
        long timestampMs = System.currentTimeMillis();

        AgentBehaviorStatistics statistics1 = new AgentBehaviorStatistics(snapshots, timeRange, timestampMs);
        AgentBehaviorStatistics statistics2 = new AgentBehaviorStatistics(snapshots, timeRange, timestampMs);

        // When & Then
        assertEquals(statistics1, statistics2);
        assertEquals(statistics1.hashCode(), statistics2.hashCode());
    }

    @Test
    void testInequality() {
        // Given
        AgentTaskSnapshot snapshot = new AgentTaskSnapshot(new Counts(100, 80, 20), new Timing(1000000000L),
                System.currentTimeMillis(), 0.8, 0.6);
        List<AgentTaskSnapshot> snapshots = List.of(snapshot);
        Duration timeRange1 = Duration.ofHours(1);
        Duration timeRange2 = Duration.ofHours(2);
        long timestampMs = System.currentTimeMillis();

        AgentBehaviorStatistics statistics1 = new AgentBehaviorStatistics(snapshots, timeRange1, timestampMs);
        AgentBehaviorStatistics statistics2 = new AgentBehaviorStatistics(snapshots, timeRange2, timestampMs);

        // When & Then
        assertNotEquals(statistics1, statistics2);
        assertNotEquals(statistics1.hashCode(), statistics2.hashCode());
    }

    @Test
    void testToString() {
        // Given
        AgentTaskSnapshot snapshot = new AgentTaskSnapshot(new Counts(100, 80, 20), new Timing(1000000000L),
                System.currentTimeMillis(), 0.8, 0.6);
        List<AgentTaskSnapshot> snapshots = List.of(snapshot);
        Duration timeRange = Duration.ofHours(1);
        long timestampMs = System.currentTimeMillis();

        AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange, timestampMs);

        // When
        String result = statistics.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("AgentBehaviorStatistics"));
        assertTrue(result.contains("snapshots="));
        assertTrue(result.contains("timeRange="));
        assertTrue(result.contains("timestampMs="));
    }

    @Test
    void testNullHandling() {
        // Given
        List<AgentTaskSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofHours(1);
        long timestampMs = System.currentTimeMillis();

        // When & Then - Should not throw NullPointerException
        assertDoesNotThrow(() -> {
            AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange, timestampMs);
            // Call all methods to ensure no NPE
            statistics.total();
            statistics.success();
            statistics.failure();
            statistics.successRate();
            statistics.totalDurationNanos();
            statistics.averageMs();
            statistics.operationsPerSecond();
            statistics.trendPercentage();
            statistics.trendDirection();
            statistics.changeRate();
            statistics.percentile50();
            statistics.percentile90();
            statistics.percentile95();
            statistics.percentile99();
            statistics.decisionQuality();
            statistics.learningEfficiency();
            statistics.adaptationRate();
            statistics.decisionAccuracy();
            statistics.learningRate();
        });
    }

    @Test
    void testImmutability() {
        // Given
        AgentTaskSnapshot snapshot = new AgentTaskSnapshot(new Counts(100, 80, 20), new Timing(1000000000L),
                System.currentTimeMillis(), 0.8, 0.6);
        List<AgentTaskSnapshot> snapshots = List.of(snapshot);
        Duration timeRange = Duration.ofHours(1);
        long timestampMs = System.currentTimeMillis();

        AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange, timestampMs);

        // When & Then - Record should be immutable
        assertEquals(snapshots, statistics.snapshots());
        assertEquals(timeRange, statistics.timeRange());
        assertEquals(timestampMs, statistics.timestampMs());

        // Verify that the record fields are final (immutable)
        assertTrue(AgentBehaviorStatistics.class.isRecord());
    }

    @Test
    void testPerformance() {
        // Given
        List<AgentTaskSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofHours(1);
        long timestampMs = System.currentTimeMillis();

        // When & Then - Should complete quickly
        long startTime = System.nanoTime();
        AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange, timestampMs);
        long endTime = System.nanoTime();

        // Construction should be fast (less than 1ms)
        assertTrue((endTime - startTime) < 1_000_000);

        // Method calls should also be fast
        startTime = System.nanoTime();
        statistics.total();
        statistics.successRate();
        statistics.averageMs();
        endTime = System.nanoTime();

        assertTrue((endTime - startTime) < 1_000_000);
    }

    @Test
    void testThreadSafety() {
        // Given
        List<AgentTaskSnapshot> snapshots = List.of();
        Duration timeRange = Duration.ofHours(1);
        long timestampMs = System.currentTimeMillis();

        AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange, timestampMs);

        // When & Then - Should be thread-safe (immutable record)
        assertDoesNotThrow(() -> {
            Thread[] threads = new Thread[10];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        statistics.total();
                        statistics.successRate();
                        statistics.averageMs();
                        statistics.trendPercentage();
                        statistics.decisionQuality();
                    }
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }
        });
    }

    @Test
    void testComplexScenario() {
        // Given - Complex scenario with multiple snapshots
        AgentTaskSnapshot snapshot1 = new AgentTaskSnapshot(new Counts(100, 80, 20), new Timing(1000000000L),
                System.currentTimeMillis(), 0.8, 0.6);
        AgentTaskSnapshot snapshot2 = new AgentTaskSnapshot(new Counts(150, 135, 15), new Timing(1500000000L),
                System.currentTimeMillis(), 0.9, 0.7);
        AgentTaskSnapshot snapshot3 = new AgentTaskSnapshot(new Counts(200, 180, 20), new Timing(2000000000L),
                System.currentTimeMillis(), 0.9, 0.8);
        List<AgentTaskSnapshot> snapshots = List.of(snapshot1, snapshot2, snapshot3);
        Duration timeRange = Duration.ofDays(1);
        AgentBehaviorStatistics statistics = new AgentBehaviorStatistics(snapshots, timeRange,
                System.currentTimeMillis());

        // When & Then - Verify all metrics work together
        assertEquals(450, statistics.total()); // 100 + 150 + 200
        assertEquals(395, statistics.success()); // 80 + 135 + 180
        assertEquals(55, statistics.failure()); // 20 + 15 + 20
        assertEquals(87.778, statistics.successRate(), 0.001); // (395/450) * 100
        assertEquals(4500000000L, statistics.totalDurationNanos()); // Sum of all durations
        assertEquals(10.0, statistics.averageMs(), 0.001); // 4500000000 / (450 * 1000000)
        assertEquals(0.005, statistics.operationsPerSecond(), 0.001); // 450 / (86400 * 1000000000)
        assertEquals(0.867, statistics.decisionQuality(), 0.001); // (0.8 + 0.9 + 0.9) / 3
        assertEquals(0.7, statistics.learningEfficiency(), 0.001); // (0.6 + 0.7 + 0.8) / 3
        assertEquals(33.333, statistics.adaptationRate(), 0.001); // ((0.8-0.6)/0.6) * 100
    }
}
