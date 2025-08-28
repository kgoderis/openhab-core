package org.openhab.core.ai.common.monitoring.statistics;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.ThroughputMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;
import org.openhab.core.ai.common.monitoring.snapshot.ThroughputSnapshot;

/**
 * Statistics for throughput operations over time ranges.
 * 
 * This class provides computed statistics and insights about throughput
 * derived from metrics data over time ranges. It implements capability interfaces
 * for clean, type-safe statistics access.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ThroughputStatistics
        implements StatisticsSnapshot, ThroughputMetrics, PercentileMetrics, TrendMetrics {

    private final List<ThroughputSnapshot> snapshots;
    private final Duration timeRange;
    private final long timestampMs;

    /**
     * Constructor for ThroughputStatistics.
     * 
     * @param snapshots the snapshots to compute statistics from
     * @param timeRange the time range these statistics cover
     */
    public ThroughputStatistics(List<ThroughputSnapshot> snapshots, Duration timeRange) {
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

    // ThroughputMetrics implementation
    @Override
    public double operationsPerSecond() {
        // Get the most recent throughput
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.get(snapshots.size() - 1).operationsPerSecond();
    }

    @Override
    public double throughput() {
        return operationsPerSecond();
    }

    @Override
    public double peakOperationsPerSecond() {
        return snapshots.stream().mapToDouble(ThroughputSnapshot::peakOperationsPerSecond).max().orElse(0.0);
    }

    @Override
    public double averageOperationsPerSecond() {
        return snapshots.stream().mapToDouble(ThroughputSnapshot::operationsPerSecond).average().orElse(0.0);
    }

    @Override
    public double minimumOperationsPerSecond() {
        return snapshots.stream().mapToDouble(ThroughputSnapshot::operationsPerSecond).min().orElse(0.0);
    }

    @Override
    public double throughputEfficiency() {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        return snapshots.stream().mapToDouble(ThroughputSnapshot::throughputEfficiency).average().orElse(0.0);
    }

    @Override
    public long throughputWindowSeconds() {
        return timeRange.toSeconds();
    }

    @Override
    public long activeOperations() {
        // Get the most recent active operations count
        if (snapshots.isEmpty()) {
            return 0;
        }
        return snapshots.get(snapshots.size() - 1).activeOperations();
    }

    @Override
    public long maxConcurrentOperations() {
        return snapshots.stream().mapToLong(ThroughputSnapshot::maxConcurrentOperations).max().orElse(0);
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        return calculateThroughputPercentile(0.5);
    }

    @Override
    public double percentile90() {
        return calculateThroughputPercentile(0.90);
    }

    @Override
    public double percentile95() {
        return calculateThroughputPercentile(0.95);
    }

    @Override
    public double percentile99() {
        return calculateThroughputPercentile(0.99);
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        double firstThroughput = snapshots.get(0).operationsPerSecond();
        double lastThroughput = snapshots.get(snapshots.size() - 1).operationsPerSecond();
        if (firstThroughput == 0.0) {
            return lastThroughput > 0 ? 100.0 : 0.0;
        }
        return ((lastThroughput - firstThroughput) / firstThroughput) * 100.0;
    }

    @Override
    public String trendDirection() {
        double trend = trendPercentage();
        if (trend > 10.0)
            return "INCREASING";
        if (trend < -10.0)
            return "DECREASING";
        return "STABLE";
    }

    @Override
    public double changeRate() {
        if (snapshots.size() < 2 || timeRange.isZero()) {
            return 0.0;
        }
        double firstThroughput = snapshots.get(0).operationsPerSecond();
        double lastThroughput = snapshots.get(snapshots.size() - 1).operationsPerSecond();
        return (lastThroughput - firstThroughput) / timeRange.toMinutes(); // change per minute
    }

    /**
     * Calculate average throughput over the time range.
     * 
     * @return average throughput
     */
    public double getAverageThroughput() {
        return averageOperationsPerSecond();
    }

    /**
     * Get the throughput trend over the time range.
     * 
     * @return positive value indicates increasing throughput, negative indicates decreasing
     */
    public double getThroughputTrend() {
        if (snapshots.size() < 2) {
            return 0.0;
        }

        // Simple linear trend calculation
        double firstThroughput = snapshots.get(0).operationsPerSecond();
        double lastThroughput = snapshots.get(snapshots.size() - 1).operationsPerSecond();
        return lastThroughput - firstThroughput;
    }

    /**
     * Calculate throughput stability score (0.0-1.0).
     * 
     * @return stability score where 1.0 is perfectly stable
     */
    public double getThroughputStabilityScore() {
        if (snapshots.isEmpty()) {
            return 1.0;
        }

        double average = averageOperationsPerSecond();
        if (average == 0.0) {
            return 1.0;
        }

        double variance = snapshots.stream().mapToDouble(s -> Math.abs(s.operationsPerSecond() - average)).average()
                .orElse(0.0);

        return Math.max(0.0, 1.0 - (variance / average));
    }

    /**
     * Calculate average throughput efficiency across all snapshots.
     * 
     * @return average efficiency (0.0-1.0)
     */
    public double getAverageThroughputEfficiency() {
        return throughputEfficiency();
    }

    /**
     * Get snapshots with low throughput performance.
     * 
     * @return list of underperforming snapshots
     */
    public List<ThroughputSnapshot> getLowPerformanceSnapshots() {
        return snapshots.stream().filter(s -> s.getThroughputPerformanceScore() < 0.7).collect(Collectors.toList());
    }

    /**
     * Get snapshots at peak capacity.
     * 
     * @return list of peak capacity snapshots
     */
    public List<ThroughputSnapshot> getPeakCapacitySnapshots() {
        return snapshots.stream().filter(ThroughputSnapshot::isAtPeakCapacity).collect(Collectors.toList());
    }

    /**
     * Get snapshots that are underutilized.
     * 
     * @return list of underutilized snapshots
     */
    public List<ThroughputSnapshot> getUnderutilizedSnapshots() {
        return snapshots.stream().filter(ThroughputSnapshot::isUnderutilized).collect(Collectors.toList());
    }

    /**
     * Check if throughput requires attention based on trends and performance.
     * 
     * @return true if attention is required
     */
    public boolean requiresAttention() {
        if (snapshots.isEmpty()) {
            return false;
        }

        // Check if throughput is declining significantly
        if (getThroughputTrend() < -0.3 * averageOperationsPerSecond()) {
            return true;
        }

        // Check if stability is low
        if (getThroughputStabilityScore() < 0.5) {
            return true;
        }

        // Check if efficiency is consistently low
        if (getAverageThroughputEfficiency() < 0.4) {
            return true;
        }

        // Check if too many snapshots show poor performance
        double lowPerformanceRatio = (double) getLowPerformanceSnapshots().size() / snapshots.size();
        if (lowPerformanceRatio > 0.6) {
            return true;
        }

        return false;
    }

    /**
     * Get the best throughput observed.
     * 
     * @return maximum throughput
     */
    public double getBestThroughput() {
        return snapshots.stream().mapToDouble(ThroughputSnapshot::operationsPerSecond).max().orElse(0.0);
    }

    /**
     * Get the worst throughput observed.
     * 
     * @return minimum throughput
     */
    public double getWorstThroughput() {
        return minimumOperationsPerSecond();
    }

    /**
     * Calculate throughput growth rate.
     * 
     * @return growth rate as percentage per minute
     */
    public double getThroughputGrowthRate() {
        if (timeRange.isZero()) {
            return 0.0;
        }
        return (changeRate() * 100.0); // Convert to percentage
    }

    /**
     * Get comprehensive throughput optimization recommendations.
     * 
     * @return list of optimization suggestions
     */
    public List<String> getOptimizationRecommendations() {
        java.util.List<String> recommendations = new java.util.ArrayList<>();

        if (requiresAttention()) {
            recommendations.add("Throughput performance requires immediate attention");
        }

        if (getThroughputTrend() < 0) {
            recommendations.add("Throughput is declining - investigate system bottlenecks and resource constraints");
        }

        if (getThroughputStabilityScore() < 0.7) {
            recommendations.add("Throughput is unstable - implement load balancing and capacity planning");
        }

        if (getAverageThroughputEfficiency() < 0.6) {
            recommendations.add("Low throughput efficiency - optimize processing algorithms and resource allocation");
        }

        double underutilizedRatio = (double) getUnderutilizedSnapshots().size() / snapshots.size();
        if (underutilizedRatio > 0.5) {
            recommendations.add("System is frequently underutilized - consider resource rightsizing");
        }

        double peakCapacityRatio = (double) getPeakCapacitySnapshots().size() / snapshots.size();
        if (peakCapacityRatio > 0.3) {
            recommendations.add("System frequently operates at peak capacity - consider scaling up");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Throughput performance is optimal across the time range");
        }

        return recommendations;
    }

    private double calculateThroughputPercentile(double percentile) {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        List<Double> throughputValues = snapshots.stream().mapToDouble(ThroughputSnapshot::operationsPerSecond).sorted()
                .boxed().collect(Collectors.toList());

        int index = (int) Math.ceil(percentile * throughputValues.size()) - 1;
        index = Math.max(0, Math.min(index, throughputValues.size() - 1));

        return throughputValues.get(index);
    }
}
