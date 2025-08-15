package org.openhab.core.ai.agent.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface AgentMetrics {
    long[] getExecutionTimes();

    long getSuccessCount();

    long getFailureCount();

    double getAverageExecutionTime();

    long getTotalExecutions();
}
