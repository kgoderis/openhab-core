package org.openhab.core.ai.tool.services;

import java.util.concurrent.atomic.AtomicLong;

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
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong successfulExecutions = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    private final AtomicLong totalFailures = new AtomicLong(0);

    /**
     * Record an execution with its result and timing.
     * 
     * @param success whether the execution was successful
     * @param executionTime execution time in milliseconds
     */
    public void recordExecution(boolean success, long executionTime) {
        totalExecutions.incrementAndGet();
        totalExecutionTime.addAndGet(executionTime);

        if (success) {
            successfulExecutions.incrementAndGet();
        } else {
            totalFailures.incrementAndGet();
        }
    }

    /**
     * Get total number of executions.
     * 
     * @return total executions
     */
    public long getTotalExecutions() {
        return totalExecutions.get();
    }

    /**
     * Get number of successful executions.
     * 
     * @return successful executions
     */
    public long getSuccessfulExecutions() {
        return successfulExecutions.get();
    }

    /**
     * Get total execution time in milliseconds.
     * 
     * @return total execution time
     */
    public long getTotalExecutionTime() {
        return totalExecutionTime.get();
    }

    /**
     * Get total number of failures.
     * 
     * @return total failures
     */
    public long getTotalFailures() {
        return totalFailures.get();
    }

    /**
     * Calculate success rate.
     * 
     * @return success rate as a percentage
     */
    public double getSuccessRate() {
        return totalExecutions.get() > 0 ? (double) successfulExecutions.get() / totalExecutions.get() : 0.0;
    }

    /**
     * Calculate average response time.
     * 
     * @return average response time in milliseconds
     */
    public double getAverageResponseTime() {
        return totalExecutions.get() > 0 ? (double) totalExecutionTime.get() / totalExecutions.get() : 0.0;
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
