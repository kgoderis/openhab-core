package org.openhab.core.ai.common.monitoring.snapshot;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.ReasoningMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;

/**
 * Immutable statistics record for reasoning engine metrics analysis.
 * 
 * <p>
 * This record provides statistical analysis and trend calculations for reasoning engine
 * metrics over time, including percentile calculations, trend analysis, and
 * reasoning-specific aggregations from multiple snapshots.
 * </p>
 * 
 * @param snapshots list of reasoning engine snapshots for analysis
 * @param timeRange time range covered by the snapshots
 * @param timestampMs timestamp when statistics were calculated
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ReasoningEngineStatistics(List<ReasoningEngineSnapshot> snapshots, Duration timeRange,
        long timestampMs) implements TrendMetrics, PercentileMetrics, ReasoningMetrics {

    public ReasoningEngineStatistics {
        snapshots = List.copyOf(Objects.requireNonNull(snapshots, "snapshots cannot be null"));
        Objects.requireNonNull(timeRange, "timeRange cannot be null");

        if (timeRange.isNegative()) {
            throw new IllegalArgumentException("timeRange cannot be negative");
        }
        if (timestampMs < 0) {
            throw new IllegalArgumentException("timestampMs cannot be negative");
        }
    }

    // TrendMetrics implementation
    @Override
    public double trendPercentage() {
        if (snapshots.size() < 2)
            return 0.0;

        long firstTotal = snapshots.get(0).total();
        long lastTotal = snapshots.get(snapshots.size() - 1).total();

        if (firstTotal == 0)
            return lastTotal > 0 ? 100.0 : 0.0;
        return ((double) (lastTotal - firstTotal) / firstTotal) * 100.0;
    }

    @Override
    public String trendDirection() {
        double trendPct = trendPercentage();
        if (trendPct > 5.0)
            return "increasing";
        if (trendPct < -5.0)
            return "decreasing";
        return "stable";
    }

    @Override
    public double changeRate() {
        if (snapshots.size() < 2)
            return 0.0;

        // Calculate change rate per second
        long firstTimestamp = snapshots.get(0).timestampMs();
        long lastTimestamp = snapshots.get(snapshots.size() - 1).timestampMs();
        long firstTotal = snapshots.get(0).total();
        long lastTotal = snapshots.get(snapshots.size() - 1).total();

        long timeDiffSeconds = (lastTimestamp - firstTimestamp) / 1000;
        if (timeDiffSeconds == 0)
            return 0.0;

        return (double) (lastTotal - firstTotal) / timeDiffSeconds;
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
        if (snapshots.isEmpty())
            return 0.0;

        var sortedLatencies = snapshots.stream()
                .mapToDouble(s -> s.total() > 0 ? s.totalDurationNanos() / 1_000_000.0 / s.total() : 0.0).sorted()
                .toArray();

        if (sortedLatencies.length == 0)
            return 0.0;

        int index = (int) Math.ceil(percentile / 100.0 * sortedLatencies.length) - 1;
        index = Math.max(0, Math.min(index, sortedLatencies.length - 1));

        return sortedLatencies[index];
    }

    // ReasoningMetrics implementation
    @Override
    public double reasoningAccuracy() {
        return snapshots.stream().mapToDouble(ReasoningEngineSnapshot::reasoningAccuracy).average().orElse(0.0);
    }

    @Override
    public double reasoningLatency() {
        return snapshots.stream().mapToDouble(ReasoningEngineSnapshot::reasoningLatency).average().orElse(0.0);
    }

    @Override
    public double reasoningComplexity() {
        return snapshots.stream().mapToDouble(ReasoningEngineSnapshot::reasoningComplexity).average().orElse(0.0);
    }

    @Override
    public double reasoningEfficiency() {
        return snapshots.stream().mapToDouble(ReasoningEngineSnapshot::reasoningEfficiency).average().orElse(0.0);
    }

    @Override
    public double reasoningThroughput() {
        return snapshots.stream().mapToDouble(ReasoningEngineSnapshot::reasoningThroughput).average().orElse(0.0);
    }

    @Override
    public double reasoningErrorRate() {
        return snapshots.stream().mapToDouble(ReasoningEngineSnapshot::reasoningErrorRate).average().orElse(0.0);
    }

    @Override
    public double reasoningConfidence() {
        return snapshots.stream().mapToDouble(ReasoningEngineSnapshot::reasoningConfidence).average().orElse(0.0);
    }

    @Override
    public long reasoningStepCount() {
        return snapshots.stream().mapToLong(ReasoningEngineSnapshot::reasoningStepCount).sum();
    }

    @Override
    public double reasoningBacktrackingRate() {
        return snapshots.stream().mapToDouble(ReasoningEngineSnapshot::reasoningBacktrackingRate).average().orElse(0.0);
    }

    @Override
    public double reasoningOptimizationLevel() {
        return snapshots.stream().mapToDouble(ReasoningEngineSnapshot::reasoningOptimizationLevel).average()
                .orElse(0.0);
    }

    /**
     * Get the total number of active agents across all snapshots.
     * 
     * @return total active agents
     */
    public long totalActiveAgents() {
        return snapshots.stream().mapToLong(ReasoningEngineSnapshot::activeAgents).sum();
    }

    /**
     * Get the total number of active sessions across all snapshots.
     * 
     * @return total active sessions
     */
    public long totalActiveSessions() {
        return snapshots.stream().mapToLong(ReasoningEngineSnapshot::activeSessions).sum();
    }

    /**
     * Get the average number of active agents.
     * 
     * @return average active agents
     */
    public double averageActiveAgents() {
        return snapshots.stream().mapToLong(ReasoningEngineSnapshot::activeAgents).average().orElse(0.0);
    }

    /**
     * Get the average number of active sessions.
     * 
     * @return average active sessions
     */
    public double averageActiveSessions() {
        return snapshots.stream().mapToLong(ReasoningEngineSnapshot::activeSessions).average().orElse(0.0);
    }
}
