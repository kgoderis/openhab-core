package org.openhab.core.ai.tool.filter.validators;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.ValidationMetrics;

/**
 * Immutable snapshot of filter validator metrics data.
 * 
 * <p>
 * This class provides a point-in-time view of filter validation performance
 * including basic counts, latency metrics, and validation-specific metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record FilterValidatorSnapshot(long total, long success, long failure, long totalDurationNanos, long cacheHits,
        long cacheSize, long customFilterTypes,
        long timestampMs) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, ValidationMetrics {

    /**
     * Factory method to create a snapshot from metrics data.
     * 
     * @param total total validation operations
     * @param success successful validations
     * @param failure failed validations
     * @param totalDurationNanos total duration in nanoseconds
     * @param cacheHits number of cache hits
     * @param cacheSize current cache size
     * @param customFilterTypes number of custom filter types
     * @return new FilterValidatorSnapshot instance
     */
    public static FilterValidatorSnapshot of(long total, long success, long failure, long totalDurationNanos,
            long cacheHits, long cacheSize, long customFilterTypes) {
        return new FilterValidatorSnapshot(total, success, failure, totalDurationNanos, cacheHits, cacheSize,
                customFilterTypes, System.currentTimeMillis());
    }

    /**
     * Factory method to create an empty snapshot.
     * 
     * @return empty FilterValidatorSnapshot instance
     */
    public static FilterValidatorSnapshot empty() {
        return new FilterValidatorSnapshot(0, 0, 0, 0, 0, 0, 0, System.currentTimeMillis());
    }

    /**
     * Get the timestamp as an Instant.
     * 
     * @return timestamp as Instant
     */
    public Instant timestamp() {
        return Instant.ofEpochMilli(timestampMs);
    }

    /**
     * Calculate cache hit rate as a percentage.
     * 
     * @return cache hit rate percentage
     */
    public double cacheHitRatePercentage() {
        return total > 0 ? (double) cacheHits / total * 100.0 : 0.0;
    }

    /**
     * Calculate cache efficiency score.
     * 
     * @return cache efficiency score
     */
    public double cacheEfficiency() {
        return cacheHitRatePercentage();
    }

    // ValidationMetrics implementation
    @Override
    public double validationSuccessRate() {
        return CountsMetrics.super.successRatePercentage();
    }

    @Override
    public double validationAccuracy() {
        // For filter validation, accuracy is the same as success rate
        return CountsMetrics.super.successRatePercentage();
    }

    @Override
    public double validationThroughput() {
        // Calculate validations per second based on duration
        return LatencyMetrics.super.throughput(total);
    }

    @Override
    public double validationEfficiency() {
        // Combine success rate and cache efficiency
        return (validationSuccessRate() + cacheEfficiency()) / 2.0;
    }

    @Override
    public double validationErrorRate() {
        return CountsMetrics.super.failureRatePercentage();
    }

    @Override
    public double validationLatency() {
        return LatencyMetrics.super.averageMs(total);
    }

    @Override
    public double validationCoverage() {
        // Coverage based on custom filter types available
        return Math.min(100.0, customFilterTypes * 10.0); // Simple heuristic
    }

    @Override
    public double validationConfidence() {
        // Confidence based on success rate and volume
        double volumeConfidence = Math.min(100.0, total / 10.0); // More operations = higher confidence
        return (validationSuccessRate() + volumeConfidence) / 2.0;
    }

    @Override
    public double validationRejectionRate() {
        return CountsMetrics.super.failureRatePercentage();
    }

    @Override
    public double validationComplianceRate() {
        return CountsMetrics.super.successRatePercentage();
    }

    /**
     * Create a summary string with key metrics.
     * 
     * @return summary string
     */
    public String toSummary() {
        return String.format(
                "FilterValidatorSnapshot{total=%d, success=%d, failure=%d, avgMs=%.2f, cacheHitRate=%.1f%%, efficiency=%.1f%%}",
                total, success, failure, LatencyMetrics.super.averageMs(total), cacheHitRatePercentage(),
                validationEfficiency());
    }
}
