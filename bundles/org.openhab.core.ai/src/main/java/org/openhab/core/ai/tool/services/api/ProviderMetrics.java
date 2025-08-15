package org.openhab.core.ai.tool.services.api;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ProviderMetrics {
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong successfulExecutions = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    private final AtomicLong totalFailures = new AtomicLong(0);

    public void recordExecution(boolean success, long executionTime) {
        totalExecutions.incrementAndGet();
        totalExecutionTime.addAndGet(executionTime);
        if (success) {
            successfulExecutions.incrementAndGet();
        } else {
            totalFailures.incrementAndGet();
        }
    }

    public long getTotalExecutions() {
        return totalExecutions.get();
    }

    public long getSuccessfulExecutions() {
        return successfulExecutions.get();
    }

    public long getTotalExecutionTime() {
        return totalExecutionTime.get();
    }

    public long getTotalFailures() {
        return totalFailures.get();
    }

    public double getSuccessRate() {
        return totalExecutions.get() > 0 ? (double) successfulExecutions.get() / totalExecutions.get() : 0.0;
    }

    public double getAverageResponseTime() {
        return totalExecutions.get() > 0 ? (double) totalExecutionTime.get() / totalExecutions.get() : 0.0;
    }

    public double getHealthScore() {
        return getSuccessRate() * (1.0 - Math.min(getAverageResponseTime() / 10000.0, 1.0));
    }
}
