package org.openhab.core.ai.common.monitoring.snapshot;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.ReasoningMetrics;
import org.openhab.core.ai.common.monitoring.api.Timing;

/**
 * Immutable snapshot of reasoning engine metrics data.
 * 
 * <p>
 * This record captures a point-in-time view of reasoning engine metrics including
 * execution counts, timing data, reasoning-specific metrics like accuracy, complexity,
 * and efficiency measurements. Used with the centralized {@link MetricsService}.
 * </p>
 * 
 * @param counts execution count data (total, success, failure)
 * @param timing execution timing data (total duration in nanoseconds)
 * @param activeAgents number of currently active agents
 * @param activeSessions number of currently active reasoning sessions
 * @param reasoningAccuracy reasoning accuracy as percentage (0.0-100.0)
 * @param reasoningComplexity reasoning complexity score (0-100)
 * @param reasoningEfficiency reasoning efficiency score (0-100)
 * @param reasoningThroughput reasoning throughput in operations per second
 * @param reasoningConfidence reasoning confidence level (0-100)
 * @param reasoningStepCount total reasoning step count
 * @param reasoningBacktrackingRate backtracking rate as percentage (0.0-100.0)
 * @param reasoningOptimizationLevel optimization level (0-100)
 * @param timestampMs timestamp when snapshot was taken
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ReasoningEngineSnapshot(Counts counts, Timing timing, long activeAgents, long activeSessions,
        double reasoningAccuracy, double reasoningComplexity, double reasoningEfficiency, double reasoningThroughput,
        double reasoningConfidence, long reasoningStepCount, double reasoningBacktrackingRate,
        double reasoningOptimizationLevel,
        long timestampMs) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, ReasoningMetrics {

    public ReasoningEngineSnapshot {
        Objects.requireNonNull(counts, "counts cannot be null");
        Objects.requireNonNull(timing, "timing cannot be null");

        if (activeAgents < 0) {
            throw new IllegalArgumentException("activeAgents cannot be negative");
        }
        if (activeSessions < 0) {
            throw new IllegalArgumentException("activeSessions cannot be negative");
        }
        if (reasoningAccuracy < 0.0 || reasoningAccuracy > 100.0) {
            throw new IllegalArgumentException("reasoningAccuracy must be between 0.0 and 100.0");
        }
        if (reasoningComplexity < 0.0 || reasoningComplexity > 100.0) {
            throw new IllegalArgumentException("reasoningComplexity must be between 0.0 and 100.0");
        }
        if (reasoningEfficiency < 0.0 || reasoningEfficiency > 100.0) {
            throw new IllegalArgumentException("reasoningEfficiency must be between 0.0 and 100.0");
        }
        if (reasoningThroughput < 0.0) {
            throw new IllegalArgumentException("reasoningThroughput cannot be negative");
        }
        if (reasoningConfidence < 0.0 || reasoningConfidence > 100.0) {
            throw new IllegalArgumentException("reasoningConfidence must be between 0.0 and 100.0");
        }
        if (reasoningStepCount < 0) {
            throw new IllegalArgumentException("reasoningStepCount cannot be negative");
        }
        if (reasoningBacktrackingRate < 0.0 || reasoningBacktrackingRate > 100.0) {
            throw new IllegalArgumentException("reasoningBacktrackingRate must be between 0.0 and 100.0");
        }
        if (reasoningOptimizationLevel < 0.0 || reasoningOptimizationLevel > 100.0) {
            throw new IllegalArgumentException("reasoningOptimizationLevel must be between 0.0 and 100.0");
        }
        if (timestampMs < 0) {
            throw new IllegalArgumentException("timestampMs cannot be negative");
        }
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

    // ReasoningMetrics implementation
    @Override
    public double reasoningLatency() {
        return total() > 0 ? (double) totalDurationNanos() / 1_000_000.0 / total() : 0.0;
    }

    @Override
    public double reasoningErrorRate() {
        return total() > 0 ? (double) failure() / total() * 100.0 : 0.0;
    }

    /**
     * Factory method to create a ReasoningEngineSnapshot with default reasoning metrics.
     * 
     * @param counts execution count data
     * @param timing execution timing data
     * @param activeAgents number of active agents
     * @param activeSessions number of active sessions
     * @param timestampMs snapshot timestamp
     * @return ReasoningEngineSnapshot with default reasoning metrics
     */
    public static ReasoningEngineSnapshot createDefault(Counts counts, Timing timing, long activeAgents,
            long activeSessions, long timestampMs) {
        return new ReasoningEngineSnapshot(counts, timing, activeAgents, activeSessions, 95.0, // reasoningAccuracy
                50.0, // reasoningComplexity
                85.0, // reasoningEfficiency
                counts.total() > 0 ? (double) counts.total() / (timing.totalDurationNanos() / 1_000_000_000.0) : 0.0, // reasoningThroughput
                90.0, // reasoningConfidence
                counts.total() * 3, // reasoningStepCount (estimate 3 steps per operation)
                5.0, // reasoningBacktrackingRate
                80.0, // reasoningOptimizationLevel
                timestampMs);
    }
}
