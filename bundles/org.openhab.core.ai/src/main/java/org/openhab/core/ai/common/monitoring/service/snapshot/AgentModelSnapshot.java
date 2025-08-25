package org.openhab.core.ai.common.monitoring.service.snapshot;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.AgentMetrics;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.ModelMetrics;
import org.openhab.core.ai.common.monitoring.api.Timing;

/**
 * Immutable snapshot of agent model performance metrics.
 *
 * <p>
 * This record provides comprehensive performance metrics for agent model operations
 * including model calls, reasoning, and decision-making processes. It implements multiple
 * capability interfaces for clean, type-safe metrics access.
 * </p>
 *
 * @param counts basic count metrics (total, success, failure)
 * @param timing latency and duration metrics
 * @param timestampMs timestamp when snapshot was created
 * @param modelId the model identifier
 * @param operationType the operation type being measured
 * @param minDurationMs minimum duration in milliseconds
 * @param maxDurationMs maximum duration in milliseconds
 * @param operationsByType operations count by type
 * @param errorsByType errors count by type
 * @param firstOperation timestamp of first operation
 * @param lastOperation timestamp of last operation
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record AgentModelSnapshot(Counts counts, Timing timing, long timestampMs, String modelId, String operationType,
        double minDurationMs, double maxDurationMs, Map<String, Long> operationsByType, Map<String, Long> errorsByType,
        @Nullable Instant firstOperation, @Nullable Instant lastOperation)
        implements
            MetricsSnapshot,
            CountsMetrics,
            LatencyMetrics,
            ModelMetrics,
            AgentMetrics {

    /**
     * Create an AgentModelSnapshot with defensive copying of maps.
     */
    public AgentModelSnapshot {
        operationsByType = Map.copyOf(operationsByType);
        errorsByType = Map.copyOf(errorsByType);
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

    public double operationsPerSecond() {
        if (totalDurationNanos() == 0)
            return 0.0;
        double durationSeconds = totalDurationNanos() / 1_000_000_000.0;
        return total() / durationSeconds;
    }

    // ModelMetrics implementation
    @Override
    public double tokensPerSecond() {
        // For agent models, we approximate tokens based on operations
        if (total() == 0 || totalDurationNanos() == 0)
            return 0.0;
        double durationSeconds = totalDurationNanos() / 1_000_000_000.0;
        return (total() * 100.0) / durationSeconds; // Approximate 100 tokens per operation
    }

    @Override
    public double costPerRequest() {
        // For agent models, cost is approximate based on operation complexity
        return total() > 0 ? (total() * 0.001) / total() : 0.0; // $0.001 per operation
    }

    @Override
    public double averageTokensPerRequest() {
        return 100.0; // Approximate tokens per agent model operation
    }

    // AgentMetrics implementation
    @Override
    public double decisionAccuracy() {
        return successRate() / 100.0; // Convert percentage to decimal
    }

    @Override
    public double learningRate() {
        // Calculate learning rate based on performance improvement over time
        if (firstOperation == null || lastOperation == null || firstOperation.equals(lastOperation)) {
            return 0.0;
        }

        // Simple approximation: higher success rate indicates better learning
        return Math.min(1.0, successRate() / 100.0);
    }

    // Domain-specific methods
    public long getOperationsByType(String type) {
        return operationsByType.getOrDefault(type, 0L);
    }

    public long getErrorsByType(String type) {
        return errorsByType.getOrDefault(type, 0L);
    }

    public double getFailureRate() {
        return total() > 0 ? (double) failure() / total() : 0.0;
    }

    /**
     * Get the model performance efficiency score.
     * 
     * @return efficiency score between 0.0 and 1.0
     */
    public double getModelPerformanceEfficiency() {
        double successRateDecimal = successRate() / 100.0;
        double avgMs = averageMs();
        double responseTimeScore = avgMs < 1000 ? 1.0 : avgMs < 5000 ? 0.8 : avgMs < 10000 ? 0.6 : 0.4;
        double consistencyScore = (maxDurationMs - minDurationMs) < 1000 ? 1.0
                : (maxDurationMs - minDurationMs) < 5000 ? 0.8 : (maxDurationMs - minDurationMs) < 10000 ? 0.6 : 0.4;

        return (successRateDecimal * 0.5) + (responseTimeScore * 0.3) + (consistencyScore * 0.2);
    }

    /**
     * Create an empty AgentModelSnapshot for the given model and operation.
     */
    public static AgentModelSnapshot empty(String modelId, String operationType) {
        return new AgentModelSnapshot(new Counts(0, 0, 0), new Timing(0), System.currentTimeMillis(), modelId,
                operationType, 0.0, 0.0, Map.of(), Map.of(), null, null);
    }
}
