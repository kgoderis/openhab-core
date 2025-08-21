package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for throughput and rate metrics.
 * 
 * <p>
 * This interface provides functionality for measuring and reporting throughput
 * and rate information including operations per second and peak throughput.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ThroughputMetrics {

    /**
     * Get the current operations per second rate.
     * 
     * @return operations per second
     */
    double operationsPerSecond();

    /**
     * Get the current items per second rate.
     * 
     * @return items per second
     */
    double itemsPerSecond();

    /**
     * Get the peak throughput achieved.
     * 
     * @return peak throughput value
     */
    long peakThroughput();

    /**
     * Get the average throughput over the measurement period.
     * 
     * @return average throughput
     */
    default double averageThroughput() {
        return operationsPerSecond();
    }

    /**
     * Check if the current throughput is at peak level.
     * 
     * @return true if current throughput equals peak throughput
     */
    default boolean isAtPeakThroughput() {
        return Math.abs(operationsPerSecond() - peakThroughput()) < 0.001;
    }
}
