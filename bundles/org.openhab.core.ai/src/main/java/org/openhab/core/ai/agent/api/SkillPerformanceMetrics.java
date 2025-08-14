package org.openhab.core.ai.agent.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface SkillPerformanceMetrics {
    long getTotalExecutions();
    long getSuccessfulExecutions();
    long getFailedExecutions();
    double getAverageExecutionTime();
    double getSuccessRate();
}


