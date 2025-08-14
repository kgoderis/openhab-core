package org.openhab.core.ai.agent.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.execution.ExecutionPriority;
import org.openhab.core.ai.agent.execution.ExecutionStrategyType;

/**
 * Execution Request
 * 
 * <p>
 * Represents a request to execute a skill or action with specific parameters and context.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ExecutionRequest {
    private final ExecutionStrategyType type;
    private final String targetName;
    private final Map<String, Object> parameters;
    private final ExecutionPriority priority;
    private final Map<String, Object> context;
    private final boolean requiresValidation;
    private final boolean requiresSafetyChecks;

    /* package */ ExecutionRequest(ExecutionRequestBuilder builder) {
        this.type = builder.type;
        this.targetName = builder.targetName;
        this.parameters = new HashMap<>(builder.parameters);
        this.priority = builder.priority;
        this.context = new HashMap<>(builder.context);
        this.requiresValidation = builder.requiresValidation;
        this.requiresSafetyChecks = builder.requiresSafetyChecks;
    }

    public ExecutionStrategyType getType() {
        return type;
    }

    public String getTargetName() {
        return targetName;
    }

    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters);
    }

    public ExecutionPriority getPriority() {
        return priority;
    }

    public Map<String, Object> getContext() {
        return new HashMap<>(context);
    }

    public boolean requiresValidation() {
        return requiresValidation;
    }

    public boolean requiresSafetyChecks() {
        return requiresSafetyChecks;
    }

    public static ExecutionRequestBuilder builder() {
        return new ExecutionRequestBuilder();
    }
}
