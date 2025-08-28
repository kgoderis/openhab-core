package org.openhab.core.ai.tool.security.filters;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.SecurityMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

/**
 * Statistical analysis of authentication performance over time.
 * 
 * <p>
 * This class provides advanced statistical analysis including trends,
 * percentiles, and historical performance data for authentication operations.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record AuthStatistics(List<AuthSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, SecurityMetrics {

    /**
     * Factory method to create statistics from historical data.
     * 
     * @param snapshots list of auth snapshots
     * @param timeRange time range for statistics
     * @return new AuthStatistics instance
     */
    public static AuthStatistics of(List<AuthSnapshot> snapshots, Duration timeRange) {
        return new AuthStatistics(List.copyOf(snapshots), timeRange, System.currentTimeMillis());
    }

    /**
     * Factory method to create empty statistics.
     * 
     * @return empty AuthStatistics instance
     */
    public static AuthStatistics empty() {
        return new AuthStatistics(List.of(), Duration.ZERO, System.currentTimeMillis());
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate percentage change from first to last snapshot
        double firstValue = snapshots.get(0).successRate();
        double lastValue = snapshots.get(snapshots.size() - 1).successRate();
        return firstValue > 0 ? ((lastValue - firstValue) / firstValue) * 100.0 : 0.0;
    }

    @Override
    public String trendDirection() {
        double percentage = trendPercentage();
        if (percentage > 5.0) {
            return "increasing";
        } else if (percentage < -5.0) {
            return "decreasing";
        } else {
            return "stable";
        }
    }

    @Override
    public double changeRate() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        // Calculate change rate per time unit
        double totalChange = trendPercentage();
        long timeSpanMs = snapshots.get(snapshots.size() - 1).timestampMs() - snapshots.get(0).timestampMs();
        return timeSpanMs > 0 ? totalChange / (timeSpanMs / 1000.0) : 0.0;
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        return calculatePercentile(50.0);
    }

    @Override
    public double percentile90() {
        return calculatePercentile(90.0);
    }

    @Override
    public double percentile95() {
        return calculatePercentile(95.0);
    }

    @Override
    public double percentile99() {
        return calculatePercentile(99.0);
    }

    private double calculatePercentile(double percentile) {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        List<Double> values = snapshots.stream().mapToDouble(s -> s.successRate()).sorted().boxed()
                .collect(Collectors.toList());

        int index = (int) Math.ceil((percentile / 100.0) * values.size()) - 1;
        index = Math.max(0, Math.min(index, values.size() - 1));
        return values.get(index);
    }

    // SecurityMetrics implementation
    @Override
    public int activeSessions() {
        if (snapshots.isEmpty()) {
            return 0;
        }
        return (int) snapshots.stream().mapToInt(s -> s.activeSessions()).average().orElse(0.0);
    }

    @Override
    public int maxConnections() {
        if (snapshots.isEmpty()) {
            return 1000; // Default
        }
        return snapshots.stream().mapToInt(s -> s.maxConnections()).max().orElse(1000);
    }

    @Override
    public boolean authenticationEnabled() {
        return true; // Auth is always enabled if we're tracking auth metrics
    }

    @Override
    public boolean requestValidationEnabled() {
        return true; // Request validation is enabled if auth is enabled
    }

    @Override
    public int rateLimitPerMinute() {
        if (snapshots.isEmpty()) {
            return 100; // Default
        }
        return (int) snapshots.stream().mapToInt(s -> s.rateLimitPerMinute()).average().orElse(100.0);
    }

    @Override
    public int blockedClients() {
        if (snapshots.isEmpty()) {
            return 0;
        }
        return (int) snapshots.stream().mapToInt(s -> s.blockedClients()).average().orElse(0.0);
    }

    /**
     * Create a summary string with key statistics.
     * 
     * @return summary string
     */
    public String toSummary() {
        return String.format("AuthStatistics{trend=%s, successRate=%.1f%%, p95Latency=%.2fms, activeSessions=%d}",
                trendDirection(), percentile50(), percentile95(), activeSessions());
    }
}

