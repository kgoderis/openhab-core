package org.openhab.core.ai.action;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Action execution performance metrics.
 *
 * author Karel Goderis - Initial Contribution
 * since 1.0.0
 */
@NonNullByDefault
public class ActionExecutionPerformanceMetrics {
    private final long totalExecutions;
    private final long successfulExecutions;
    private final long failedExecutions;
    private final long totalExecutionTime;
    private final long totalRetryAttempts;
    private final int cacheSize;

    public ActionExecutionPerformanceMetrics(long totalExecutions, long successfulExecutions, long failedExecutions,
            long totalExecutionTime, long totalRetryAttempts, int cacheSize) {
        this.totalExecutions = totalExecutions;
        this.successfulExecutions = successfulExecutions;
        this.failedExecutions = failedExecutions;
        this.totalExecutionTime = totalExecutionTime;
        this.totalRetryAttempts = totalRetryAttempts;
        this.cacheSize = cacheSize;
    }

    public long getTotalExecutions() {
        return totalExecutions;
    }

    public long getSuccessfulExecutions() {
        return successfulExecutions;
    }

    public long getFailedExecutions() {
        return failedExecutions;
    }

    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }

    public long getTotalRetryAttempts() {
        return totalRetryAttempts;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public double getSuccessRate() {
        return totalExecutions > 0 ? (double) successfulExecutions / totalExecutions : 0.0;
    }

    public double getAverageExecutionTime() {
        return totalExecutions > 0 ? (double) totalExecutionTime / totalExecutions : 0.0;
    }
}
