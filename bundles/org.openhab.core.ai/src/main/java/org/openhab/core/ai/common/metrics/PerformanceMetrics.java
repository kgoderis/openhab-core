package org.openhab.core.ai.common.metrics;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base interface for all performance metrics in the openHAB AI system.
 * 
 * <p>
 * This interface provides a unified contract for all performance metrics objects,
 * ensuring consistent behavior across different performance domains.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface PerformanceMetrics {

    /**
     * Get the total number of operations.
     * 
     * @return total operations
     */
    long getTotalOperations();

    /**
     * Get the number of successful operations.
     * 
     * @return successful operations
     */
    long getSuccessfulOperations();

    /**
     * Get the number of failed operations.
     * 
     * @return failed operations
     */
    long getFailedOperations();

    /**
     * Get the total processing time in milliseconds.
     * 
     * @return total processing time
     */
    long getTotalProcessingTime();

    /**
     * Get the average response time in milliseconds.
     * 
     * @return average response time
     */
    double getAverageResponseTime();

    /**
     * Get the timestamp of the last operation.
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
     * Get the operations per second rate.
     * 
     * @return operations per second
     */
    default double getOperationsPerSecond() {
        long totalTime = getTotalProcessingTime();
        if (totalTime == 0) {
            return 0.0;
        }
        return (double) getTotalOperations() / (totalTime / 1000.0);
    }
}
