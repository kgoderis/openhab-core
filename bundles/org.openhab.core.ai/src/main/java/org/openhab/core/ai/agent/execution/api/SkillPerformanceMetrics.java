package org.openhab.core.ai.agent.execution.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface SkillPerformanceMetrics {
    long getTotalExecutions();

    long getSuccessfulExecutions();

    long getFailedExecutions();

    double getAverageExecutionTime();

    double getSuccessRate();
}
