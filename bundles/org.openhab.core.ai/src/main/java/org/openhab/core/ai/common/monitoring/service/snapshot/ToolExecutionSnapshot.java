package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.Timing;
import org.openhab.core.ai.common.monitoring.api.ToolMetrics;

/**
 * Immutable snapshot of tool execution performance metrics.
 *
 * <p>
 * This record provides comprehensive performance metrics for tool operations including
 * execution counts, success rates, latency metrics, and throughput. It implements
 * capability interfaces for clean, type-safe metrics access.
 * </p>
 *
 * @param counts basic count metrics (total, success, failure)
 * @param timing latency and duration metrics
 * @param timestampMs timestamp when snapshot was created
 * @param toolId unique identifier for the tool
 * @param operationsPerSecond current operations per second
 * @param itemsPerSecond current items per second (processed data items)
 * @param peakThroughput peak throughput achieved
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ToolExecutionSnapshot(Counts counts, Timing timing, long timestampMs, String toolId,
        double operationsPerSecond, double itemsPerSecond,
        long peakThroughput) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, ToolMetrics {

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

    @Override
    public double successRate() {
        return CountsMetrics.super.successRate();
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return timing.totalDurationNanos();
    }

    public double averageMs() {
        return LatencyMetrics.super.averageMs(total());
    }

    @Override
    public double operationsPerSecond() {
        return this.operationsPerSecond;
    }

    // ToolMetrics implementation
    @Override
    public double throughputPerSecond() {
        return itemsPerSecond;
    }

    @Override
    public double averageResourceUsage() {
        // Calculate resource usage based on performance
        if (total() == 0)
            return 0.0;

        // Higher throughput with lower latency indicates better resource usage
        double latencyScore = averageMs() > 0 ? Math.min(1.0, 100.0 / averageMs()) : 1.0;
        double throughputScore = Math.min(1.0, operationsPerSecond / 100.0);

        return (latencyScore + throughputScore) / 2.0;
    }

    @Override
    public int maxConcurrentExecutions() {
        // Estimate max concurrency based on throughput and average response time
        if (averageMs() == 0)
            return 1;

        double estimatedConcurrency = (operationsPerSecond * averageMs()) / 1000.0;
        return Math.max(1, (int) Math.ceil(estimatedConcurrency));
    }

    // Domain-specific methods
    public double getFailureRate() {
        return total() > 0 ? (double) failure() / total() : 0.0;
    }

    public double getThroughputEfficiency() {
        if (peakThroughput == 0)
            return 1.0;
        return Math.min(1.0, operationsPerSecond / peakThroughput);
    }

    public double getPerformanceScore() {
        double successRateDecimal = successRate() / 100.0;
        double efficiencyScore = getThroughputEfficiency();
        double resourceScore = averageResourceUsage();

        return (successRateDecimal * 0.4) + (efficiencyScore * 0.3) + (resourceScore * 0.3);
    }

    /**
     * Create an empty ToolExecutionSnapshot for the given tool.
     */
    public static ToolExecutionSnapshot empty(String toolId) {
        return new ToolExecutionSnapshot(new Counts(0, 0, 0), new Timing(0), System.currentTimeMillis(), toolId, 0.0,
                0.0, 0);
    }
}
