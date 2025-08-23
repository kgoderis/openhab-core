package org.openhab.core.ai.agent.planning;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents an individual action within an action plan.
 * 
 * <p>
 * This class encapsulates a single action with its parameters, priority,
 * dependencies, and execution metadata.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelAction {

    private final String actionId;
    private final String actionName;
    private final String description;
    private final Map<String, Object> parameters;
    private final AgentModelActionPriority priority;
    private final long estimatedDurationMs;
    private final List<String> dependencies;

    private AgentModelAction(Builder builder) {
        this.actionId = Objects.requireNonNull(builder.actionId, "actionId");
        this.actionName = Objects.requireNonNull(builder.actionName, "actionName");
        this.description = Objects.requireNonNull(builder.description, "description");
        this.parameters = Map.copyOf(builder.parameters);
        this.priority = Objects.requireNonNull(builder.priority, "priority");
        this.estimatedDurationMs = builder.estimatedDurationMs;
        this.dependencies = List.copyOf(builder.dependencies);
    }

    /**
     * Create a new builder for AgentModelAction.
     * 
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a builder from this instance for modification.
     * 
     * @return a new builder with current values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    // Getters
    public String getActionId() {
        return actionId;
    }

    public String getActionName() {
        return actionName;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public AgentModelActionPriority getPriority() {
        return priority;
    }

    public long getEstimatedDurationMs() {
        return estimatedDurationMs;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    /**
     * Builder for AgentModelAction.
     */
    public static final class Builder {
        private String actionId = "";
        private String actionName = "";
        private String description = "";
        private Map<String, Object> parameters = Map.of();
        private AgentModelActionPriority priority = AgentModelActionPriority.MEDIUM;
        private long estimatedDurationMs = 1000L;
        private List<String> dependencies = List.of();

        public Builder() {
        }

        public Builder(AgentModelAction source) {
            this.actionId = source.actionId;
            this.actionName = source.actionName;
            this.description = source.description;
            this.parameters = source.parameters;
            this.priority = source.priority;
            this.estimatedDurationMs = source.estimatedDurationMs;
            this.dependencies = source.dependencies;
        }

        public Builder withActionId(String actionId) {
            this.actionId = actionId;
            return this;
        }

        public Builder withActionName(String actionName) {
            this.actionName = actionName;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder withPriority(AgentModelActionPriority priority) {
            this.priority = priority;
            return this;
        }

        public Builder withEstimatedDurationMs(long estimatedDurationMs) {
            this.estimatedDurationMs = estimatedDurationMs;
            return this;
        }

        public Builder withDependencies(List<String> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public AgentModelAction build() {
            if (estimatedDurationMs < 0) {
                throw new IllegalArgumentException("estimatedDurationMs must be non-negative");
            }
            return new AgentModelAction(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AgentModelAction other = (AgentModelAction) obj;
        return estimatedDurationMs == other.estimatedDurationMs && Objects.equals(actionId, other.actionId)
                && Objects.equals(actionName, other.actionName) && Objects.equals(description, other.description)
                && Objects.equals(parameters, other.parameters) && priority == other.priority
                && Objects.equals(dependencies, other.dependencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionId, actionName, description, parameters, priority, estimatedDurationMs, dependencies);
    }

    @Override
    public String toString() {
        return String.format("AgentModelAction{actionId='%s', actionName='%s', priority=%s, duration=%dms}", actionId,
                actionName, priority, estimatedDurationMs);
    }
}
