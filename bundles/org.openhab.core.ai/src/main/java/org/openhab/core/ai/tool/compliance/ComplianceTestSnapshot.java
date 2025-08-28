package org.openhab.core.ai.tool.compliance;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.ValidationMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

/**
 * Immutable snapshot of compliance test metrics data.
 * 
 * <p>
 * This class provides a point-in-time view of compliance test performance
 * including basic counts, latency metrics, and validation-specific metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ComplianceTestSnapshot(long total, long success, long failure, long totalDurationNanos, String testId,
        String category, int failureCount, int warningCount, int detailCount,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, ValidationMetrics {

    /**
     * Factory method to create a snapshot from metrics data.
     * 
     * @param total total compliance test operations
     * @param success successful tests
     * @param failure failed tests
     * @param totalDurationNanos total duration in nanoseconds
     * @param testId test identifier
     * @param category test category
     * @param failureCount number of failures
     * @param warningCount number of warnings
     * @param detailCount number of details
     * @return new ComplianceTestSnapshot instance
     */
    public static ComplianceTestSnapshot of(long total, long success, long failure, long totalDurationNanos,
            String testId, String category, int failureCount, int warningCount, int detailCount) {
        return new ComplianceTestSnapshot(total, success, failure, totalDurationNanos, testId, category, failureCount,
                warningCount, detailCount, System.currentTimeMillis());
    }

    /**
     * Factory method to create an empty snapshot.
     * 
     * @return empty ComplianceTestSnapshot instance
     */
    public static ComplianceTestSnapshot empty() {
        return new ComplianceTestSnapshot(0, 0, 0, 0, "unknown", "unknown", 0, 0, 0, System.currentTimeMillis());
    }

    // ValidationMetrics implementation
    @Override
    public double validationSuccessRate() {
        return CountsMetrics.super.successRatePercentage();
    }

    @Override
    public double validationAccuracy() {
        // For compliance tests, accuracy is the same as success rate
        return CountsMetrics.super.successRatePercentage();
    }

    @Override
    public double validationThroughput() {
        // Calculate tests per second based on duration
        return LatencyMetrics.super.throughput(total);
    }

    @Override
    public double validationEfficiency() {
        // Efficiency based on success rate and test coverage
        double coverage = Math.min(100.0, detailCount * 10.0); // Simple heuristic
        return (validationSuccessRate() + coverage) / 2.0;
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
        // Coverage based on detail count
        return Math.min(100.0, detailCount * 10.0);
    }

    @Override
    public double validationConfidence() {
        // Confidence based on success rate and test volume
        double volumeConfidence = Math.min(100.0, total / 10.0); // More tests = higher confidence
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
                "ComplianceTestSnapshot{testId=%s, category=%s, total=%d, success=%d, failure=%d, avgMs=%.2f, efficiency=%.1f%%}",
                testId, category, total, success, failure, LatencyMetrics.super.averageMs(total),
                validationEfficiency());
    }
}

