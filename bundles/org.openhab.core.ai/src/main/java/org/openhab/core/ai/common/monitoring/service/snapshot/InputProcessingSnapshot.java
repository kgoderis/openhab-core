package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.InputMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

/**
 * Snapshot class for input processing metrics.
 * 
 * <p>
 * This class provides input processing metrics including validation rates,
 * processing throughput, error rates, and input quality metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record InputProcessingSnapshot(long total, long success, long failure, long totalDurationNanos,
        double validationSuccessRate, double processingThroughput, double inputErrorRate, double inputQuality,
        double averageInputSize, double processingLatency, long queueDepth, double rejectionRate,
        double formatCompatibilityRate,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, InputMetrics {

    /**
     * Create an input processing snapshot.
     * 
     * @param total total inputs
     * @param success successful inputs
     * @param failure failed inputs
     * @param totalDurationNanos total duration in nanoseconds
     * @param validationSuccessRate validation success rate percentage
     * @param processingThroughput processing throughput in inputs per second
     * @param inputErrorRate input error rate percentage
     * @param inputQuality input quality score
     * @param averageInputSize average input size in bytes
     * @param processingLatency processing latency in milliseconds
     * @param queueDepth input queue depth
     * @param rejectionRate rejection rate percentage
     * @param formatCompatibilityRate format compatibility rate percentage
     * @param timestampMs timestamp in milliseconds
     */
    public InputProcessingSnapshot {
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
        if (validationSuccessRate < 0.0 || validationSuccessRate > 100.0) {
            throw new IllegalArgumentException("validationSuccessRate must be between 0.0 and 100.0");
        }
        if (processingThroughput < 0.0) {
            throw new IllegalArgumentException("processingThroughput must be non-negative");
        }
        if (inputErrorRate < 0.0 || inputErrorRate > 100.0) {
            throw new IllegalArgumentException("inputErrorRate must be between 0.0 and 100.0");
        }
        if (inputQuality < 0.0 || inputQuality > 100.0) {
            throw new IllegalArgumentException("inputQuality must be between 0.0 and 100.0");
        }
        if (averageInputSize < 0.0) {
            throw new IllegalArgumentException("averageInputSize must be non-negative");
        }
        if (processingLatency < 0.0) {
            throw new IllegalArgumentException("processingLatency must be non-negative");
        }
        if (queueDepth < 0) {
            throw new IllegalArgumentException("queueDepth must be non-negative");
        }
        if (rejectionRate < 0.0 || rejectionRate > 100.0) {
            throw new IllegalArgumentException("rejectionRate must be between 0.0 and 100.0");
        }
        if (formatCompatibilityRate < 0.0 || formatCompatibilityRate > 100.0) {
            throw new IllegalArgumentException("formatCompatibilityRate must be between 0.0 and 100.0");
        }
        if (timestampMs < 0) {
            throw new IllegalArgumentException("timestampMs must be non-negative");
        }
    }

    /**
     * Create an input processing snapshot from basic metrics.
     * 
     * @param total total inputs
     * @param success successful inputs
     * @param failure failed inputs
     * @param totalDurationNanos total duration in nanoseconds
     * @param validationSuccessRate validation success rate percentage
     * @param processingThroughput processing throughput in inputs per second
     * @param inputErrorRate input error rate percentage
     * @param inputQuality input quality score
     * @param averageInputSize average input size in bytes
     * @param processingLatency processing latency in milliseconds
     * @param queueDepth input queue depth
     * @param rejectionRate rejection rate percentage
     * @param formatCompatibilityRate format compatibility rate percentage
     * @return input processing snapshot
     */
    public static InputProcessingSnapshot of(long total, long success, long failure, long totalDurationNanos,
            double validationSuccessRate, double processingThroughput, double inputErrorRate, double inputQuality,
            double averageInputSize, double processingLatency, long queueDepth, double rejectionRate,
            double formatCompatibilityRate) {
        return new InputProcessingSnapshot(total, success, failure, totalDurationNanos, validationSuccessRate,
                processingThroughput, inputErrorRate, inputQuality, averageInputSize, processingLatency, queueDepth,
                rejectionRate, formatCompatibilityRate, System.currentTimeMillis());
    }

    /**
     * Create an empty input processing snapshot.
     * 
     * @return empty input processing snapshot
     */
    public static InputProcessingSnapshot empty() {
        return new InputProcessingSnapshot(0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0.0, 0.0,
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

    // InputMetrics implementation
    @Override
    public double validationSuccessRate() {
        return validationSuccessRate;
    }

    @Override
    public double processingThroughput() {
        return processingThroughput;
    }

    @Override
    public double inputErrorRate() {
        return inputErrorRate;
    }

    @Override
    public double inputQuality() {
        return inputQuality;
    }

    @Override
    public double averageInputSize() {
        return averageInputSize;
    }

    @Override
    public double processingLatency() {
        return processingLatency;
    }

    @Override
    public long queueDepth() {
        return queueDepth;
    }

    @Override
    public double rejectionRate() {
        return rejectionRate;
    }

    @Override
    public double formatCompatibilityRate() {
        return formatCompatibilityRate;
    }
}
