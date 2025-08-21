package org.openhab.core.ai.tool.services;

import java.util.concurrent.atomic.LongAdder;

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
    private final LongAdder totalExecutions = new LongAdder();
    private final LongAdder successfulExecutions = new LongAdder();
    private final LongAdder totalExecutionTime = new LongAdder();

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
     * Calculate success rate.
     * 
     * @return success rate as a percentage
     */
    public double getSuccessRate() {
        return totalExecutions.sum() > 0 ? (double) successfulExecutions.sum() / totalExecutions.sum() : 0.0;
    }

    /**
     * Calculate average execution time.
     * 
     * @return average execution time in milliseconds
     */
    public double getAverageExecutionTime() {
        return totalExecutions.sum() > 0 ? (double) totalExecutionTime.sum() / totalExecutions.sum() : 0.0;
    }
}
