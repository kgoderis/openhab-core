package org.openhab.core.ai.common.metrics;

import java.time.Instant;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base implementation of PerformanceMetrics interface.
 * 
 * <p>
 * This class provides common functionality for all performance metrics implementations,
 * including basic properties and utility methods.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class BasePerformanceMetrics implements PerformanceMetrics {

    private final long totalOperations;
    private final long successfulOperations;
    private final long failedOperations;
    private final long totalProcessingTime;
    private final double averageResponseTime;
    private final @Nullable Instant lastOperationTime;

    /**
     * Protected constructor for subclasses.
     * 
     * @param totalOperations Total number of operations
     * @param successfulOperations Number of successful operations
     * @param failedOperations Number of failed operations
     * @param totalProcessingTime Total processing time in milliseconds
     * @param averageResponseTime Average response time in milliseconds
     * @param lastOperationTime Timestamp of last operation
     */
    protected BasePerformanceMetrics(long totalOperations, long successfulOperations, long failedOperations,
            long totalProcessingTime, double averageResponseTime, @Nullable Instant lastOperationTime) {
        this.totalOperations = totalOperations;
        this.successfulOperations = successfulOperations;
        this.failedOperations = failedOperations;
        this.totalProcessingTime = totalProcessingTime;
        this.averageResponseTime = averageResponseTime;
        this.lastOperationTime = lastOperationTime;
    }

    @Override
    public long getTotalOperations() {
        return totalOperations;
    }

    @Override
    public long getSuccessfulOperations() {
        return successfulOperations;
    }

    @Override
    public long getFailedOperations() {
        return failedOperations;
    }

    @Override
    public long getTotalProcessingTime() {
        return totalProcessingTime;
    }

    @Override
    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    @Override
    public @Nullable Instant getLastOperationTime() {
        return lastOperationTime;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BasePerformanceMetrics other = (BasePerformanceMetrics) obj;
        return totalOperations == other.totalOperations && successfulOperations == other.successfulOperations
                && failedOperations == other.failedOperations && totalProcessingTime == other.totalProcessingTime
                && Double.compare(averageResponseTime, other.averageResponseTime) == 0
                && Objects.equals(lastOperationTime, other.lastOperationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalOperations, successfulOperations, failedOperations, totalProcessingTime,
                averageResponseTime, lastOperationTime);
    }

    @Override
    public String toString() {
        return String.format(
                "BasePerformanceMetrics{totalOperations=%d, successfulOperations=%d, failedOperations=%d, totalProcessingTime=%d, averageResponseTime=%.2f, lastOperationTime=%s}",
                totalOperations, successfulOperations, failedOperations, totalProcessingTime, averageResponseTime,
                lastOperationTime);
    }
}
