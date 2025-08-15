package org.openhab.core.ai.agent.execution.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface ExecutionMetric {
    ExecutionStrategyType getStrategyType();

    long getDuration();

    long getMemoryUsage();

    double getCpuUsage();

    double getSuccessRate();
}
