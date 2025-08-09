package org.openhab.core.ai.agent.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.execution.ExecutionStrategy;

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
    private final ExecutionStrategy.ExecutionStrategyType type;
    private final String targetName;
    private final Map<String, Object> parameters;
    private final ExecutionStrategy.ExecutionPriority priority;
    private final Map<String, Object> context;
    private final boolean requiresValidation;
    private final boolean requiresSafetyChecks;

    private ExecutionRequest(Builder builder) {
        this.type = builder.type;
        this.targetName = builder.targetName;
        this.parameters = new HashMap<>(builder.parameters);
        this.priority = builder.priority;
        this.context = new HashMap<>(builder.context);
        this.requiresValidation = builder.requiresValidation;
        this.requiresSafetyChecks = builder.requiresSafetyChecks;
    }

    public ExecutionStrategy.ExecutionStrategyType getType() {
        return type;
    }

    public String getTargetName() {
        return targetName;
    }

    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters);
    }

    public ExecutionStrategy.ExecutionPriority getPriority() {
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

    /**
     * Builder for ExecutionRequest
     */
    public static class Builder {
        private ExecutionStrategy.ExecutionStrategyType type = ExecutionStrategy.ExecutionStrategyType.SKILL;
        private String targetName = "";
        private Map<String, Object> parameters = new HashMap<>();
        private ExecutionStrategy.ExecutionPriority priority = ExecutionStrategy.ExecutionPriority.MEDIUM;
        private Map<String, Object> context = new HashMap<>();
        private boolean requiresValidation = true;
        private boolean requiresSafetyChecks = true;

        public Builder type(ExecutionStrategy.ExecutionStrategyType type) {
            this.type = type;
            return this;
        }

        public Builder targetName(String targetName) {
            this.targetName = targetName;
            return this;
        }

        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = new HashMap<>(parameters);
            return this;
        }

        public Builder parameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        public Builder priority(ExecutionStrategy.ExecutionPriority priority) {
            this.priority = priority;
            return this;
        }

        public Builder context(Map<String, Object> context) {
            this.context = new HashMap<>(context);
            return this;
        }

        public Builder context(String key, Object value) {
            this.context.put(key, value);
            return this;
        }

        public Builder requiresValidation(boolean requiresValidation) {
            this.requiresValidation = requiresValidation;
            return this;
        }

        public Builder requiresSafetyChecks(boolean requiresSafetyChecks) {
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

    /**
     * Create a new builder
     * 
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }
}
