package org.openhab.core.ai.common.monitoring.statistics;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.HealthMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;
import org.openhab.core.ai.common.monitoring.snapshot.SystemHealthMonitorSnapshot;

/**
 * Statistics for system health monitoring operations over time ranges.
 * 
 * This class provides computed statistics and insights about system health monitoring
 * derived from metrics data over time ranges. It implements capability interfaces
 * for clean, type-safe statistics access.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class SystemHealthMonitorStatistics
        implements StatisticsSnapshot, HealthMetrics, PercentileMetrics, TrendMetrics {

    private final List<SystemHealthMonitorSnapshot> snapshots;
    private final Duration timeRange;
    private final long timestampMs;

    /**
     * Constructor for SystemHealthMonitorStatistics.
     * 
     * @param snapshots the snapshots to compute statistics from
     * @param timeRange the time range these statistics cover
     */
    public SystemHealthMonitorStatistics(List<SystemHealthMonitorSnapshot> snapshots, Duration timeRange) {
        this.snapshots = List.copyOf(snapshots);
        this.timeRange = timeRange;
        this.timestampMs = System.currentTimeMillis();
    }

    public Duration timeRange() {
        return timeRange;
    }

    public long timestampMs() {
        return timestampMs;
    }

    // HealthMetrics implementation
    @Override
    public org.openhab.core.ai.common.monitoring.api.HealthStatus healthStatus() {
        if (snapshots.isEmpty()) {
            return org.openhab.core.ai.common.monitoring.api.HealthStatus.UNKNOWN;
        }

        // Get the most recent snapshot's health status
        SystemHealthMonitorSnapshot latest = snapshots.get(snapshots.size() - 1);
        return latest.healthStatus();
    }

    @Override
    public String statusMessage() {
        if (snapshots.isEmpty()) {
            return "No system health monitoring data available";
        }

        SystemHealthMonitorSnapshot latest = snapshots.get(snapshots.size() - 1);
        return latest.statusMessage();
    }

    @Override
    public java.util.Map<String, Object> healthIndicators() {
        java.util.Map<String, Object> indicators = new java.util.HashMap<>();
        indicators.put("totalSnapshots", snapshots.size());
        indicators.put("averageHealthScore", averageSystemHealthScore());
        indicators.put("healthTrend", getSystemHealthTrend());
        indicators.put("averageProviderHealth", averageProviderHealthPercentage());
        indicators.put("averageServiceHealth", averageServiceHealthPercentage());
        indicators.put("systemUptime", getSystemUptimePercentage());
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
        double firstScore = snapshots.get(0).getOverallHealthScore();
        double lastScore = snapshots.get(snapshots.size() - 1).getOverallHealthScore();
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
        double trend = getSystemHealthTrend();
        return trend / timeRange.toMinutes(); // change per minute
    }

    /**
     * Calculate average system health score across all snapshots.
     * 
     * @return average system health score (0.0-1.0)
     */
    public double averageSystemHealthScore() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        return snapshots.stream().mapToDouble(SystemHealthMonitorSnapshot::getOverallHealthScore).average().orElse(0.0);
    }

    /**
     * Get the system health trend over the time range.
     * 
     * @return positive value indicates improving health, negative indicates degrading
     */
    public double getSystemHealthTrend() {
        if (snapshots.size() < 2) {
            return 0.0;
        }

        // Simple linear trend calculation
        double firstScore = snapshots.get(0).getOverallHealthScore();
        double lastScore = snapshots.get(snapshots.size() - 1).getOverallHealthScore();
        return lastScore - firstScore;
    }

    /**
     * Calculate average provider health percentage across all snapshots.
     * 
     * @return average provider health percentage (0.0-100.0)
     */
    public double averageProviderHealthPercentage() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        return snapshots.stream().mapToDouble(SystemHealthMonitorSnapshot::getProviderHealthPercentage).average()
                .orElse(0.0);
    }

    /**
     * Calculate average service health percentage across all snapshots.
     * 
     * @return average service health percentage (0.0-100.0)
     */
    public double averageServiceHealthPercentage() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        return snapshots.stream().mapToDouble(SystemHealthMonitorSnapshot::getServiceHealthPercentage).average()
                .orElse(0.0);
    }

    /**
     * Calculate percentage of time the system was healthy.
     * 
     * @return percentage of healthy snapshots (0.0-100.0)
     */
    public double getSystemUptimePercentage() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        long healthyCount = snapshots.stream().mapToLong(s -> s.isHealthy() ? 1 : 0).sum();

        return (healthyCount * 100.0) / snapshots.size();
    }

    /**
     * Get snapshots that indicate critical system health.
     * 
     * @return list of critical health snapshots
     */
    public List<SystemHealthMonitorSnapshot> getCriticalHealthSnapshots() {
        return snapshots.stream().filter(s -> s.requiresImmediateAttention()).collect(Collectors.toList());
    }

    /**
     * Check if immediate attention is required based on recent system health trends.
     * 
     * @return true if attention is required
     */
    public boolean requiresImmediateAttention() {
        if (snapshots.isEmpty()) {
            return false;
        }

        // Check recent health status
        SystemHealthMonitorSnapshot latest = snapshots.get(snapshots.size() - 1);
        if (latest.requiresImmediateAttention()) {
            return true;
        }

        // Check if health trend is severely degrading
        if (getSystemHealthTrend() < -0.3) {
            return true;
        }

        // Check if system uptime is critically low
        if (getSystemUptimePercentage() < 30.0) {
            return true;
        }

        return false;
    }

    /**
     * Get the worst health score observed.
     * 
     * @return minimum health score (0.0-1.0)
     */
    public double getWorstHealthScore() {
        return snapshots.stream().mapToDouble(SystemHealthMonitorSnapshot::getOverallHealthScore).min().orElse(0.0);
    }

    /**
     * Get the best health score observed.
     * 
     * @return maximum health score (0.0-1.0)
     */
    public double getBestHealthScore() {
        return snapshots.stream().mapToDouble(SystemHealthMonitorSnapshot::getOverallHealthScore).max().orElse(0.0);
    }

    private double calculateHealthScorePercentile(double percentile) {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        List<Double> healthScores = snapshots.stream().mapToDouble(SystemHealthMonitorSnapshot::getOverallHealthScore)
                .sorted().boxed().collect(Collectors.toList());

        int index = (int) Math.ceil(percentile * healthScores.size()) - 1;
        index = Math.max(0, Math.min(index, healthScores.size() - 1));

        return healthScores.get(index);
    }
}
