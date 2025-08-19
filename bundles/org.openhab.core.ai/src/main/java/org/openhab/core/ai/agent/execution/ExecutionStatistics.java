package org.openhab.core.ai.agent.execution;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.statistics.BaseStatistics;
import org.openhab.core.ai.common.statistics.StatisticsType;

/**
 * Execution statistics for agent operations.
 * 
 * <p>
 * This class provides statistics about agent execution operations including
 * total executions, successful executions, and failed executions.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ExecutionStatistics extends BaseStatistics {

    /**
     * Create a new ExecutionStatistics instance.
     * 
     * @param totalExecutions the total number of executions
     * @param successfulExecutions the number of successful executions
     * @param failedExecutions the number of failed executions
     */
    public ExecutionStatistics(long totalExecutions, long successfulExecutions, long failedExecutions) {
        super("execution-stats", StatisticsType.EXECUTION);

        // Add execution-specific metrics
        addMetric("totalExecutions", totalExecutions);
        addMetric("successfulExecutions", successfulExecutions);
        addMetric("failedExecutions", failedExecutions);
        addMetric("totalCount", totalExecutions);
        addMetric("successCount", successfulExecutions);
        addMetric("failureCount", failedExecutions);
    }

    /**
     * Get the total number of executions.
     * 
     * @return total executions count
     */
    public long getTotalExecutions() {
        return (Long) getMetric("totalExecutions");
    }

    /**
     * Get the number of successful executions.
     * 
     * @return successful executions count
     */
    public long getSuccessfulExecutions() {
        return (Long) getMetric("successfulExecutions");
    }

    /**
     * Get the number of failed executions.
     * 
     * @return failed executions count
     */
    public long getFailedExecutions() {
        return (Long) getMetric("failedExecutions");
    }

    /**
     * Get the success rate as a percentage.
     * 
     * @return success rate (0.0 to 100.0)
     */
    public double getSuccessRate() {
        return super.getSuccessRate();
    }
}
