package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.AgentTaskSnapshot;

/**
 * Statistics class for filter operations analysis.
 * 
 * <p>
 * This class provides computed statistics and insights about filter operations
 * derived from metrics data over time ranges. It implements capability interfaces
 * for clean, type-safe statistics access.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record FilterOperationsStatistics(List<AgentTaskSnapshot> snapshots, Duration timeRange, long timestampMs)
        implements
            StatisticsSnapshot,
            CountsMetrics,
            LatencyMetrics,
            TrendMetrics,
            PercentileMetrics {

    // CountsMetrics implementation
    @Override
    public long total() {
        return snapshots.stream().mapToLong(s -> s.total()).sum();
    }

    @Override
    public long success() {
        return snapshots.stream().mapToLong(s -> s.success()).sum();
    }

    @Override
    public long failure() {
        return snapshots.stream().mapToLong(s -> s.failure()).sum();
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
        return snapshots.stream().mapToLong(s -> s.totalDurationNanos()).sum();
    }

    public double averageMs() {
        if (total() == 0)
            return 0.0;
        return totalDurationNanos() / (total() * 1_000_000.0);
    }

    public double operationsPerSecond() {
        if (timeRange.toNanos() == 0)
            return 0.0;
        return total() / (timeRange.toNanos() / 1_000_000_000.0);
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2)
            return 0.0;

        // Calculate trend based on success rate over time
        double firstHalf = calculateAverageSuccessRate(0, snapshots.size() / 2);
        double secondHalf = calculateAverageSuccessRate(snapshots.size() / 2, snapshots.size());

        if (firstHalf == 0)
            return 0.0;
        return ((secondHalf - firstHalf) / firstHalf) * 100.0;
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
        return calculatePercentile(0.5);
    }

    @Override
    public double percentile90() {
        return calculatePercentile(0.9);
    }

    @Override
    public double percentile95() {
        return calculatePercentile(0.95);
    }

    @Override
    public double percentile99() {
        return calculatePercentile(0.99);
    }

    // Helper methods
    private double calculateAverageSuccessRate(int start, int end) {
        return snapshots.subList(start, end).stream().mapToDouble(s -> s.successRate()).average().orElse(0.0);
    }

    private double calculatePercentile(double percentile) {
        List<Double> latencies = snapshots.stream().flatMap(s -> Stream.generate(() -> s.averageMs()).limit(s.total()))
                .sorted().collect(Collectors.toList());

        if (latencies.isEmpty())
            return 0.0;

        int index = (int) Math.ceil(percentile * latencies.size()) - 1;
        return latencies.get(Math.max(0, index));
    }

    /**
     * Create statistics from a list of snapshots.
     * 
     * @param snapshots the list of snapshots
     * @param timeRange the time range for statistics
     * @return filter operations statistics
     */
    public static FilterOperationsStatistics fromSnapshots(List<AgentTaskSnapshot> snapshots, Duration timeRange) {
        return new FilterOperationsStatistics(snapshots, timeRange, System.currentTimeMillis());
    }

    /**
     * Create empty statistics.
     * 
     * @param timeRange the time range for statistics
     * @return empty filter operations statistics
     */
    public static FilterOperationsStatistics empty(Duration timeRange) {
        return new FilterOperationsStatistics(List.of(), timeRange, System.currentTimeMillis());
    }
}
