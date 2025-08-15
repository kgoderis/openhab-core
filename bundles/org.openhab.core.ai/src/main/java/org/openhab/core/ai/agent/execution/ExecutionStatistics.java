package org.openhab.core.ai.agent.execution;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public final class ExecutionStatistics {
    private final long totalExecutions;
    private final long successfulExecutions;
    private final long failedExecutions;

    public ExecutionStatistics(long totalExecutions, long successfulExecutions, long failedExecutions) {
        this.totalExecutions = totalExecutions;
        this.successfulExecutions = successfulExecutions;
        this.failedExecutions = failedExecutions;
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

    public double getSuccessRate() {
        return totalExecutions > 0 ? (double) successfulExecutions / totalExecutions : 0.0;
    }
}
