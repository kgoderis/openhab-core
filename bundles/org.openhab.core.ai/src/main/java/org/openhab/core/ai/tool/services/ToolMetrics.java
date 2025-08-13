package org.openhab.core.ai.tool.services;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Tool metrics for tracking individual tool performance.
 * 
 * <p>
 * This class provides metrics for individual tools including
 * execution counts, success rates, and execution times.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolMetrics {
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong successfulExecutions = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);

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
     * Calculate success rate.
     * 
     * @return success rate as a percentage
     */
    public double getSuccessRate() {
        return totalExecutions.get() > 0 ? (double) successfulExecutions.get() / totalExecutions.get() : 0.0;
    }

    /**
     * Calculate average execution time.
     * 
     * @return average execution time in milliseconds
     */
    public double getAverageExecutionTime() {
        return totalExecutions.get() > 0 ? (double) totalExecutionTime.get() / totalExecutions.get() : 0.0;
    }
}
