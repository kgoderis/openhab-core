package org.openhab.core.ai.common.metrics;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Model-specific performance metrics implementation.
 * 
 * <p>
 * This class provides performance metrics specific to model operations,
 * including parsing attempts, JSON/regex parsing, and action calls.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ModelPerformanceMetrics extends BasePerformanceMetrics {

    private final long successfulJsonParses;
    private final long successfulRegexParses;
    private final long totalActionCalls;

    /**
     * Constructor for ModelPerformanceMetrics.
     * 
     * @param totalParsingAttempts Total number of parsing attempts
     * @param successfulJsonParses Number of successful JSON parses
     * @param successfulRegexParses Number of successful regex parses
     * @param failedParses Number of failed parses
     * @param totalActionCalls Total number of action calls
     * @param totalProcessingTime Total processing time in milliseconds
     * @param averageResponseTime Average response time in milliseconds
     * @param lastOperationTime Timestamp of last operation
     */
    public ModelPerformanceMetrics(long totalParsingAttempts, long successfulJsonParses, long successfulRegexParses,
            long failedParses, long totalActionCalls, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime) {
        super(totalParsingAttempts, successfulJsonParses + successfulRegexParses, failedParses, totalProcessingTime,
                averageResponseTime, lastOperationTime);
        this.successfulJsonParses = successfulJsonParses;
        this.successfulRegexParses = successfulRegexParses;
        this.totalActionCalls = totalActionCalls;
    }

    /**
     * Get the number of successful JSON parses.
     * 
     * @return successful JSON parses count
     */
    public long getSuccessfulJsonParses() {
        return successfulJsonParses;
    }

    /**
     * Get the number of successful regex parses.
     * 
     * @return successful regex parses count
     */
    public long getSuccessfulRegexParses() {
        return successfulRegexParses;
    }

    /**
     * Get the total number of action calls.
     * 
     * @return total action calls count
     */
    public long getTotalActionCalls() {
        return totalActionCalls;
    }

    // Backward-compatible aliases for existing code
    /**
     * Get total parsing attempts (alias for getTotalOperations).
     * 
     * @return total parsing attempts
     */
    public long getTotalParsingAttempts() {
        return getTotalOperations();
    }

    /**
     * Get failed parses (alias for getFailedOperations).
     * 
     * @return failed parses count
     */
    public long getFailedParses() {
        return getFailedOperations();
    }
}
