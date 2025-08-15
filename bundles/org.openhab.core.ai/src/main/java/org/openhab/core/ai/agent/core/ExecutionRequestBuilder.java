package org.openhab.core.ai.agent.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.execution.api.ExecutionPriority;
import org.openhab.core.ai.agent.execution.api.ExecutionStrategyType;

@NonNullByDefault
public class ExecutionRequestBuilder {
    /* package */ ExecutionStrategyType type = ExecutionStrategyType.SKILL;
    /* package */ String targetName = "";
    /* package */ Map<String, Object> parameters = new HashMap<>();
    /* package */ ExecutionPriority priority = ExecutionPriority.MEDIUM;
    /* package */ Map<String, Object> context = new HashMap<>();
    /* package */ boolean requiresValidation = true;
    /* package */ boolean requiresSafetyChecks = true;

    public ExecutionRequestBuilder type(ExecutionStrategyType type) {
        this.type = type;
        return this;
    }

    public ExecutionRequestBuilder targetName(String targetName) {
        this.targetName = targetName;
        return this;
    }

    public ExecutionRequestBuilder parameters(Map<String, Object> parameters) {
        this.parameters = new HashMap<>(parameters);
        return this;
    }

    public ExecutionRequestBuilder parameter(String key, Object value) {
        this.parameters.put(key, value);
        return this;
    }

    public ExecutionRequestBuilder priority(ExecutionPriority priority) {
        this.priority = priority;
        return this;
    }

    public ExecutionRequestBuilder context(Map<String, Object> context) {
        this.context = new HashMap<>(context);
        return this;
    }

    public ExecutionRequestBuilder context(String key, Object value) {
        this.context.put(key, value);
        return this;
    }

    public ExecutionRequestBuilder requiresValidation(boolean requiresValidation) {
        this.requiresValidation = requiresValidation;
        return this;
    }

    public ExecutionRequestBuilder requiresSafetyChecks(boolean requiresSafetyChecks) {
        this.requiresSafetyChecks = requiresSafetyChecks;
        return this;
    }

    public ExecutionRequest build() {
        if (targetName == null || targetName.trim().isEmpty()) {
            throw new IllegalArgumentException("Target name cannot be null or empty");
        }
        return new ExecutionRequest(this);
    }
}
