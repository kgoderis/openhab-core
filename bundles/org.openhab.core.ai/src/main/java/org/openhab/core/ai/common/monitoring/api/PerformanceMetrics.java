package org.openhab.core.ai.common.monitoring.api;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Capability interface for performance monitoring metrics.
 * 
 * <p>
 * This interface provides functionality for performance monitoring, including
 * timing, throughput, efficiency, and performance indicators. It replaces
 * the Monitoring hierarchy's performance-related interfaces with a
 * capability-based approach.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface PerformanceMetrics {

    /**
     * Get the total number of operations performed.
     * 
     * @return total operation count
     */
    long totalOperations();

    /**
     * Get the number of successful operations.
     * 
     * @return successful operation count
     */
    long successfulOperations();

    /**
     * Get the number of failed operations.
     * 
     * @return failed operation count
     */
    long failedOperations();

    /**
     * Get the total processing time in nanoseconds.
     * 
     * @return total processing time in nanoseconds
     */
    long totalProcessingTimeNanos();

    /**
     * Get the average processing time per operation in milliseconds.
     * 
     * @return average processing time in milliseconds
     */
    default double averageProcessingTimeMs() {
        long total = totalOperations();
        if (total == 0) {
            return 0.0;
        }
        return (double) totalProcessingTimeNanos() / total / 1_000_000.0;
    }

    /**
     * Get the operations per second rate.
     * 
     * @return operations per second
     */
    default double operationsPerSecond() {
        long totalTimeNanos = totalProcessingTimeNanos();
        if (totalTimeNanos == 0) {
            return 0.0;
        }
        double totalTimeSeconds = totalTimeNanos / 1_000_000_000.0;
        return totalOperations() / totalTimeSeconds;
    }

    /**
     * Get the minimum processing time in milliseconds.
     * 
     * @return minimum processing time in milliseconds, or 0 if not tracked
     */
    default double minProcessingTimeMs() {
        return 0.0; // To be implemented by concrete classes
    }

    /**
     * Get the maximum processing time in milliseconds.
     * 
     * @return maximum processing time in milliseconds, or 0 if not tracked
     */
    default double maxProcessingTimeMs() {
        return 0.0; // To be implemented by concrete classes
    }

    /**
     * Get the median processing time in milliseconds.
     * 
     * @return median processing time in milliseconds, or 0 if not tracked
     */
    default double medianProcessingTimeMs() {
        return 0.0; // To be implemented by concrete classes
    }

    /**
     * Get the 95th percentile processing time in milliseconds.
     * 
     * @return 95th percentile processing time in milliseconds, or 0 if not tracked
     */
    default double p95ProcessingTimeMs() {
        return 0.0; // To be implemented by concrete classes
    }

    /**
     * Get the 99th percentile processing time in milliseconds.
     * 
     * @return 99th percentile processing time in milliseconds, or 0 if not tracked
     */
    default double p99ProcessingTimeMs() {
        return 0.0; // To be implemented by concrete classes
    }

    /**
     * Get the timestamp when performance monitoring started.
     * 
     * @return start timestamp, or null if not tracked
     */
    @Nullable
    Instant monitoringStartTime();

    /**
     * Get the timestamp when performance monitoring ended.
     * 
     * @return end timestamp, or null if monitoring is ongoing
     */
    @Nullable
    Instant monitoringEndTime();

    /**
     * Get the monitoring duration in milliseconds.
     * 
     * @return monitoring duration in milliseconds
     */
    default long monitoringDurationMs() {
        Instant start = monitoringStartTime();
        Instant end = monitoringEndTime();
        if (start == null || end == null) {
            return 0;
        }
        return end.toEpochMilli() - start.toEpochMilli();
    }

    /**
     * Check if performance monitoring is ongoing.
     * 
     * @return true if monitoring is ongoing, false if completed
     */
    default boolean isMonitoringOngoing() {
        return monitoringEndTime() == null;
    }

    /**
     * Get the success rate as a percentage.
     * 
     * @return success rate as a percentage (0.0 to 100.0)
     */
    default double successRate() {
        long total = totalOperations();
        if (total == 0) {
            return 0.0;
        }
        return (double) successfulOperations() / total * 100.0;
    }

    /**
     * Get the efficiency score (0.0 to 1.0).
     * 
     * @return efficiency score between 0.0 and 1.0
     */
    default double efficiencyScore() {
        long total = totalOperations();
        if (total == 0) {
            return 0.0;
        }
        double successRate = successRate() / 100.0;
        double avgTimeMs = averageProcessingTimeMs();
        // Normalize processing time (lower is better)
        double timeScore = avgTimeMs > 0 ? Math.min(1.0, 1000.0 / avgTimeMs) : 1.0;
        return (successRate + timeScore) / 2.0;
    }
}
