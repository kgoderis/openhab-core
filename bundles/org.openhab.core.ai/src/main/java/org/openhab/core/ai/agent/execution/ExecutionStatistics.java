package org.openhab.core.ai.agent.execution;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractStatistics;

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
public final class ExecutionStatistics extends AbstractStatistics implements CountsMetrics {

    private final long totalExecutions;
    private final long successfulExecutions;
    private final long failedExecutions;

    /**
     * Create a new ExecutionStatistics instance.
     * 
     * @param totalExecutions the total number of executions
     * @param successfulExecutions the number of successful executions
     * @param failedExecutions the number of failed executions
     */
    public ExecutionStatistics(long totalExecutions, long successfulExecutions, long failedExecutions) {
        super("execution-stats", java.time.Instant.now(), "agent", "execution", "Agent execution statistics", null,
                totalExecutions, successfulExecutions, failedExecutions, null, null, null);
        this.totalExecutions = totalExecutions;
        this.successfulExecutions = successfulExecutions;
        this.failedExecutions = failedExecutions;
    }

    /**
     * Get the total number of executions.
     * 
     * @return total executions count
     */
    public long getTotalExecutions() {
        return totalExecutions;
    }

    /**
     * Get the number of successful executions.
     * 
     * @return successful executions count
     */
    public long getSuccessfulExecutions() {
        return successfulExecutions;
    }

    /**
     * Get the number of failed executions.
     * 
     * @return failed executions count
     */
    public long getFailedExecutions() {
        return failedExecutions;
    }

    // CountsMetrics interface implementation
    @Override
    public long total() {
        return getTotalCount();
    }

    @Override
    public long success() {
        return getSuccessCount();
    }

    @Override
    public long failure() {
        return getFailureCount();
    }
}
