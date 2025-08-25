package org.openhab.core.ai.common.monitoring.service.statistics;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.BusinessMetrics;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.snapshot.ModelCompletionSnapshot;

/**
 * Statistics for model completion operations with capability interfaces.
 * 
 * <p>
 * This class provides comprehensive statistics for model completion operations:
 * - Trend analysis (percentage, direction, change rate)
 * - Percentile analysis (50th, 90th, 95th, 99th percentiles)
 * - Business metrics (cost efficiency, resource utilization, throughput efficiency)
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ModelCompletionStatistics(List<ModelCompletionSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, BusinessMetrics {

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

    // BusinessMetrics implementation
    @Override
    public double costEfficiency() {
        if (snapshots.isEmpty())
            return 0.0;

        double totalCost = snapshots.stream().mapToDouble(s -> s.totalCost()).sum();
        long totalOperations = snapshots.stream().mapToLong(s -> s.total()).sum();

        if (totalOperations == 0)
            return 0.0;
        return totalCost / totalOperations;
    }

    @Override
    public double resourceUtilization() {
        if (snapshots.isEmpty())
            return 0.0;

        long totalDuration = snapshots.stream().mapToLong(s -> s.totalDurationNanos()).sum();
        long totalOperations = snapshots.stream().mapToLong(s -> s.total()).sum();

        if (totalOperations == 0)
            return 0.0;
        return (double) totalDuration / (totalOperations * timeRange.toNanos());
    }

    @Override
    public double throughputEfficiency() {
        if (snapshots.isEmpty())
            return 0.0;

        long totalOperations = snapshots.stream().mapToLong(s -> s.total()).sum();

        return (double) totalOperations / timeRange.toHours();
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
}
