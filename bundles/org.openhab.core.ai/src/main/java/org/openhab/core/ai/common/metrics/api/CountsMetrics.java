package org.openhab.core.ai.common.metrics.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for count-based metrics.
 * 
 * <p>
 * This interface provides methods for accessing count-based metrics including
 * total operations, successful operations, and failed operations.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface CountsMetrics {

    /**
     * Get the total number of operations.
     * 
     * @return total operations
     */
    long total();

    /**
     * Get the number of successful operations.
     * 
     * @return successful operations
     */
    long success();

    /**
     * Get the number of failed operations.
     * 
     * @return failed operations
     */
    long failure();

    /**
     * Get the success rate as a percentage.
     * 
     * @return success rate (0.0 to 100.0)
     */
    default double successRate() {
        long total = total();
        if (total == 0) {
            return 0.0;
        }
        return (double) success() / total * 100.0;
    }
}
