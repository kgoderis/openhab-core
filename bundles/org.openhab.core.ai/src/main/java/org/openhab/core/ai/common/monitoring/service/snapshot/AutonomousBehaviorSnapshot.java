package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.AutonomousMetrics;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

/**
 * Snapshot class for autonomous behavior metrics.
 * 
 * <p>
 * This class provides autonomous behavior metrics including decision-making accuracy,
 * autonomous action success rates, learning progress, and adaptation metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record AutonomousBehaviorSnapshot(long total, long success, long failure, long totalDurationNanos,
        double decisionAccuracy, double actionSuccessRate, double learningProgress, double adaptationRate,
        double behaviorEfficiency, double decisionLatency, double actionThroughput, double autonomousErrorRate,
        double confidenceLevel, double explorationRate,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, AutonomousMetrics {

    /**
     * Create an autonomous behavior snapshot.
     * 
     * @param total total autonomous actions
     * @param success successful autonomous actions
     * @param failure failed autonomous actions
     * @param totalDurationNanos total duration in nanoseconds
     * @param decisionAccuracy decision accuracy percentage
     * @param actionSuccessRate action success rate percentage
     * @param learningProgress learning progress score
     * @param adaptationRate adaptation rate percentage
     * @param behaviorEfficiency behavior efficiency score
     * @param decisionLatency decision latency in milliseconds
     * @param actionThroughput action throughput in actions per minute
     * @param autonomousErrorRate autonomous error rate percentage
     * @param confidenceLevel confidence level score
     * @param explorationRate exploration rate percentage
     * @param timestampMs timestamp in milliseconds
     */
    public AutonomousBehaviorSnapshot {
        // Validation
        if (total < 0) {
            throw new IllegalArgumentException("total must be non-negative");
        }
        if (success < 0) {
            throw new IllegalArgumentException("success must be non-negative");
        }
        if (failure < 0) {
            throw new IllegalArgumentException("failure must be non-negative");
        }
        if (totalDurationNanos < 0) {
            throw new IllegalArgumentException("totalDurationNanos must be non-negative");
        }
        if (decisionAccuracy < 0.0 || decisionAccuracy > 100.0) {
            throw new IllegalArgumentException("decisionAccuracy must be between 0.0 and 100.0");
        }
        if (actionSuccessRate < 0.0 || actionSuccessRate > 100.0) {
            throw new IllegalArgumentException("actionSuccessRate must be between 0.0 and 100.0");
        }
        if (learningProgress < 0.0 || learningProgress > 100.0) {
            throw new IllegalArgumentException("learningProgress must be between 0.0 and 100.0");
        }
        if (adaptationRate < 0.0 || adaptationRate > 100.0) {
            throw new IllegalArgumentException("adaptationRate must be between 0.0 and 100.0");
        }
        if (behaviorEfficiency < 0.0 || behaviorEfficiency > 100.0) {
            throw new IllegalArgumentException("behaviorEfficiency must be between 0.0 and 100.0");
        }
        if (decisionLatency < 0.0) {
            throw new IllegalArgumentException("decisionLatency must be non-negative");
        }
        if (actionThroughput < 0.0) {
            throw new IllegalArgumentException("actionThroughput must be non-negative");
        }
        if (autonomousErrorRate < 0.0 || autonomousErrorRate > 100.0) {
            throw new IllegalArgumentException("autonomousErrorRate must be between 0.0 and 100.0");
        }
        if (confidenceLevel < 0.0 || confidenceLevel > 100.0) {
            throw new IllegalArgumentException("confidenceLevel must be between 0.0 and 100.0");
        }
        if (explorationRate < 0.0 || explorationRate > 100.0) {
            throw new IllegalArgumentException("explorationRate must be between 0.0 and 100.0");
        }
        if (timestampMs < 0) {
            throw new IllegalArgumentException("timestampMs must be non-negative");
        }
    }

    /**
     * Create an autonomous behavior snapshot from basic metrics.
     * 
     * @param total total autonomous actions
     * @param success successful autonomous actions
     * @param failure failed autonomous actions
     * @param totalDurationNanos total duration in nanoseconds
     * @param decisionAccuracy decision accuracy percentage
     * @param actionSuccessRate action success rate percentage
     * @param learningProgress learning progress score
     * @param adaptationRate adaptation rate percentage
     * @param behaviorEfficiency behavior efficiency score
     * @param decisionLatency decision latency in milliseconds
     * @param actionThroughput action throughput in actions per minute
     * @param autonomousErrorRate autonomous error rate percentage
     * @param confidenceLevel confidence level score
     * @param explorationRate exploration rate percentage
     * @return autonomous behavior snapshot
     */
    public static AutonomousBehaviorSnapshot of(long total, long success, long failure, long totalDurationNanos,
            double decisionAccuracy, double actionSuccessRate, double learningProgress, double adaptationRate,
            double behaviorEfficiency, double decisionLatency, double actionThroughput, double autonomousErrorRate,
            double confidenceLevel, double explorationRate) {
        return new AutonomousBehaviorSnapshot(total, success, failure, totalDurationNanos, decisionAccuracy,
                actionSuccessRate, learningProgress, adaptationRate, behaviorEfficiency, decisionLatency,
                actionThroughput, autonomousErrorRate, confidenceLevel, explorationRate, System.currentTimeMillis());
    }

    /**
     * Create an empty autonomous behavior snapshot.
     * 
     * @return empty autonomous behavior snapshot
     */
    public static AutonomousBehaviorSnapshot empty() {
        return new AutonomousBehaviorSnapshot(0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                System.currentTimeMillis());
    }

    // CountsMetrics implementation
    @Override
    public long total() {
        return total;
    }

    @Override
    public long success() {
        return success;
    }

    @Override
    public long failure() {
        return failure;
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return totalDurationNanos;
    }

    // AutonomousMetrics implementation
    @Override
    public double decisionAccuracy() {
        return decisionAccuracy;
    }

    @Override
    public double actionSuccessRate() {
        return actionSuccessRate;
    }

    @Override
    public double learningProgress() {
        return learningProgress;
    }

    @Override
    public double adaptationRate() {
        return adaptationRate;
    }

    @Override
    public double behaviorEfficiency() {
        return behaviorEfficiency;
    }

    @Override
    public double decisionLatency() {
        return decisionLatency;
    }

    @Override
    public double actionThroughput() {
        return actionThroughput;
    }

    @Override
    public double autonomousErrorRate() {
        return autonomousErrorRate;
    }

    @Override
    public double confidenceLevel() {
        return confidenceLevel;
    }

    @Override
    public double explorationRate() {
        return explorationRate;
    }
}
