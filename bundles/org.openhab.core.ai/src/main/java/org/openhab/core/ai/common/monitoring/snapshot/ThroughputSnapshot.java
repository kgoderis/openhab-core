package org.openhab.core.ai.common.monitoring.snapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.ThroughputMetrics;
import org.openhab.core.ai.common.monitoring.api.Timing;

/**
 * Snapshot of throughput metrics at a specific point in time.
 * 
 * This class provides real-time throughput metrics implementing CountsMetrics,
 * LatencyMetrics, and ThroughputMetrics interfaces for clean, type-safe access
 * to throughput data.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ThroughputSnapshot(Counts counts, Timing timing, long timestampMs, double currentThroughput,
        double peakThroughput, double averageThroughput, double minimumThroughput, long activeOperations,
        long maxConcurrentOperations,
        long throughputWindowSeconds) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, ThroughputMetrics {

    public ThroughputSnapshot {
        Objects.requireNonNull(counts, "counts");
        Objects.requireNonNull(timing, "timing");
    }

    // CountsMetrics implementation
    @Override
    public long total() {
        return counts.total();
    }

    @Override
    public long success() {
        return counts.success();
    }

    @Override
    public long failure() {
        return counts.failure();
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return timing.totalDurationNanos();
    }

    /**
     * Get the average response time in milliseconds.
     *
     * @return average response time in milliseconds
     */
    public double averageMs() {
        return LatencyMetrics.super.averageMs(total());
    }

    // ThroughputMetrics implementation
    @Override
    public double operationsPerSecond() {
        return currentThroughput;
    }

    @Override
    public double throughput() {
        return currentThroughput;
    }

    @Override
    public double peakOperationsPerSecond() {
        return peakThroughput;
    }

    @Override
    public double averageOperationsPerSecond() {
        return averageThroughput;
    }

    @Override
    public double minimumOperationsPerSecond() {
        return minimumThroughput;
    }

    @Override
    public double throughputEfficiency() {
        if (peakThroughput == 0.0) {
            return 1.0;
        }
        return Math.min(1.0, currentThroughput / peakThroughput);
    }

    @Override
    public long throughputWindowSeconds() {
        return throughputWindowSeconds;
    }

    @Override
    public long activeOperations() {
        return activeOperations;
    }

    @Override
    public long maxConcurrentOperations() {
        return maxConcurrentOperations;
    }

    /**
     * Calculate throughput relative to capacity.
     * 
     * @return relative throughput (0.0-1.0)
     */
    public double getRelativeThroughput() {
        if (maxConcurrentOperations == 0) {
            return 0.0;
        }
        return Math.min(1.0, (double) activeOperations / maxConcurrentOperations);
    }

    /**
     * Calculate success rate percentage.
     * 
     * @return success rate as percentage (0.0-100.0)
     */
    public double successRatePercent() {
        if (total() == 0) {
            return 0.0;
        }
        return (success() * 100.0) / total();
    }

    /**
     * Get throughput variance from average.
     * 
     * @return variance from average throughput
     */
    public double getThroughputVariance() {
        return currentThroughput - averageThroughput;
    }

    /**
     * Check if throughput is stable (within 10% of average).
     * 
     * @return true if throughput is stable
     */
    public boolean isThroughputStable() {
        if (averageThroughput == 0.0) {
            return true;
        }
        double variancePercent = Math.abs(getThroughputVariance()) / averageThroughput;
        return variancePercent <= 0.1; // Within 10%
    }

    /**
     * Get throughput performance score (0.0-1.0).
     * 
     * @return performance score based on efficiency and stability
     */
    public double getThroughputPerformanceScore() {
        double efficiency = throughputEfficiency();
        double stability = throughputStability();
        double utilization = getRelativeThroughput();

        return (efficiency + stability + utilization) / 3.0;
    }

    /**
     * Get additional throughput indicators.
     * 
     * @return map of throughput indicators
     */
    public Map<String, Object> getThroughputIndicators() {
        Map<String, Object> indicators = new HashMap<>();
        indicators.put("currentThroughput", currentThroughput);
        indicators.put("peakThroughput", peakThroughput);
        indicators.put("averageThroughput", averageThroughput);
        indicators.put("minimumThroughput", minimumThroughput);
        indicators.put("throughputEfficiency", throughputEfficiency());
        indicators.put("throughputUtilization", throughputUtilizationPercentage());
        indicators.put("throughputStability", throughputStability());
        indicators.put("throughputVariance", getThroughputVariance());
        indicators.put("isThroughputStable", isThroughputStable());
        indicators.put("isAtPeakCapacity", isAtPeakCapacity());
        indicators.put("isUnderutilized", isUnderutilized());
        indicators.put("performanceScore", getThroughputPerformanceScore());
        indicators.put("activeOperations", activeOperations);
        indicators.put("maxConcurrentOperations", maxConcurrentOperations);
        indicators.put("relativeThroughput", getRelativeThroughput());
        indicators.put("successRate", successRatePercent());
        indicators.put("averageLatencyMs", averageMs());
        return indicators;
    }

    /**
     * Check if throughput requires optimization.
     * 
     * @return true if optimization is recommended
     */
    public boolean requiresOptimization() {
        return getThroughputPerformanceScore() < 0.7 || isUnderutilized() || !isThroughputStable()
                || throughputEfficiency() < 0.6;
    }

    /**
     * Get throughput optimization recommendations.
     * 
     * @return list of optimization suggestions
     */
    public java.util.List<String> getOptimizationRecommendations() {
        java.util.List<String> recommendations = new java.util.ArrayList<>();

        if (isUnderutilized()) {
            recommendations.add("System is underutilized - consider increasing load or reducing resources");
        }

        if (isAtPeakCapacity()) {
            recommendations.add("System is at peak capacity - consider scaling up or load balancing");
        }

        if (!isThroughputStable()) {
            recommendations.add("Throughput is unstable - investigate bottlenecks or resource constraints");
        }

        if (throughputEfficiency() < 0.6) {
            recommendations.add("Low throughput efficiency - optimize processing algorithms or resource allocation");
        }

        if (successRatePercent() < 95.0) {
            recommendations.add("Low success rate affecting throughput - improve error handling and retry mechanisms");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Throughput performance is optimal");
        }

        return recommendations;
    }
}
