package org.openhab.core.ai.common.security;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base interface for all security statistics in the openHAB AI system.
 * 
 * <p>
 * This interface provides a unified contract for all security statistics objects,
 * ensuring consistent behavior across different security domains.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface SecurityStatistics {

    /**
     * Get the total number of security operations.
     * 
     * @return total security operations
     */
    long getTotalOperations();

    /**
     * Get the number of successful security operations.
     * 
     * @return successful security operations
     */
    long getSuccessfulOperations();

    /**
     * Get the number of failed security operations.
     * 
     * @return failed security operations
     */
    long getFailedOperations();

    /**
     * Get the number of security violations.
     * 
     * @return security violations count
     */
    long getSecurityViolations();

    /**
     * Get the timestamp of the last security operation.
     * 
     * @return last operation time, or null if no operations have occurred
     */
    @Nullable
    Instant getLastOperationTime();

    /**
     * Get the success rate as a percentage.
     * 
     * @return success rate (0.0 to 100.0)
     */
    default double getSuccessRate() {
        long total = getTotalOperations();
        if (total == 0) {
            return 0.0;
        }
        return (double) getSuccessfulOperations() / total * 100.0;
    }

    /**
     * Get the failure rate as a percentage.
     * 
     * @return failure rate (0.0 to 100.0)
     */
    default double getFailureRate() {
        long total = getTotalOperations();
        if (total == 0) {
            return 0.0;
        }
        return (double) getFailedOperations() / total * 100.0;
    }

    /**
     * Get the violation rate as a percentage.
     * 
     * @return violation rate (0.0 to 100.0)
     */
    default double getViolationRate() {
        long total = getTotalOperations();
        if (total == 0) {
            return 0.0;
        }
        return (double) getSecurityViolations() / total * 100.0;
    }
}
