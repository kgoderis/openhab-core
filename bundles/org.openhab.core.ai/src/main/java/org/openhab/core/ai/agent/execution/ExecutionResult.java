package org.openhab.core.ai.agent.execution;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface ExecutionResult {
    boolean isSuccess();
    Object getData();
    String getErrorMessage();
    long getExecutionTime();
    ExecutionMetric getMetrics();
}


