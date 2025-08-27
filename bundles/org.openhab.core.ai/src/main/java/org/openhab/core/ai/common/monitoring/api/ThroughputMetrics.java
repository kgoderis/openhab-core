package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for throughput and performance metrics.
 * 
 * <p>
 * This interface provides functionality for measuring and reporting throughput
 * and performance characteristics of operations.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ThroughputMetrics {

    /**
     * Get operations per second.
     * 
     * @return operations per second
     */
    double operationsPerSecond();

    /**
     * Get overall throughput.
     * 
     * @return throughput value
     */
    double throughput();

    /**
     * Get the peak operations per second.
     * 
     * @return peak operations per second
     */
    double peakOperationsPerSecond();

    /**
     * Get the average operations per second.
     * 
     * @return average operations per second
     */
    double averageOperationsPerSecond();

    /**
     * Get the minimum operations per second.
     * 
     * @return minimum operations per second
     */
    double minimumOperationsPerSecond();

    /**
     * Get the throughput efficiency (0.0 to 1.0).
     * 
     * @return throughput efficiency between 0.0 and 1.0
     */
    double throughputEfficiency();

    /**
     * Get the time window for throughput calculation in seconds.
     * 
     * @return time window in seconds
     */
    long throughputWindowSeconds();

    /**
     * Get the number of active operations.
     * 
     * @return number of active operations
     */
    long activeOperations();

    /**
     * Get the maximum concurrent operations.
     * 
     * @return maximum concurrent operations
     */
    long maxConcurrentOperations();

    /**
     * Check if the system is operating at peak capacity.
     * 
     * @return true if at peak capacity, false otherwise
     */
    default boolean isAtPeakCapacity() {
        return operationsPerSecond() >= peakOperationsPerSecond() * 0.95;
    }

    /**
     * Check if the system is underutilized.
     * 
     * @return true if underutilized, false otherwise
     */
    default boolean isUnderutilized() {
        return operationsPerSecond() < averageOperationsPerSecond() * 0.5;
    }

    /**
     * Get the throughput utilization percentage.
     * 
     * @return utilization percentage (0.0-100.0)
     */
    default double throughputUtilizationPercentage() {
        return peakOperationsPerSecond() > 0 ? (operationsPerSecond() * 100.0) / peakOperationsPerSecond() : 0.0;
    }

    /**
     * Get the throughput trend (positive = increasing, negative = decreasing).
     * 
     * @return throughput trend value
     */
    default double throughputTrend() {
        return operationsPerSecond() - averageOperationsPerSecond();
    }

    /**
     * Check if throughput is increasing.
     * 
     * @return true if increasing, false otherwise
     */
    default boolean isThroughputIncreasing() {
        return throughputTrend() > 0;
    }

    /**
     * Check if throughput is decreasing.
     * 
     * @return true if decreasing, false otherwise
     */
    default boolean isThroughputDecreasing() {
        return throughputTrend() < 0;
    }

    /**
     * Get the throughput stability score (0.0 to 1.0).
     * 
     * @return stability score between 0.0 and 1.0
     */
    default double throughputStability() {
        if (peakOperationsPerSecond() == 0) {
            return 1.0;
        }
        double variance = Math.abs(operationsPerSecond() - averageOperationsPerSecond()) / peakOperationsPerSecond();
        return Math.max(0.0, 1.0 - variance);
    }
}
