package org.openhab.core.ai.tool.monitoring;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;

/**
 * Test class for MonitoringStatistics migration to MetricsService pattern.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class MonitoringStatisticsTest {

    @Test
    void testMonitoringStatisticsCreation() {
        // Test basic creation
        Map<String, Long> metricsByType = Map.of("cpu", 10L, "memory", 5L);
        Map<String, Long> alertsBySeverity = Map.of("warning", 2L, "error", 1L);

        MonitoringStatistics stats = new MonitoringStatistics(15L, // totalMetricsCollected
                3L, // totalAlertsGenerated
                metricsByType, alertsBySeverity, System.currentTimeMillis(), Duration.ofDays(1),
                System.currentTimeMillis());

        // Test basic getters
        assertEquals(15L, stats.getTotalMetricsCollected());
        assertEquals(3L, stats.getTotalAlertsGenerated());
        assertEquals(metricsByType, stats.getMetricsByType());
        assertEquals(alertsBySeverity, stats.getAlertsBySeverity());
        assertNotNull(stats.getTimeRange());
        assertTrue(stats.getTimestampMs() > 0);
    }

    @Test
    void testCountsMetricsImplementation() {
        MonitoringStatistics stats = new MonitoringStatistics(10L, // totalMetricsCollected
                2L, // totalAlertsGenerated
                Map.of("cpu", 10L), Map.of("warning", 2L), System.currentTimeMillis(), Duration.ofDays(1),
                System.currentTimeMillis());

        // Test CountsMetrics interface implementation
        assertEquals(12L, stats.total()); // 10 + 2
        assertEquals(10L, stats.success()); // metrics collected
        assertEquals(2L, stats.failure()); // alerts generated
        assertEquals(83.33, stats.successRate(), 0.01); // (10/12) * 100
    }

    @Test
    void testLatencyMetricsImplementation() {
        MonitoringStatistics stats = new MonitoringStatistics(10L, // totalMetricsCollected
                2L, // totalAlertsGenerated
                Map.of("cpu", 10L), Map.of("warning", 2L), System.currentTimeMillis(), Duration.ofDays(1),
                System.currentTimeMillis());

        // Test LatencyMetrics interface implementation
        assertEquals(0L, stats.totalDurationNanos()); // Not implemented yet
        assertEquals(0.0, stats.averageMs(12L), 0.01); // 0 duration / 12 operations
    }

    @Test
    void testTrendMetricsImplementation() {
        MonitoringStatistics stats = new MonitoringStatistics(10L, // totalMetricsCollected
                2L, // totalAlertsGenerated
                Map.of("cpu", 10L), Map.of("warning", 2L), System.currentTimeMillis(), Duration.ofDays(1),
                System.currentTimeMillis());

        // Test TrendMetrics interface implementation
        assertEquals(0.0, stats.trendPercentage(), 0.01); // Simplified implementation
        assertEquals("stable", stats.trendDirection()); // Based on 0.0 trend
        assertEquals(0.0, stats.changeRate(), 0.01); // 0.0 / 1 day
    }

    @Test
    void testPercentileMetricsImplementation() {
        MonitoringStatistics stats = new MonitoringStatistics(10L, // totalMetricsCollected
                2L, // totalAlertsGenerated
                Map.of("cpu", 10L), Map.of("warning", 2L), System.currentTimeMillis(), Duration.ofDays(1),
                System.currentTimeMillis());

        // Test PercentileMetrics interface implementation
        assertEquals(0.0, stats.percentile50(), 0.01); // Simplified implementation
        assertEquals(0.0, stats.percentile90(), 0.01); // Simplified implementation
        assertEquals(0.0, stats.percentile95(), 0.01); // Simplified implementation
        assertEquals(0.0, stats.percentile99(), 0.01); // Simplified implementation
    }

    @Test
    void testMonitoringSpecificMethods() {
        Map<String, Long> metricsByType = Map.of("cpu", 10L, "memory", 5L);
        Map<String, Long> alertsBySeverity = Map.of("warning", 2L, "error", 1L);

        MonitoringStatistics stats = new MonitoringStatistics(15L, // totalMetricsCollected
                3L, // totalAlertsGenerated
                metricsByType, alertsBySeverity, System.currentTimeMillis(), Duration.ofHours(2), // 2 hours
                System.currentTimeMillis());

        // Test monitoring-specific methods
        assertEquals(7.5, stats.getMetricsCollectionRatePerHour(), 0.01); // 15 / 2 hours
        assertEquals(1.5, stats.getAlertGenerationRatePerHour(), 0.01); // 3 / 2 hours
        assertEquals("cpu", stats.getMostCommonMetricType()); // cpu has highest count (10)
        assertEquals("warning", stats.getMostCommonAlertSeverity()); // warning has highest count (2)
        assertEquals(0.2, stats.getAlertToMetricsRatio(), 0.01); // 3 / 15
    }

    @Test
    void testFromCurrentStateFactory() {
        Map<String, Long> metricsByType = Map.of("cpu", 10L);
        Map<String, Long> alertsBySeverity = Map.of("warning", 2L);

        MonitoringStatistics stats = MonitoringStatistics.fromCurrentState(10L, // totalMetricsCollected
                2L, // totalAlertsGenerated
                metricsByType, alertsBySeverity, System.currentTimeMillis());

        assertEquals(10L, stats.getTotalMetricsCollected());
        assertEquals(2L, stats.getTotalAlertsGenerated());
        assertEquals(metricsByType, stats.getMetricsByType());
        assertEquals(alertsBySeverity, stats.getAlertsBySeverity());
        assertEquals(Duration.ofDays(1), stats.getTimeRange());
        assertTrue(stats.getTimestampMs() > 0);
    }

    @Test
    void testFromSnapshotsFactory() {
        // Create test snapshots
        MonitoringStatistics.MonitoringSnapshot snapshot1 = new MonitoringStatistics.MonitoringSnapshot(5L, // totalMetricsCollected
                1L, // totalAlertsGenerated
                Map.of("cpu", 5L), Map.of("warning", 1L), System.currentTimeMillis(), System.currentTimeMillis());

        MonitoringStatistics.MonitoringSnapshot snapshot2 = new MonitoringStatistics.MonitoringSnapshot(3L, // totalMetricsCollected
                1L, // totalAlertsGenerated
                Map.of("memory", 3L), Map.of("error", 1L), System.currentTimeMillis(), System.currentTimeMillis());

        List<MonitoringStatistics.MonitoringSnapshot> snapshots = List.of(snapshot1, snapshot2);
        Duration timeRange = Duration.ofHours(1);

        MonitoringStatistics stats = MonitoringStatistics.fromSnapshots(snapshots, timeRange);

        assertEquals(8L, stats.getTotalMetricsCollected()); // 5 + 3
        assertEquals(2L, stats.getTotalAlertsGenerated()); // 1 + 1
        assertEquals(2, stats.getMetricsByType().size()); // cpu and memory
        assertEquals(2, stats.getAlertsBySeverity().size()); // warning and error
        assertEquals(timeRange, stats.getTimeRange());
    }

    @Test
    void testEmptyFactory() {
        Duration timeRange = Duration.ofHours(1);
        MonitoringStatistics stats = MonitoringStatistics.empty(timeRange);

        assertEquals(0L, stats.getTotalMetricsCollected());
        assertEquals(0L, stats.getTotalAlertsGenerated());
        assertTrue(stats.getMetricsByType().isEmpty());
        assertTrue(stats.getAlertsBySeverity().isEmpty());
        assertEquals(timeRange, stats.getTimeRange());
        assertTrue(stats.getTimestampMs() > 0);
    }

    @Test
    void testEdgeCases() {
        // Test with zero values
        MonitoringStatistics stats = new MonitoringStatistics(0L, // totalMetricsCollected
                0L, // totalAlertsGenerated
                Map.of(), Map.of(), System.currentTimeMillis(), Duration.ofDays(1), System.currentTimeMillis());

        assertEquals(0L, stats.total());
        assertEquals(0L, stats.success());
        assertEquals(0L, stats.failure());
        assertEquals(0.0, stats.successRate(), 0.01);
        assertEquals(0.0, stats.getMetricsCollectionRatePerHour(), 0.01);
        assertEquals(0.0, stats.getAlertGenerationRatePerHour(), 0.01);
        assertNull(stats.getMostCommonMetricType());
        assertNull(stats.getMostCommonAlertSeverity());
        assertEquals(0.0, stats.getAlertToMetricsRatio(), 0.01);
    }

    @Test
    void testInterfaceCompliance() {
        MonitoringStatistics stats = new MonitoringStatistics(10L, // totalMetricsCollected
                2L, // totalAlertsGenerated
                Map.of("cpu", 10L), Map.of("warning", 2L), System.currentTimeMillis(), Duration.ofDays(1),
                System.currentTimeMillis());

        // Verify that the class implements all required interfaces
        assertTrue(stats instanceof CountsMetrics);
        assertTrue(stats instanceof LatencyMetrics);
        assertTrue(stats instanceof TrendMetrics);
        assertTrue(stats instanceof PercentileMetrics);
    }
}
