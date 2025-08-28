package org.openhab.core.ai.common.security;

import java.time.Instant;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;

/**
 * Immutable snapshot of security metrics data.
 * 
 * <p>
 * This class provides a point-in-time view of security performance metrics
 * including operation counts, success/failure rates, and security violations.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record SecuritySnapshot(Counts counts, long securityViolations, long timestampMs,
        @Nullable Instant lastOperationTime) implements MetricsSnapshot, CountsMetrics {

    /**
     * Create a new SecuritySnapshot instance.
     * 
     * @param counts the operation counts
     * @param securityViolations the number of security violations
     * @param timestampMs the timestamp in milliseconds
     * @param lastOperationTime the timestamp of the last operation
     * @return new SecuritySnapshot instance
     */
    public static SecuritySnapshot of(Counts counts, long securityViolations, long timestampMs,
            @Nullable Instant lastOperationTime) {
        return new SecuritySnapshot(counts, securityViolations, timestampMs, lastOperationTime);
    }

    /**
     * Create an empty SecuritySnapshot instance.
     * 
     * @return empty SecuritySnapshot instance
     */
    public static SecuritySnapshot empty() {
        return new SecuritySnapshot(new Counts(0, 0, 0), 0, System.currentTimeMillis(), null);
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

    /**
     * Get the number of security violations.
     * 
     * @return security violations count
     */
    public long securityViolations() {
        return securityViolations;
    }

    /**
     * Get the timestamp of the last operation.
     * 
     * @return last operation time, or null if no operations have occurred
     */
    public @Nullable Instant lastOperationTime() {
        return lastOperationTime;
    }

    /**
     * Get the failure rate as a percentage.
     * 
     * @return failure rate between 0.0 and 100.0
     */
    public double failureRate() {
        return total() > 0 ? (double) failure() / total() * 100.0 : 0.0;
    }

    /**
     * Get the violation rate as a percentage.
     * 
     * @return violation rate between 0.0 and 100.0
     */
    public double violationRate() {
        return total() > 0 ? (double) securityViolations / total() * 100.0 : 0.0;
    }

    /**
     * Get the security efficiency as a percentage.
     * 
     * @return efficiency between 0.0 and 100.0
     */
    public double securityEfficiency() {
        return total() > 0 ? (double) success() / total() * 100.0 : 0.0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SecuritySnapshot that = (SecuritySnapshot) obj;
        return securityViolations == that.securityViolations && timestampMs == that.timestampMs
                && Objects.equals(counts, that.counts) && Objects.equals(lastOperationTime, that.lastOperationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(counts, securityViolations, timestampMs, lastOperationTime);
    }

    @Override
    public String toString() {
        return String.format(
                "SecuritySnapshot{total=%d, success=%d, failure=%d, violations=%d, successRate=%.1f%%, violationRate=%.1f%%, efficiency=%.1f%%}",
                total(), success(), failure(), securityViolations, successRate(), violationRate(), securityEfficiency());
    }
}
