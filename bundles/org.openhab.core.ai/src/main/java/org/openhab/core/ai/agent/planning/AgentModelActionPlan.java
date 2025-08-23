package org.openhab.core.ai.agent.planning;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.context.ExecutionContext;

/**
 * Represents an action plan for intelligent agents.
 * 
 * <p>
 * This class encapsulates a complete action plan including the goal, actions,
 * context, parameters, and metadata for execution.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelActionPlan {

    private final String planId;
    private final String goal;
    private final List<AgentModelAction> actions;
    private final ExecutionContext context;
    private final AgentModelActionPlanParameters parameters;
    private final Instant createdAt;
    private final long estimatedTotalDurationMs;
    private final double confidence;

    private AgentModelActionPlan(Builder builder) {
        this.planId = Objects.requireNonNull(builder.planId, "planId");
        this.goal = Objects.requireNonNull(builder.goal, "goal");
        this.actions = List.copyOf(builder.actions);
        this.context = Objects.requireNonNull(builder.context, "context");
        this.parameters = Objects.requireNonNull(builder.parameters, "parameters");
        this.createdAt = Objects.requireNonNull(builder.createdAt, "createdAt");
        this.estimatedTotalDurationMs = builder.estimatedTotalDurationMs;
        this.confidence = builder.confidence;
    }

    /**
     * Create a new builder for AgentModelActionPlan.
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
    public String getPlanId() {
        return planId;
    }

    public String getGoal() {
        return goal;
    }

    public List<AgentModelAction> getActions() {
        return actions;
    }

    public ExecutionContext getContext() {
        return context;
    }

    public AgentModelActionPlanParameters getParameters() {
        return parameters;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getEstimatedTotalDurationMs() {
        return estimatedTotalDurationMs;
    }

    public double getConfidence() {
        return confidence;
    }

    /**
     * Builder for AgentModelActionPlan.
     */
    public static final class Builder {
        private String planId = "";
        private String goal = "";
        private List<AgentModelAction> actions = List.of();
        private ExecutionContext context;
        private AgentModelActionPlanParameters parameters;
        private Instant createdAt = Instant.now();
        private long estimatedTotalDurationMs = 0L;
        private double confidence = 0.0;

        public Builder() {
        }

        public Builder(AgentModelActionPlan source) {
            this.planId = source.planId;
            this.goal = source.goal;
            this.actions = source.actions;
            this.context = source.context;
            this.parameters = source.parameters;
            this.createdAt = source.createdAt;
            this.estimatedTotalDurationMs = source.estimatedTotalDurationMs;
            this.confidence = source.confidence;
        }

        public Builder withPlanId(String planId) {
            this.planId = planId;
            return this;
        }

        public Builder withGoal(String goal) {
            this.goal = goal;
            return this;
        }

        public Builder withActions(List<AgentModelAction> actions) {
            this.actions = actions;
            return this;
        }

        public Builder withContext(ExecutionContext context) {
            this.context = context;
            return this;
        }

        public Builder withParameters(AgentModelActionPlanParameters parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder withCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withEstimatedTotalDurationMs(long estimatedTotalDurationMs) {
            this.estimatedTotalDurationMs = estimatedTotalDurationMs;
            return this;
        }

        public Builder withConfidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public AgentModelActionPlan build() {
            if (confidence < 0.0 || confidence > 1.0) {
                throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
            }
            if (estimatedTotalDurationMs < 0) {
                throw new IllegalArgumentException("estimatedTotalDurationMs must be non-negative");
            }
            return new AgentModelActionPlan(this);
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
        AgentModelActionPlan other = (AgentModelActionPlan) obj;
        return Double.compare(confidence, other.confidence) == 0
                && estimatedTotalDurationMs == other.estimatedTotalDurationMs && Objects.equals(planId, other.planId)
                && Objects.equals(goal, other.goal) && Objects.equals(actions, other.actions)
                && Objects.equals(context, other.context) && Objects.equals(parameters, other.parameters)
                && Objects.equals(createdAt, other.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId, goal, actions, context, parameters, createdAt, estimatedTotalDurationMs,
                confidence);
    }

    @Override
    public String toString() {
        return String.format("AgentModelActionPlan{planId='%s', goal='%s', actions=%d, confidence=%.2f, duration=%dms}",
                planId, goal, actions.size(), confidence, estimatedTotalDurationMs);
    }
}
