package org.openhab.core.ai.agent.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.execution.api.ExecutionPriority;
import org.openhab.core.ai.agent.execution.api.ExecutionStrategyType;
import org.openhab.core.ai.common.builder.ExecutionRequestBuilder;

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

    public ExecutionRequest(ExecutionRequestBuilder builder) {
        this.type = ExecutionStrategyType.ACTION; // Default value since unified builder doesn't have type
        this.targetName = builder.getTaskDescription();
        this.parameters = builder.getParameters() != null ? new HashMap<>(builder.getParameters()) : new HashMap<>();
        this.priority = ExecutionPriority.MEDIUM; // Default value since unified builder doesn't have priority enum
        this.context = new HashMap<>(); // Default empty context since unified builder doesn't have context
        this.requiresValidation = builder.isRequiresValidation();
        this.requiresSafetyChecks = builder.isRequiresSafetyChecks();
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
        return new ExecutionRequestBuilder("request-" + System.currentTimeMillis());
    }
}
