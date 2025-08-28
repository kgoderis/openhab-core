package org.openhab.core.ai.agent.execution;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

/**
 * Statistical analysis of task execution performance over time.
 * 
 * <p>
 * This class provides advanced statistical analysis including trends,
 * percentiles, and historical performance data for task execution operations.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record TaskExecutionStatistics(List<TaskExecutionSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics {

    /**
     * Factory method to create statistics from historical data.
     * 
     * @param snapshots list of task execution snapshots
     * @param timeRange time range for statistics
     * @return new TaskExecutionStatistics instance
     */
    public static TaskExecutionStatistics of(List<TaskExecutionSnapshot> snapshots, Duration timeRange) {
        return new TaskExecutionStatistics(List.copyOf(snapshots), timeRange, System.currentTimeMillis());
    }

    /**
     * Factory method to create empty statistics.
     * 
     * @return empty TaskExecutionStatistics instance
     */
    public static TaskExecutionStatistics empty() {
        return new TaskExecutionStatistics(List.of(), Duration.ZERO, System.currentTimeMillis());
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

    /**
     * Get the average retry rate across all snapshots.
     * 
     * @return average retry rate percentage
     */
    public double averageRetryRate() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.retryRate()).average().orElse(0.0);
    }

    /**
     * Get the average execution efficiency across all snapshots.
     * 
     * @return average execution efficiency
     */
    public double averageExecutionEfficiency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(s -> s.executionEfficiency()).average().orElse(0.0);
    }

    /**
     * Get the total number of tasks executed across all snapshots.
     * 
     * @return total task count
     */
    public long totalTasksExecuted() {
        return snapshots.stream().mapToLong(s -> s.total()).sum();
    }

    /**
     * Get the total number of successful tasks across all snapshots.
     * 
     * @return total success count
     */
    public long totalSuccessfulTasks() {
        return snapshots.stream().mapToLong(s -> s.success()).sum();
    }

    /**
     * Get the total number of failed tasks across all snapshots.
     * 
     * @return total failure count
     */
    public long totalFailedTasks() {
        return snapshots.stream().mapToLong(s -> s.failure()).sum();
    }

    /**
     * Create a summary string with key statistics.
     * 
     * @return summary string
     */
    public String toSummary() {
        return String.format(
                "TaskExecutionStatistics{trend=%s, successRate=%.1f%%, p95Latency=%.2fms, avgRetryRate=%.1f%%, efficiency=%.1f%%}",
                trendDirection(), percentile50(), percentile95(), averageRetryRate(), averageExecutionEfficiency());
    }
}

