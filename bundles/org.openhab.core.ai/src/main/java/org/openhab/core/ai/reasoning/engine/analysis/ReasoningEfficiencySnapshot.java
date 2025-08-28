package org.openhab.core.ai.reasoning.engine.analysis;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.base.Counts;
import org.openhab.core.ai.common.monitoring.base.Timing;

/**
 * Snapshot of reasoning efficiency metrics at a point in time.
 * 
 * <p>
 * This class provides an immutable view of reasoning efficiency metrics including
 * analysis counts, success/failure rates, and timing information.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ReasoningEfficiencySnapshot(Counts counts, Timing timing, long timestampMs, long totalReasoningSteps, long totalAnalysisOperations, double efficiencyScore)
        implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

    /**
     * Create a reasoning efficiency snapshot from individual metrics.
     * 
     * @param total total number of operations
     * @param success number of successful operations
     * @param failure number of failed operations
     * @param totalDurationNanos total duration in nanoseconds
     * @param totalReasoningSteps total number of reasoning steps
     * @param totalAnalysisOperations total number of analysis operations
     * @param efficiencyScore efficiency score (0.0 to 1.0)
     * @return the reasoning efficiency snapshot
     */
    public static ReasoningEfficiencySnapshot of(long total, long success, long failure, long totalDurationNanos, long totalReasoningSteps, long totalAnalysisOperations, double efficiencyScore) {
        return new ReasoningEfficiencySnapshot(
            new Counts(total, success, failure),
            new Timing(totalDurationNanos),
            System.currentTimeMillis(),
            totalReasoningSteps,
            totalAnalysisOperations,
            efficiencyScore
        );
    }

    /**
     * Create an empty reasoning efficiency snapshot.
     * 
     * @return an empty reasoning efficiency snapshot
     */
    public static ReasoningEfficiencySnapshot empty() {
        return new ReasoningEfficiencySnapshot(
            new Counts(0, 0, 0),
            new Timing(0),
            System.currentTimeMillis(),
            0,
            0,
            0.0
        );
    }

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
    public long totalDurationNanos() {
        return timing.totalDurationNanos();
    }

    /**
     * Get the average processing time in milliseconds.
     * 
     * @return average processing time in milliseconds
     */
    public double averageMs() {
        return LatencyMetrics.super.averageMs(total());
    }

    /**
     * Get the total number of reasoning steps.
     * 
     * @return total number of reasoning steps
     */
    public long totalReasoningSteps() {
        return totalReasoningSteps;
    }

    /**
     * Get the total number of analysis operations.
     * 
     * @return total number of analysis operations
     */
    public long totalAnalysisOperations() {
        return totalAnalysisOperations;
    }

    /**
     * Get the efficiency score (0.0 to 1.0).
     * 
     * @return efficiency score
     */
    public double efficiencyScore() {
        return efficiencyScore;
    }
}
