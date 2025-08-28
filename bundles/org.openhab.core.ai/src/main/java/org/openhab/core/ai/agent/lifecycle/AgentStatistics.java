package org.openhab.core.ai.agent.lifecycle;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.StatisticsSnapshot;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;

/**
 * Statistics for agent metrics over a time range.
 * 
 * <p>
 * This class provides statistical analysis of agent metrics including trends,
 * percentiles, and aggregated performance data.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record AgentStatistics(List<AgentSnapshot> snapshots, Duration timeRange, long timestampMs)
        implements StatisticsSnapshot, TrendMetrics, PercentileMetrics {

    /**
     * Create agent statistics from a list of snapshots.
     * 
     * @param snapshots the list of agent snapshots
     * @param timeRange the time range covered by the snapshots
     * @return the agent statistics
     */
    public static AgentStatistics of(List<AgentSnapshot> snapshots, Duration timeRange) {
        return new AgentStatistics(snapshots, timeRange, System.currentTimeMillis());
    }

    /**
     * Get the number of snapshots.
     * 
     * @return number of snapshots
     */
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

    /**
     * Calculate the success rate trend.
     * 
     * @return success rate trend
     */
    public double successRateTrend() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        
        AgentSnapshot first = snapshots.get(0);
        AgentSnapshot last = snapshots.get(snapshots.size() - 1);
        
        double firstRate = first.total() > 0 ? (double) first.success() / first.total() : 0.0;
        double lastRate = last.total() > 0 ? (double) last.success() / last.total() : 0.0;
        
        return lastRate - firstRate;
    }

    /**
     * Calculate the latency trend.
     * 
     * @return latency trend in milliseconds
     */
    public double latencyTrend() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        
        AgentSnapshot first = snapshots.get(0);
        AgentSnapshot last = snapshots.get(snapshots.size() - 1);
        
        double firstLatency = first.averageMs();
        double lastLatency = last.averageMs();
        
        return lastLatency - firstLatency;
    }

    /**
     * Calculate the throughput trend.
     * 
     * @return throughput trend (executions per second)
     */
    public double throughputTrend() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        
        AgentSnapshot first = snapshots.get(0);
        AgentSnapshot last = snapshots.get(snapshots.size() - 1);
        
        double firstThroughput = first.totalExecutions();
        double lastThroughput = last.totalExecutions();
        
        return lastThroughput - firstThroughput;
    }

    /**
     * Calculate the 50th percentile latency.
     * 
     * @return 50th percentile latency in milliseconds
     */
    public double p50Latency() {
        return calculatePercentile(50.0);
    }

    /**
     * Calculate the 95th percentile latency.
     * 
     * @return 95th percentile latency in milliseconds
     */
    public double p95Latency() {
        return calculatePercentile(95.0);
    }

    /**
     * Calculate the 99th percentile latency.
     * 
     * @return 99th percentile latency in milliseconds
     */
    public double p99Latency() {
        return calculatePercentile(99.0);
    }

    /**
     * Calculate a percentile latency.
     * 
     * @param percentile the percentile to calculate (0-100)
     * @return the percentile latency in milliseconds
     */
    private double calculatePercentile(double percentile) {
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        
        List<Double> latencies = snapshots.stream()
            .map(AgentSnapshot::averageMs)
            .sorted()
            .toList();
        
        int index = (int) Math.ceil((percentile / 100.0) * latencies.size()) - 1;
        index = Math.max(0, Math.min(index, latencies.size() - 1));
        
        return latencies.get(index);
    }

    @Override
    public double changeRate() {
        return successRateTrend();
    }

    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2) {
            return 0.0;
        }
        
        AgentSnapshot first = snapshots.get(0);
        double firstRate = first.total() > 0 ? (double) first.success() / first.total() : 0.0;
        double change = successRateTrend();
        
        return firstRate > 0 ? (change / firstRate) * 100.0 : 0.0;
    }

    @Override
    public String trendDirection() {
        double trend = successRateTrend();
        if (trend > 0.01) {
            return "increasing";
        } else if (trend < -0.01) {
            return "decreasing";
        } else {
            return "stable";
        }
    }

    @Override
    public double percentile50() {
        return p50Latency();
    }

    @Override
    public double percentile90() {
        return calculatePercentile(90.0);
    }

    @Override
    public double percentile95() {
        return p95Latency();
    }

    @Override
    public double percentile99() {
        return p99Latency();
    }
}
