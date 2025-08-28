package org.openhab.core.ai.agent.lifecycle;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

/**
 * Statistical analysis of default agent performance over time.
 * 
 * <p>
 * This class provides comprehensive statistical analysis of agent performance
 * including trend analysis, percentiles, and historical performance data.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record DefaultAgentStatistics(List<DefaultAgentSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics {

    /**
     * Create a new DefaultAgentStatistics instance.
     * 
     * @param snapshots the list of snapshots
     * @param timeRange the time range covered
     * @return new DefaultAgentStatistics instance
     */
    public static DefaultAgentStatistics of(List<DefaultAgentSnapshot> snapshots, Duration timeRange) {
        return new DefaultAgentStatistics(snapshots, timeRange, System.currentTimeMillis());
    }

    // StatisticsSnapshot implementation
    public long snapshotCount() {
        return snapshots.size();
    }

    @Override
    public Duration timeRange() {
        return timeRange;
    }

    @Override
    public long timestampMs() {
        return timestampMs;
    }

    // TrendMetrics implementation
    public double successRateTrend() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        
        List<Double> rates = snapshots.stream()
                .map(DefaultAgentSnapshot::successRate)
                .collect(Collectors.toList());
        
        return calculateTrend(rates);
    }

    public double latencyTrend() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        
        List<Double> latencies = snapshots.stream()
                .map(DefaultAgentSnapshot::averageMs)
                .collect(Collectors.toList());
        
        return calculateTrend(latencies);
    }

    public double throughputTrend() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        
        List<Double> throughputs = snapshots.stream()
                .map(snapshot -> snapshot.total() / (timeRange.toSeconds() > 0 ? timeRange.toSeconds() : 1.0))
                .collect(Collectors.toList());
        
        return calculateTrend(throughputs);
    }

    // PercentileMetrics implementation
    public double p50Latency() {
        return calculatePercentile(snapshots.stream()
                .mapToDouble(DefaultAgentSnapshot::averageMs)
                .toArray(), 50.0);
    }

    public double p95Latency() {
        return calculatePercentile(snapshots.stream()
                .mapToDouble(DefaultAgentSnapshot::averageMs)
                .toArray(), 95.0);
    }

    public double p99Latency() {
        return calculatePercentile(snapshots.stream()
                .mapToDouble(DefaultAgentSnapshot::averageMs)
                .toArray(), 99.0);
    }

    // Additional TrendMetrics methods
    @Override
    public double changeRate() {
        return successRateTrend();
    }

    @Override
    public double trendPercentage() {
        return Math.abs(successRateTrend()) * 100.0;
    }

    @Override
    public String trendDirection() {
        double trend = successRateTrend();
        if (trend > 0.1) {
            return "increasing";
        } else if (trend < -0.1) {
            return "decreasing";
        } else {
            return "stable";
        }
    }

    // Additional PercentileMetrics methods
    @Override
    public double percentile50() {
        return p50Latency();
    }

    @Override
    public double percentile90() {
        return calculatePercentile(snapshots.stream()
                .mapToDouble(DefaultAgentSnapshot::averageMs)
                .toArray(), 90.0);
    }

    @Override
    public double percentile95() {
        return p95Latency();
    }

    @Override
    public double percentile99() {
        return p99Latency();
    }

    /**
     * Get the average success rate across all snapshots.
     * 
     * @return average success rate
     */
    public double averageSuccessRate() {
        return snapshots.stream()
                .mapToDouble(DefaultAgentSnapshot::successRate)
                .average()
                .orElse(0.0);
    }

    /**
     * Get the average execution efficiency across all snapshots.
     * 
     * @return average execution efficiency
     */
    public double averageExecutionEfficiency() {
        return snapshots.stream()
                .mapToDouble(DefaultAgentSnapshot::executionEfficiency)
                .average()
                .orElse(0.0);
    }

    /**
     * Get the total executions across all snapshots.
     * 
     * @return total executions
     */
    public long totalExecutions() {
        return snapshots.stream()
                .mapToLong(DefaultAgentSnapshot::totalExecutions)
                .sum();
    }

    /**
     * Get the peak execution count in any single snapshot.
     * 
     * @return peak execution count
     */
    public long peakExecutionCount() {
        return snapshots.stream()
                .mapToLong(DefaultAgentSnapshot::total)
                .max()
                .orElse(0L);
    }

    /**
     * Get the minimum execution count in any single snapshot.
     * 
     * @return minimum execution count
     */
    public long minExecutionCount() {
        return snapshots.stream()
                .mapToLong(DefaultAgentSnapshot::total)
                .min()
                .orElse(0L);
    }

    /**
     * Calculate trend using linear regression.
     * 
     * @param values the values to analyze
     * @return trend value (positive = increasing, negative = decreasing)
     */
    private double calculateTrend(List<Double> values) {
        if (values.size() < 2) {
            return 0.0;
        }
        
        int n = values.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = values.get(i);
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }
        
        double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        return slope;
    }

    /**
     * Calculate percentile value.
     * 
     * @param values the values to analyze
     * @param percentile the percentile to calculate (0-100)
     * @return percentile value
     */
    private double calculatePercentile(double[] values, double percentile) {
        if (values.length == 0) {
            return 0.0;
        }
        
        java.util.Arrays.sort(values);
        double index = (percentile / 100.0) * (values.length - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);
        
        if (lower == upper) {
            return values[lower];
        }
        
        double weight = index - lower;
        return values[lower] * (1 - weight) + values[upper] * weight;
    }
}
