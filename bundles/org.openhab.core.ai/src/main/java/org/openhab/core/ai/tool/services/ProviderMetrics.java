package org.openhab.core.ai.tool.services;

import java.util.concurrent.atomic.LongAdder;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Provider metrics for tracking individual provider performance.
 * 
 * <p>
 * This class provides metrics for individual model providers including
 * execution counts, success rates, response times, and health scores.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ProviderMetrics {
    private final LongAdder totalExecutions = new LongAdder();
    private final LongAdder successfulExecutions = new LongAdder();
    private final LongAdder totalExecutionTime = new LongAdder();
    private final LongAdder totalFailures = new LongAdder();

    /**
     * Record an execution with its result and timing.
     * 
     * @param success whether the execution was successful
     * @param executionTime execution time in milliseconds
     */
    public void recordExecution(boolean success, long executionTime) {
        totalExecutions.increment();
        totalExecutionTime.add(executionTime);

        if (success) {
            successfulExecutions.increment();
        } else {
            totalFailures.increment();
        }
    }

    /**
     * Get total number of executions.
     * 
     * @return total executions
     */
    public long getTotalExecutions() {
        return totalExecutions.sum();
    }

    /**
     * Get number of successful executions.
     * 
     * @return successful executions
     */
    public long getSuccessfulExecutions() {
        return successfulExecutions.sum();
    }

    /**
     * Get total execution time in milliseconds.
     * 
     * @return total execution time
     */
    public long getTotalExecutionTime() {
        return totalExecutionTime.sum();
    }

    /**
     * Get total number of failures.
     * 
     * @return total failures
     */
    public long getTotalFailures() {
        return totalFailures.sum();
    }

    /**
     * Calculate success rate.
     * 
     * @return success rate as a percentage
     */
    public double getSuccessRate() {
        return totalExecutions.sum() > 0 ? (double) successfulExecutions.sum() / totalExecutions.sum() : 0.0;
    }

    /**
     * Calculate average response time.
     * 
     * @return average response time in milliseconds
     */
    public double getAverageResponseTime() {
        return totalExecutions.sum() > 0 ? (double) totalExecutionTime.sum() / totalExecutions.sum() : 0.0;
    }

    /**
     * Calculate health score based on success rate and response time.
     * 
     * @return health score between 0.0 and 1.0
     */
    public double getHealthScore() {
        return getSuccessRate() * (1.0 - Math.min(getAverageResponseTime() / 10000.0, 1.0));
    }
}
