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
import org.openhab.core.ai.common.monitoring.service.snapshot.ClientPerformanceSnapshot;

/**
 * Statistics class for client performance metrics.
 * 
 * <p>
 * This class provides comprehensive client performance statistics including
 * trend analysis, percentile calculations, counts, and latency metrics.
 * It aggregates multiple ClientPerformanceSnapshot instances to provide
 * historical and statistical analysis of client performance patterns.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ClientPerformanceStatistics(List<ClientPerformanceSnapshot> snapshots, Duration timeRange,
        long timestampMs)
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

    @Override
    public double averageMs(long total) {
        if (total == 0)
            return 0.0;
        return totalDurationNanos() / (total * 1_000_000.0);
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2)
            return 0.0;
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

    // Factory method
    public static ClientPerformanceStatistics fromClientData(long totalRequests, long successfulRequests,
            long failedRequests, long totalDurationNanos, Duration timeRange) {
        return new ClientPerformanceStatistics(List.of(), timeRange, System.currentTimeMillis());
    }

    /**
     * Create statistics from a list of snapshots.
     * 
     * @param snapshots the list of snapshots to aggregate
     * @param timeRange the time range for the statistics
     * @return client performance statistics
     */
    public static ClientPerformanceStatistics fromSnapshots(List<ClientPerformanceSnapshot> snapshots,
            Duration timeRange) {
        return new ClientPerformanceStatistics(snapshots, timeRange, System.currentTimeMillis());
    }

    // Helper methods
    private double calculateAverageSuccessRate(int start, int end) {
        return snapshots.subList(start, end).stream().mapToDouble(s -> s.successRate()).average().orElse(0.0);
    }

    private double calculatePercentile(double percentile) {
        List<Double> latencies = snapshots.stream()
                .flatMap(s -> Stream.generate(() -> s.averageResponseTime()).limit(s.total())).sorted()
                .collect(Collectors.toList());
        if (latencies.isEmpty())
            return 0.0;
        int index = (int) Math.ceil(percentile * latencies.size()) - 1;
        return latencies.get(Math.max(0, index));
    }
}
