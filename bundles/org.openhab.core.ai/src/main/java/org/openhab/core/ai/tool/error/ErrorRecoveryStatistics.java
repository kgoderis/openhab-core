package org.openhab.core.ai.tool.error;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Error recovery statistics.
 * 
 * <p>
 * This class provides comprehensive statistics about error recovery operations including
 * total errors, recoveries, fallbacks, and failures with breakdowns by type and strategy.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ErrorRecoveryStatistics {
    private final long totalErrors;
    private final long totalRecoveries;
    private final long totalFallbacks;
    private final long totalFailures;
    private final Map<String, Long> errorCountsByType;
    private final Map<String, Long> recoveryCountsByStrategy;

    /**
     * Constructor for ErrorRecoveryStatistics.
     * 
     * @param totalErrors total number of errors
     * @param totalRecoveries total number of successful recoveries
     * @param totalFallbacks total number of fallback operations
     * @param totalFailures total number of unrecoverable failures
     * @param errorCountsByType breakdown of errors by type
     * @param recoveryCountsByStrategy breakdown of recoveries by strategy
     */
    public ErrorRecoveryStatistics(long totalErrors, long totalRecoveries, long totalFallbacks, long totalFailures,
            Map<String, Long> errorCountsByType, Map<String, Long> recoveryCountsByStrategy) {
        this.totalErrors = totalErrors;
        this.totalRecoveries = totalRecoveries;
        this.totalFallbacks = totalFallbacks;
        this.totalFailures = totalFailures;
        this.errorCountsByType = errorCountsByType;
        this.recoveryCountsByStrategy = recoveryCountsByStrategy;
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
}
