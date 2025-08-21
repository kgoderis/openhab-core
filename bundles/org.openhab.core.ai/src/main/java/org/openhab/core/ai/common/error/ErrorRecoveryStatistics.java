package org.openhab.core.ai.common.error;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractStatistics;

/**
 * Unified error recovery statistics.
 * 
 * This class provides comprehensive statistics about error recovery operations including
 * total errors, recoveries, fallbacks, and failures with breakdowns by type and strategy.
 * This unified class combines functionality from both tool manager and tool error implementations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ErrorRecoveryStatistics extends AbstractStatistics implements CountsMetrics {
    private final long totalErrors;
    private final long totalRecoveries;
    private final long totalFallbacks;
    private final long totalFailures;
    private final long recoveryAttempts;
    private final Map<String, Long> errorCountsByType;
    private final Map<String, Long> recoveryCountsByStrategy;

    /**
     * Constructor for ErrorRecoveryStatistics.
     * 
     * @param totalErrors total number of errors
     * @param totalRecoveries total number of successful recoveries
     * @param totalFallbacks total number of fallback operations
     * @param totalFailures total number of unrecoverable failures
     * @param recoveryAttempts total number of recovery attempts made
     * @param errorCountsByType breakdown of errors by type
     * @param recoveryCountsByStrategy breakdown of recoveries by strategy
     */
    public ErrorRecoveryStatistics(long totalErrors, long totalRecoveries, long totalFallbacks, long totalFailures,
            long recoveryAttempts, Map<String, Long> errorCountsByType, Map<String, Long> recoveryCountsByStrategy) {
        super("error-recovery-statistics", java.time.Instant.now(), "error", "recovery", "Error recovery statistics",
                Map.of("errorCountsByType", errorCountsByType, "recoveryCountsByStrategy", recoveryCountsByStrategy),
                totalErrors + totalRecoveries + totalFallbacks + totalFailures, totalRecoveries, totalFailures, null,
                null, null);
        this.totalErrors = totalErrors;
        this.totalRecoveries = totalRecoveries;
        this.totalFallbacks = totalFallbacks;
        this.totalFailures = totalFailures;
        this.recoveryAttempts = recoveryAttempts;
        this.errorCountsByType = Objects.requireNonNull(errorCountsByType, "errorCountsByType");
        this.recoveryCountsByStrategy = Objects.requireNonNull(recoveryCountsByStrategy, "recoveryCountsByStrategy");
    }

    /**
     * Get total number of errors.
     * 
     * @return total errors
     */
    public long getTotalErrors() {
        return totalErrors;
    }

    /**
     * Get total number of successful recoveries.
     * 
     * @return total recoveries
     */
    public long getTotalRecoveries() {
        return totalRecoveries;
    }

    /**
     * Get total number of fallback operations.
     * 
     * @return total fallbacks
     */
    public long getTotalFallbacks() {
        return totalFallbacks;
    }

    /**
     * Get total number of unrecoverable failures.
     * 
     * @return total failures
     */
    public long getTotalFailures() {
        return totalFailures;
    }

    /**
     * Get total number of recovery attempts made.
     * 
     * @return total recovery attempts
     */
    public long getRecoveryAttempts() {
        return recoveryAttempts;
    }

    /**
     * Get breakdown of errors by type.
     * 
     * @return error counts by type
     */
    public Map<String, Long> getErrorCountsByType() {
        return errorCountsByType;
    }

    /**
     * Get breakdown of recoveries by strategy.
     * 
     * @return recovery counts by strategy
     */
    public Map<String, Long> getRecoveryCountsByStrategy() {
        return recoveryCountsByStrategy;
    }

    /**
     * Get recovery success rate as a percentage.
     * 
     * @return recovery success rate (0.0 to 1.0)
     */
    public double getRecoverySuccessRate() {
        return totalErrors > 0 ? (double) totalRecoveries / totalErrors : 0.0;
    }

    /**
     * Get fallback rate as a percentage.
     * 
     * @return fallback rate (0.0 to 1.0)
     */
    public double getFallbackRate() {
        return totalErrors > 0 ? (double) totalFallbacks / totalErrors : 0.0;
    }

    /**
     * Get failure rate as a percentage.
     * 
     * @return failure rate (0.0 to 1.0)
     */
    public double getFailureRate() {
        return totalErrors > 0 ? (double) totalFailures / totalErrors : 0.0;
    }

    /**
     * Get average recovery attempts per error.
     * 
     * @return average recovery attempts
     */
    public double getAverageRecoveryAttempts() {
        return totalErrors > 0 ? (double) recoveryAttempts / totalErrors : 0.0;
    }

    // CountsMetrics interface implementation
    @Override
    public long total() {
        return getTotalCount();
    }

    @Override
    public long success() {
        return getSuccessCount();
    }

    @Override
    public long failure() {
        return getFailureCount();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ErrorRecoveryStatistics other = (ErrorRecoveryStatistics) obj;
        return totalErrors == other.totalErrors && totalRecoveries == other.totalRecoveries
                && totalFallbacks == other.totalFallbacks && totalFailures == other.totalFailures
                && recoveryAttempts == other.recoveryAttempts
                && Objects.equals(errorCountsByType, other.errorCountsByType)
                && Objects.equals(recoveryCountsByStrategy, other.recoveryCountsByStrategy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalErrors, totalRecoveries, totalFallbacks, totalFailures, recoveryAttempts,
                errorCountsByType, recoveryCountsByStrategy);
    }

    @Override
    public String toString() {
        return "ErrorRecoveryStatistics{" + "totalErrors=" + totalErrors + ", totalRecoveries=" + totalRecoveries
                + ", totalFallbacks=" + totalFallbacks + ", totalFailures=" + totalFailures + ", recoveryAttempts="
                + recoveryAttempts + ", errorCountsByType=" + errorCountsByType + ", recoveryCountsByStrategy="
                + recoveryCountsByStrategy + '}';
    }
}
