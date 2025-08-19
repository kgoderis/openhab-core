package org.openhab.core.ai.agent.lifecycle;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.api.AgentMetrics;

/**
 * Default implementation of {@link AgentMetrics} used by {@link AgentRegistry}.
 *
 * <p>
 * Tracks execution times and success/failure counts for an agent.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class DefaultAgentMetrics implements AgentMetrics {
    private final String agentId;
    private final List<Long> executionTimes = new CopyOnWriteArrayList<>();
    private int successCount = 0;
    private int failureCount = 0;

    public DefaultAgentMetrics(String agentId) {
        this.agentId = agentId;
    }

    public void recordExecution(long time, boolean success) {
        executionTimes.add(time);
        if (success) {
            successCount++;
        } else {
            failureCount++;
        }
    }

    @Override
    public long[] getExecutionTimes() {
        return executionTimes.stream().mapToLong(Long::longValue).toArray();
    }

    String getAgentId() {
        return agentId;
    }

    @Override
    public long getSuccessCount() {
        return successCount;
    }

    @Override
    public long getFailureCount() {
        return failureCount;
    }

    @Override
    public double getAverageExecutionTime() {
        return executionTimes.isEmpty() ? 0.0
                : executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }

    @Override
    public long getTotalExecutions() {
        return executionTimes.size();
    }
}
