package org.openhab.core.ai.common.metrics;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Reasoning-specific performance metrics implementation.
 * 
 * <p>
 * This class provides performance metrics specific to reasoning operations,
 * including sessions, steps, actions, and session duration.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ReasoningPerformanceMetrics extends BasePerformanceMetrics {

    private final long totalSteps;
    private final long totalActions;
    private final double averageSessionDuration;

    /**
     * Constructor for ReasoningPerformanceMetrics.
     * 
     * @param totalSessions Total number of reasoning sessions
     * @param successfulSessions Number of successful sessions
     * @param failedSessions Number of failed sessions
     * @param totalSteps Total number of reasoning steps
     * @param totalActions Total number of actions executed
     * @param averageSessionDuration Average session duration in milliseconds
     * @param totalProcessingTime Total processing time in milliseconds
     * @param averageResponseTime Average response time in milliseconds
     * @param lastOperationTime Timestamp of last operation
     */
    public ReasoningPerformanceMetrics(long totalSessions, long successfulSessions, long failedSessions,
            long totalSteps, long totalActions, double averageSessionDuration, long totalProcessingTime,
            double averageResponseTime, @Nullable Instant lastOperationTime) {
        super(totalSessions, successfulSessions, failedSessions, totalProcessingTime, averageResponseTime,
                lastOperationTime);
        this.totalSteps = totalSteps;
        this.totalActions = totalActions;
        this.averageSessionDuration = averageSessionDuration;
    }

    /**
     * Get the total number of reasoning steps.
     * 
     * @return total steps count
     */
    public long getTotalSteps() {
        return totalSteps;
    }

    /**
     * Get the total number of actions executed.
     * 
     * @return total actions count
     */
    public long getTotalActions() {
        return totalActions;
    }

    /**
     * Get the average session duration in milliseconds.
     * 
     * @return average session duration
     */
    public double getAverageSessionDuration() {
        return averageSessionDuration;
    }

    // Backward-compatible aliases for existing code
    /**
     * Get total sessions (alias for getTotalOperations).
     * 
     * @return total sessions count
     */
    public long getTotalSessions() {
        return getTotalOperations();
    }

    /**
     * Get successful sessions (alias for getSuccessfulOperations).
     * 
     * @return successful sessions count
     */
    public long getSuccessfulSessions() {
        return getSuccessfulOperations();
    }

    /**
     * Get failed sessions (alias for getFailedOperations).
     * 
     * @return failed sessions count
     */
    public long getFailedSessions() {
        return getFailedOperations();
    }

    /**
     * Get total reasoning sessions (alias for getTotalOperations).
     * 
     * @return total reasoning sessions count
     */
    public long getTotalReasoningSessions() {
        return getTotalOperations();
    }
}
