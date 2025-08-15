package org.openhab.core.ai.agent.execution.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface ExecutionStrategyRequest {
    ExecutionStrategyType getType();

    String getTargetName();

    Map<String, Object> getParameters();

    ExecutionPriority getPriority();

    Map<String, Object> getContext();

    boolean requiresValidation();

    boolean requiresSafetyChecks();
}
