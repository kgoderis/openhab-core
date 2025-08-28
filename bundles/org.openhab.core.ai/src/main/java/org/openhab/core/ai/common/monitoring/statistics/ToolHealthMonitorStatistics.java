package org.openhab.core.ai.common.monitoring.statistics;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.HealthMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;
import org.openhab.core.ai.common.monitoring.snapshot.ToolHealthMonitorSnapshot;

/**
 * Immutable statistics for tool health monitoring operations over time ranges.
 * 
 * This record provides computed statistics and insights about tool health monitoring
 * derived from metrics data over time ranges. It implements capability interfaces
 * for clean, type-safe statistics access.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ToolHealthMonitorStatistics(List<ToolHealthMonitorSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, HealthMetrics, PercentileMetrics, TrendMetrics {

    public ToolHealthMonitorStatistics {
        Objects.requireNonNull(snapshots, "snapshots");
        Objects.requireNonNull(timeRange, "timeRange");
        snapshots = List.copyOf(snapshots);
    }

    /**
     * Constructor for ToolHealthMonitorStatistics with current timestamp.
     * 
     * @param snapshots the snapshots to compute statistics from
     * @param timeRange the time range these statistics cover
     */
    public ToolHealthMonitorStatistics(List<ToolHealthMonitorSnapshot> snapshots, Duration timeRange) {
        this(snapshots, timeRange, System.currentTimeMillis());
    }

    // HealthMetrics implementation
    @Override
    public org.openhab.core.ai.common.monitoring.api.HealthStatus healthStatus() {
        if (snapshots.isEmpty()) {
            return org.openhab.core.ai.common.monitoring.api.HealthStatus.UNKNOWN;
        }

        // Get the most recent snapshot's health status
        ToolHealthMonitorSnapshot latest = snapshots.get(snapshots.size() - 1);
        return latest.healthStatus();
    }

    @Override
    public String statusMessage() {
        if (snapshots.isEmpty()) {
            return "No health monitoring data available";
        }

        ToolHealthMonitorSnapshot latest = snapshots.get(snapshots.size() - 1);
        return latest.statusMessage();
    }

    @Override
    public java.util.Map<String, Object> healthIndicators() {
        java.util.Map<String, Object> indicators = new java.util.HashMap<>();
        indicators.put("totalSnapshots", snapshots.size());
        indicators.put("averageHealthScore", averageHealthScore());
        indicators.put("healthTrend", getHealthTrend());
        indicators.put("maxConsecutiveFailures", maxConsecutiveFailures());
        indicators.put("healthyPercentage", healthyPercentage());
        return indicators;
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        return calculateHealthScorePercentile(0.5);
    }

    @Override
    public double percentile90() {
        return calculateHealthScorePercentile(0.90);
    }

    @Override
    public double percentile95() {
        return calculateHealthScorePercentile(0.95);
    }

    @Override
    public double percentile99() {
        return calculateHealthScorePercentile(0.99);
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        double firstScore = snapshots.get(0).healthScore();
        double lastScore = snapshots.get(snapshots.size() - 1).healthScore();
        if (firstScore == 0.0) {
            return lastScore > 0 ? 100.0 : 0.0;
        }
        return ((lastScore - firstScore) / firstScore) * 100.0;
    }

    @Override
    public String trendDirection() {
        double trend = trendPercentage();
        if (trend > 10.0)
            return "IMPROVING";
        if (trend < -10.0)
            return "DEGRADING";
        return "STABLE";
    }

    @Override
    public double changeRate() {
        if (snapshots.size() < 2 || timeRange.isZero()) {
            return 0.0;
        }
        double trend = getHealthTrend();
        return trend / timeRange.toMinutes(); // change per minute
    }

    /**
     * Calculate average health score across all snapshots.
     * 
     * @return average health score (0.0-1.0)
     */
    public double averageHealthScore() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        return snapshots.stream().mapToDouble(ToolHealthMonitorSnapshot::healthScore).average().orElse(0.0);
    }

    /**
     * Get the health trend over the time range.
     * 
     * @return positive value indicates improving health, negative indicates degrading
     */
    public double getHealthTrend() {
        if (snapshots.size() < 2) {
            return 0.0;
        }

        // Simple linear trend calculation
        double firstScore = snapshots.get(0).healthScore();
        double lastScore = snapshots.get(snapshots.size() - 1).healthScore();
        return lastScore - firstScore;
    }

    /**
     * Get the maximum consecutive failures observed.
     * 
     * @return maximum consecutive failures
     */
    public long maxConsecutiveFailures() {
        return snapshots.stream().mapToLong(ToolHealthMonitorSnapshot::getConsecutiveFailures).max().orElse(0);
    }

    /**
     * Calculate percentage of time the component was healthy.
     * 
     * @return percentage of healthy snapshots (0.0-100.0)
     */
    public double healthyPercentage() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        long healthyCount = snapshots.stream().mapToLong(s -> s.isHealthy() ? 1 : 0).sum();

        return (healthyCount * 100.0) / snapshots.size();
    }

    /**
     * Get snapshots that are unhealthy.
     * 
     * @return list of unhealthy snapshots
     */
    public List<ToolHealthMonitorSnapshot> getUnhealthySnapshots() {
        return snapshots.stream().filter(s -> !s.isHealthy()).collect(Collectors.toList());
    }

    /**
     * Check if immediate attention is required based on recent health trends.
     * 
     * @return true if attention is required
     */
    public boolean requiresImmediateAttention() {
        if (snapshots.isEmpty()) {
            return false;
        }

        // Check recent health status
        ToolHealthMonitorSnapshot latest = snapshots.get(snapshots.size() - 1);
        if (latest.isUnhealthy()) {
            return true;
        }

        // Check if consecutive failures exceed threshold
        if (maxConsecutiveFailures() >= 5) {
            return true;
        }

        // Check if health trend is severely degrading
        if (getHealthTrend() < -0.3) {
            return true;
        }

        return false;
    }

    private double calculateHealthScorePercentile(double percentile) {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        List<Double> healthScores = snapshots.stream().mapToDouble(ToolHealthMonitorSnapshot::healthScore).sorted()
                .boxed().collect(Collectors.toList());

        int index = (int) Math.ceil(percentile * healthScores.size()) - 1;
        index = Math.max(0, Math.min(index, healthScores.size() - 1));

        return healthScores.get(index);
    }
}
