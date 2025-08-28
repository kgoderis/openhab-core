package org.openhab.core.ai.common.monitoring.service.statistics;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.ToolMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;

/**
 * Immutable statistics snapshot for tool execution performance analysis.
 * 
 * <p>
 * This record provides comprehensive statistics for tool execution including
 * trend analysis, percentile calculations, and performance metrics. It supports
 * statistical analysis over time periods for tool optimization.
 * </p>
 * 
 * @param toolId unique identifier for the tool
 * @param totalExecutions total number of executions analyzed
 * @param totalSuccesses total number of successful executions
 * @param totalFailures total number of failed executions
 * @param averageLatencyMs average execution latency in milliseconds
 * @param totalThroughput total throughput operations
 * @param peakThroughput peak throughput achieved
 * @param trendPercentage trend percentage (positive for increasing, negative for decreasing)
 * @param changeRate change rate per time unit
 * @param percentile50 median latency in milliseconds
 * @param percentile90 90th percentile latency in milliseconds
 * @param percentile95 95th percentile latency in milliseconds
 * @param percentile99 99th percentile latency in milliseconds
 * @param timestampMs timestamp when statistics were calculated
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ToolExecutionStatistics(String toolId, long totalExecutions, long totalSuccesses, long totalFailures,
        double averageLatencyMs, long totalThroughput, long peakThroughput, double trendPercentage, double changeRate,
        double percentile50, double percentile90, double percentile95, double percentile99,
        long timestampMs) implements StatisticsSnapshot, TrendMetrics, PercentileMetrics, ToolMetrics {

    public ToolExecutionStatistics {
        Objects.requireNonNull(toolId, "toolId");
        if (totalExecutions < 0) {
            throw new IllegalArgumentException("totalExecutions must be >= 0");
        }
        if (totalSuccesses < 0) {
            throw new IllegalArgumentException("totalSuccesses must be >= 0");
        }
        if (totalFailures < 0) {
            throw new IllegalArgumentException("totalFailures must be >= 0");
        }
        if (averageLatencyMs < 0) {
            throw new IllegalArgumentException("averageLatencyMs must be >= 0");
        }
        if (totalThroughput < 0) {
            throw new IllegalArgumentException("totalThroughput must be >= 0");
        }
        if (peakThroughput < 0) {
            throw new IllegalArgumentException("peakThroughput must be >= 0");
        }
        if (timestampMs < 0) {
            throw new IllegalArgumentException("timestampMs must be >= 0");
        }
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        return trendPercentage;
    }

    @Override
    public String trendDirection() {
        if (trendPercentage > 1.0) {
            return "increasing";
        } else if (trendPercentage < -1.0) {
            return "decreasing";
        } else {
            return "stable";
        }
    }

    @Override
    public double changeRate() {
        return changeRate;
    }

    // PercentileMetrics implementation
    @Override
    public double percentile50() {
        return percentile50;
    }

    @Override
    public double percentile90() {
        return percentile90;
    }

    @Override
    public double percentile95() {
        return percentile95;
    }

    @Override
    public double percentile99() {
        return percentile99;
    }

    // ToolMetrics implementation
    @Override
    public double throughputPerSecond() {
        return totalThroughput; // Total throughput over the measurement period
    }

    @Override
    public double averageResourceUsage() {
        // Calculate resource usage based on performance metrics
        if (totalExecutions == 0) {
            return 0.0;
        }

        // Higher success rate and lower latency indicate better resource usage
        double successRate = (double) totalSuccesses / totalExecutions;
        double latencyScore = averageLatencyMs > 0 ? Math.min(1.0, 100.0 / averageLatencyMs) : 1.0;
        double throughputScore = peakThroughput > 0 ? Math.min(1.0, (double) totalThroughput / peakThroughput) : 1.0;

        return (successRate * 0.5) + (latencyScore * 0.3) + (throughputScore * 0.2);
    }

    @Override
    public int maxConcurrentExecutions() {
        // Estimate max concurrency based on throughput and average response time
        if (averageLatencyMs == 0 || totalThroughput == 0) {
            return 1;
        }

        // Simple estimation: throughput * latency = concurrent executions
        double estimatedConcurrency = (totalThroughput * averageLatencyMs) / 1000.0;
        return Math.max(1, (int) Math.ceil(estimatedConcurrency));
    }

    // Domain-specific methods

    /**
     * Calculate success rate as a percentage.
     * 
     * @return success rate as percentage (0.0-100.0)
     */
    public double getSuccessRate() {
        return totalExecutions > 0 ? (totalSuccesses * 100.0) / totalExecutions : 0.0;
    }

    /**
     * Calculate failure rate as a percentage.
     * 
     * @return failure rate as percentage (0.0-100.0)
     */
    public double getFailureRate() {
        return totalExecutions > 0 ? (totalFailures * 100.0) / totalExecutions : 0.0;
    }

    /**
     * Calculate throughput efficiency compared to peak.
     * 
     * @return efficiency ratio (0.0-1.0)
     */
    public double getThroughputEfficiency() {
        return peakThroughput > 0 ? Math.min(1.0, (double) totalThroughput / peakThroughput) : 1.0;
    }

    /**
     * Calculate overall performance score.
     * 
     * @return performance score (0.0-1.0)
     */
    public double getPerformanceScore() {
        double successRateDecimal = getSuccessRate() / 100.0;
        double efficiencyScore = getThroughputEfficiency();
        double resourceScore = averageResourceUsage();

        return (successRateDecimal * 0.4) + (efficiencyScore * 0.3) + (resourceScore * 0.3);
    }

    /**
     * Check if the tool performance is trending upward.
     * 
     * @return true if performance is improving
     */
    public boolean isImproving() {
        return trendPercentage > 0 && getSuccessRate() > 95.0;
    }

    /**
     * Check if the tool performance is degrading.
     * 
     * @return true if performance is degrading
     */
    public boolean isDegrading() {
        return trendPercentage < -5.0 || getSuccessRate() < 90.0 || averageLatencyMs > percentile95;
    }

    /**
     * Create an empty ToolExecutionStatistics for the given tool.
     * 
     * @param toolId unique identifier for the tool
     * @return empty statistics snapshot
     */
    public static ToolExecutionStatistics empty(String toolId) {
        return new ToolExecutionStatistics(toolId, 0, // totalExecutions
                0, // totalSuccesses
                0, // totalFailures
                0.0, // averageLatencyMs
                0, // totalThroughput
                0, // peakThroughput
                0.0, // trendPercentage
                0.0, // changeRate
                0.0, // percentile50
                0.0, // percentile90
                0.0, // percentile95
                0.0, // percentile99
                System.currentTimeMillis() // timestampMs
        );
    }

    @Override
    public String toString() {
        return new StringBuilder("ToolExecutionStatistics{").append("toolId='").append(toolId).append('\'')
                .append(", executions=").append(totalExecutions).append(", successRate=")
                .append(String.format("%.1f%%", getSuccessRate())).append(", avgLatency=")
                .append(String.format("%.1fms", averageLatencyMs)).append(", throughput=").append(totalThroughput)
                .append(", trend=").append(trendDirection()).append(", p95=")
                .append(String.format("%.1fms", percentile95)).append('}').toString();
    }
}
