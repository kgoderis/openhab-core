package org.openhab.core.ai.tool.monitoring;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.osgi.service.component.annotations.Reference;

/**
 * Monitoring statistics for system monitoring operations.
 * 
 * <p>
 * This class provides computed statistics and insights about monitoring operations
 * derived from metrics data over time ranges. It implements capability interfaces
 * for clean, type-safe statistics access.
 * </p>
 * 
 * <p>
 * Statistics include:
 * - Metrics collection performance and throughput
 * - Alert generation patterns and trends
 * - System health monitoring effectiveness
 * - Performance optimization insights
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record MonitoringStatistics(long totalMetricsCollected, long totalAlertsGenerated,
        Map<String, Long> metricsByType, Map<String, Long> alertsBySeverity, long lastCollectionTime,
        Duration timeRange,
        long timestampMs) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, TrendMetrics, PercentileMetrics {

    @Reference
    private static @Nullable MetricsService metricsService;

    // CountsMetrics implementation
    @Override
    public long total() {
        return totalMetricsCollected + totalAlertsGenerated;
    }

    @Override
    public long success() {
        // For monitoring statistics, we consider metrics collection as successful operations
        return totalMetricsCollected;
    }

    @Override
    public long failure() {
        // Alerts generated could be considered as "failures" in the monitoring context
        return totalAlertsGenerated;
    }

    @Override
    public double successRate() {
        if (total() == 0)
            return 0.0;
        return (success() * 100.0) / total();
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        // Monitoring operations don't track duration in the current implementation
        // This would need to be enhanced to track actual monitoring operation durations
        return 0;
    }

    @Override
    public double averageMs(long total) {
        if (total == 0)
            return 0.0;
        return totalDurationNanos() / (total * 1_000_000.0);
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        // Simplified implementation - would need historical data for proper trend calculation
        // For now, return a stable trend since we don't have historical snapshots
        return 0.0;
    }

    @Override
    public String trendDirection() {
        double trend = trendPercentage();
        if (trend > 1.0)
            return "increasing";
        if (trend < -1.0)
            return "decreasing";
        return "stable";
    }

    @Override
    public double changeRate() {
        return trendPercentage() / timeRange.toDays();
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        // Simplified implementation - would need historical data for proper percentile calculation
        return 0.0;
    }

    @Override
    public double percentile90() {
        // Simplified implementation - would need historical data for proper percentile calculation
        return 0.0;
    }

    @Override
    public double percentile95() {
        // Simplified implementation - would need historical data for proper percentile calculation
        return 0.0;
    }

    @Override
    public double percentile99() {
        // Simplified implementation - would need historical data for proper percentile calculation
        return 0.0;
    }

    // Monitoring-specific methods
    /**
     * Get the total number of metrics collected.
     * 
     * @return total metrics collected
     */
    public long getTotalMetricsCollected() {
        return totalMetricsCollected;
    }

    /**
     * Get the total number of alerts generated.
     * 
     * @return total alerts generated
     */
    public long getTotalAlertsGenerated() {
        return totalAlertsGenerated;
    }

    /**
     * Get metrics breakdown by type.
     * 
     * @return metrics by type
     */
    public Map<String, Long> getMetricsByType() {
        return metricsByType;
    }

    /**
     * Get alerts breakdown by severity.
     * 
     * @return alerts by severity
     */
    public Map<String, Long> getAlertsBySeverity() {
        return alertsBySeverity;
    }

    /**
     * Get the last collection time.
     * 
     * @return last collection time
     */
    public long getLastCollectionTime() {
        return lastCollectionTime;
    }

    /**
     * Get the time range for these statistics.
     * 
     * @return time range
     */
    public Duration getTimeRange() {
        return timeRange;
    }

    /**
     * Get the timestamp when these statistics were created.
     * 
     * @return timestamp
     */
    public long getTimestampMs() {
        return timestampMs;
    }

    /**
     * Calculate metrics collection rate per hour.
     * 
     * @return metrics collection rate per hour
     */
    public double getMetricsCollectionRatePerHour() {
        if (timeRange.toHours() == 0)
            return 0.0;
        return (double) totalMetricsCollected / timeRange.toHours();
    }

    /**
     * Calculate alert generation rate per hour.
     * 
     * @return alert generation rate per hour
     */
    public double getAlertGenerationRatePerHour() {
        if (timeRange.toHours() == 0)
            return 0.0;
        return (double) totalAlertsGenerated / timeRange.toHours();
    }

    /**
     * Get the most common metric type.
     * 
     * @return most common metric type or null if no metrics
     */
    public String getMostCommonMetricType() {
        if (metricsByType.isEmpty())
            return null;
        return metricsByType.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
    }

    /**
     * Get the most common alert severity.
     * 
     * @return most common alert severity or null if no alerts
     */
    public String getMostCommonAlertSeverity() {
        if (alertsBySeverity.isEmpty())
            return null;
        return alertsBySeverity.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Calculate the alert-to-metrics ratio.
     * 
     * @return alert-to-metrics ratio
     */
    public double getAlertToMetricsRatio() {
        if (totalMetricsCollected == 0)
            return 0.0;
        return (double) totalAlertsGenerated / totalMetricsCollected;
    }

    /**
     * Factory method for creating from current state.
     * 
     * @param totalMetricsCollected total metrics collected
     * @param totalAlertsGenerated total alerts generated
     * @param metricsByType metrics breakdown by type
     * @param alertsBySeverity alerts breakdown by severity
     * @param lastCollectionTime last collection time
     * @return monitoring statistics
     */
    public static MonitoringStatistics fromCurrentState(long totalMetricsCollected, long totalAlertsGenerated,
            Map<String, Long> metricsByType, Map<String, Long> alertsBySeverity, long lastCollectionTime) {
        return new MonitoringStatistics(totalMetricsCollected, totalAlertsGenerated, metricsByType, alertsBySeverity,
                lastCollectionTime, Duration.ofDays(1), System.currentTimeMillis());
    }

    /**
     * Factory method for creating from snapshots.
     * 
     * @param snapshots list of monitoring snapshots
     * @param timeRange time range for statistics
     * @return monitoring statistics
     */
    public static MonitoringStatistics fromSnapshots(List<MonitoringSnapshot> snapshots, Duration timeRange) {
        if (snapshots.isEmpty()) {
            return new MonitoringStatistics(0, 0, Map.of(), Map.of(), 0, timeRange, System.currentTimeMillis());
        }

        // Aggregate data from snapshots
        long totalMetricsCollected = snapshots.stream().mapToLong(s -> s.totalMetricsCollected()).sum();
        long totalAlertsGenerated = snapshots.stream().mapToLong(s -> s.totalAlertsGenerated()).sum();

        // Aggregate metrics by type
        Map<String, Long> metricsByType = snapshots.stream().flatMap(s -> s.metricsByType().entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingLong(Map.Entry::getValue)));

        // Aggregate alerts by severity
        Map<String, Long> alertsBySeverity = snapshots.stream().flatMap(s -> s.alertsBySeverity().entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingLong(Map.Entry::getValue)));

        // Get the latest collection time
        long lastCollectionTime = snapshots.stream().mapToLong(s -> s.lastCollectionTime()).max().orElse(0);

        return new MonitoringStatistics(totalMetricsCollected, totalAlertsGenerated, metricsByType, alertsBySeverity,
                lastCollectionTime, timeRange, System.currentTimeMillis());
    }

    /**
     * Create empty monitoring statistics.
     * 
     * @param timeRange time range for statistics
     * @return empty monitoring statistics
     */
    public static MonitoringStatistics empty(Duration timeRange) {
        return new MonitoringStatistics(0, 0, Map.of(), Map.of(), 0, timeRange, System.currentTimeMillis());
    }

    /**
     * Monitoring snapshot record for historical data.
     */
    public record MonitoringSnapshot(long totalMetricsCollected, long totalAlertsGenerated,
            Map<String, Long> metricsByType, Map<String, Long> alertsBySeverity, long lastCollectionTime,
            long timestampMs) {
    }

    /**
     * Record monitoring statistics using MetricsService.
     */
    public static void recordMonitoringStatistics(boolean success, long durationMs) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            metrics.recordOperation("monitoring", "statistics", success, java.time.Duration.ofMillis(durationMs));
        }
    }
}
