package org.openhab.core.ai.common.monitoring.api;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface for performance monitoring data in the openHAB AI system.
 * 
 * <p>
 * This interface extends MonitoringData to provide specialized methods for
 * performance-related monitoring, focusing on operations, timing, rates,
 * and efficiency metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface Metrics extends Monitoring {

    /**
     * Get the total number of operations performed.
     * 
     * @return the total operations count
     */
    long getTotalOperations();

    /**
     * Get the number of successful operations.
     * 
     * @return the successful operations count
     */
    long getSuccessfulOperations();

    /**
     * Get the number of failed operations.
     * 
     * @return the failed operations count
     */
    long getFailedOperations();

    /**
     * Get the total processing time in milliseconds.
     * 
     * @return the total processing time
     */
    long getTotalProcessingTime();

    /**
     * Get the average response time in milliseconds.
     * 
     * @return the average response time
     */
    double getAverageResponseTime();

    /**
     * Get the timestamp of the last operation.
     * 
     * @return the last operation time, or null if no operations have occurred
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

    /**
     * Get the total processing time as a Duration.
     * 
     * @return the total processing time as Duration
     */
    default Duration getTotalProcessingDuration() {
        return Duration.ofMillis(getTotalProcessingTime());
    }

    /**
     * Get the average response time as a Duration.
     * 
     * @return the average response time as Duration
     */
    default Duration getAverageResponseDuration() {
        return Duration.ofMillis((long) getAverageResponseTime());
    }

    @Override
    default MonitoringType getType() {
        return MonitoringType.PERFORMANCE;
    }
}
