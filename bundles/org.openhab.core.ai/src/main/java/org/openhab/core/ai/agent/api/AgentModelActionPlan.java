package org.openhab.core.ai.agent.api;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Model-based action plan representation and management
 * 
 * <p>
 * This class provides:
 * - Action plan representation with ordered execution steps
 * - Plan metadata and configuration
 * - Plan validation and safety information
 * - Plan execution state tracking
 * - Plan performance metrics and optimization data
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelActionPlan {

    // Plan identification
    private final String planId;
    private final String agentId;
    private final String goal;
    private final Instant createdAt;
    private final String version;

    // Plan structure
    private final List<AgentModelActionStep> steps;
    private final Map<String, Object> context;
    private final Map<String, Object> constraints;
    private final Map<String, Object> metadata;

    // Plan state
    private final AgentModelActionPlanState state;
    private final @Nullable Instant startedAt;
    private final @Nullable Instant completedAt;
    private final @Nullable Duration estimatedDuration;
    private final @Nullable Duration actualDuration;

    // Plan validation
    private final boolean isValid;
    private final @Nullable String validationMessage;
    private final double confidenceScore;
    private final @Nullable List<String> warnings;

    // Performance metrics
    private final @Nullable AgentModelActionPlanPerformanceMetrics performanceMetrics;

    private AgentModelActionPlan(Builder builder) {
        this.planId = builder.planId;
        this.agentId = builder.agentId;
        this.goal = builder.goal;
        this.createdAt = builder.createdAt;
        this.version = builder.version;
        this.steps = List.copyOf(builder.steps);
        this.context = Map.copyOf(builder.context);
        this.constraints = Map.copyOf(builder.constraints);
        this.metadata = Map.copyOf(builder.metadata);
        this.state = builder.state;
        this.startedAt = builder.startedAt;
        this.completedAt = builder.completedAt;
        this.estimatedDuration = builder.estimatedDuration;
        this.actualDuration = builder.actualDuration;
        this.isValid = builder.isValid;
        this.validationMessage = builder.validationMessage;
        this.confidenceScore = builder.confidenceScore;
        this.warnings = builder.warnings != null ? List.copyOf(builder.warnings) : null;
        this.performanceMetrics = builder.performanceMetrics;
    }

    /**
     * Create a new action plan builder
     * 
     * @param agentId the agent ID
     * @param goal the goal to achieve
     * @return the builder
     */
    public static Builder builder(String agentId, String goal) {
        return new Builder(agentId, goal);
    }

    /**
     * Create a copy of this plan with updated state
     * 
     * @param newState the new state
     * @return the updated plan
     */
    public AgentModelActionPlan withState(AgentModelActionPlanState newState) {
        return new Builder(this).state(newState).build();
    }

    /**
     * Create a copy of this plan with execution started
     * 
     * @return the updated plan
     */
    public AgentModelActionPlan withExecutionStarted() {
        return new Builder(this).state(AgentModelActionPlanState.EXECUTING).startedAt(Instant.now()).build();
    }

    /**
     * Create a copy of this plan with execution completed
     * 
     * @param actualDuration the actual execution duration
     * @return the updated plan
     */
    public AgentModelActionPlan withExecutionCompleted(Duration actualDuration) {
        return new Builder(this).state(AgentModelActionPlanState.COMPLETED).completedAt(Instant.now())
                .actualDuration(actualDuration).build();
    }

    /**
     * Create a copy of this plan with execution failed
     * 
     * @param errorMessage the error message
     * @return the updated plan
     */
    public AgentModelActionPlan withExecutionFailed(String errorMessage) {
        return new Builder(this).state(AgentModelActionPlanState.FAILED).completedAt(Instant.now())
                .metadata(Map.of("errorMessage", errorMessage)).build();
    }

    // Getters
    public String getPlanId() {
        return planId;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getGoal() {
        return goal;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getVersion() {
        return version;
    }

    public List<AgentModelActionStep> getSteps() {
        return steps;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public Map<String, Object> getConstraints() {
        return constraints;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public AgentModelActionPlanState getState() {
        return state;
    }

    public @Nullable Instant getStartedAt() {
        return startedAt;
    }

    public @Nullable Instant getCompletedAt() {
        return completedAt;
    }

    public @Nullable Duration getEstimatedDuration() {
        return estimatedDuration;
    }

    public @Nullable Duration getActualDuration() {
        return actualDuration;
    }

    public boolean isValid() {
        return isValid;
    }

    public @Nullable String getValidationMessage() {
        return validationMessage;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public @Nullable List<String> getWarnings() {
        return warnings;
    }

    public @Nullable AgentModelActionPlanPerformanceMetrics getPerformanceMetrics() {
        return performanceMetrics;
    }

    /**
     * Get the number of steps in this plan
     * 
     * @return the number of steps
     */
    public int getStepCount() {
        return steps.size();
    }

    /**
     * Get a specific step by index
     * 
     * @param index the step index
     * @return the step
     */
    public @Nullable AgentModelActionStep getStep(int index) {
        if (index >= 0 && index < steps.size()) {
            return steps.get(index);
        }
        return null;
    }

    /**
     * Check if the plan is ready for execution
     * 
     * @return true if ready for execution
     */
    public boolean isReadyForExecution() {
        return state == AgentModelActionPlanState.CREATED && isValid;
    }

    /**
     * Check if the plan is currently executing
     * 
     * @return true if currently executing
     */
    public boolean isExecuting() {
        return state == AgentModelActionPlanState.EXECUTING;
    }

    /**
     * Check if the plan has completed
     * 
     * @return true if completed
     */
    public boolean isCompleted() {
        return state == AgentModelActionPlanState.COMPLETED;
    }

    /**
     * Check if the plan has failed
     * 
     * @return true if failed
     */
    public boolean isFailed() {
        return state == AgentModelActionPlanState.FAILED;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        AgentModelActionPlan that = (AgentModelActionPlan) obj;
        return Objects.equals(planId, that.planId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId);
    }

    @Override
    public String toString() {
        return String.format("AgentModelActionPlan{planId='%s', agentId='%s', goal='%s', state=%s, steps=%d}", planId,
                agentId, goal, state, steps.size());
    }

    /**
     * Builder for AgentModelActionPlan
     */
    public static final class Builder {
        private String planId;
        private String agentId;
        private String goal;
        private Instant createdAt;
        private String version;
        private List<AgentModelActionStep> steps;
        private Map<String, Object> context;
        private Map<String, Object> constraints;
        private Map<String, Object> metadata;
        private AgentModelActionPlanState state;
        private @Nullable Instant startedAt;
        private @Nullable Instant completedAt;
        private @Nullable Duration estimatedDuration;
        private @Nullable Duration actualDuration;
        private boolean isValid;
        private @Nullable String validationMessage;
        private double confidenceScore;
        private @Nullable List<String> warnings;
        private @Nullable AgentModelActionPlanPerformanceMetrics performanceMetrics;

        public Builder(String agentId, String goal) {
            this.planId = "plan_" + UUID.randomUUID().toString().replace("-", "");
            this.agentId = Objects.requireNonNull(agentId, "agentId");
            this.goal = Objects.requireNonNull(goal, "goal");
            this.createdAt = Instant.now();
            this.version = "1.0.0";
            this.steps = new ArrayList<>();
            this.context = new HashMap<>();
            this.constraints = new HashMap<>();
            this.metadata = new HashMap<>();
            this.state = AgentModelActionPlanState.CREATED;
            this.isValid = true;
            this.confidenceScore = 1.0;
        }

        public Builder(AgentModelActionPlan source) {
            this.planId = source.planId;
            this.agentId = source.agentId;
            this.goal = source.goal;
            this.createdAt = source.createdAt;
            this.version = source.version;
            this.steps = new ArrayList<>(source.steps);
            this.context = new HashMap<>(source.context);
            this.constraints = new HashMap<>(source.constraints);
            this.metadata = new HashMap<>(source.metadata);
            this.state = source.state;
            this.startedAt = source.startedAt;
            this.completedAt = source.completedAt;
            this.estimatedDuration = source.estimatedDuration;
            this.actualDuration = source.actualDuration;
            this.isValid = source.isValid;
            this.validationMessage = source.validationMessage;
            this.confidenceScore = source.confidenceScore;
            this.warnings = source.warnings != null ? new ArrayList<>(source.warnings) : null;
            this.performanceMetrics = source.performanceMetrics;
        }

        public Builder planId(String planId) {
            this.planId = Objects.requireNonNull(planId, "planId");
            return this;
        }

        public Builder agentId(String agentId) {
            this.agentId = Objects.requireNonNull(agentId, "agentId");
            return this;
        }

        public Builder goal(String goal) {
            this.goal = Objects.requireNonNull(goal, "goal");
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
            return this;
        }

        public Builder version(String version) {
            this.version = Objects.requireNonNull(version, "version");
            return this;
        }

        public Builder steps(List<AgentModelActionStep> steps) {
            this.steps = new ArrayList<>(Objects.requireNonNull(steps, "steps"));
            return this;
        }

        public Builder addStep(AgentModelActionStep step) {
            if (this.steps == null) {
                this.steps = new ArrayList<>();
            }
            this.steps.add(Objects.requireNonNull(step, "step"));
            return this;
        }

        public Builder context(Map<String, Object> context) {
            this.context = new HashMap<>(Objects.requireNonNull(context, "context"));
            return this;
        }

        public Builder addContext(String key, Object value) {
            if (this.context == null) {
                this.context = new HashMap<>();
            }
            this.context.put(Objects.requireNonNull(key, "key"), value);
            return this;
        }

        public Builder constraints(Map<String, Object> constraints) {
            this.constraints = new HashMap<>(Objects.requireNonNull(constraints, "constraints"));
            return this;
        }

        public Builder addConstraint(String key, Object value) {
            if (this.constraints == null) {
                this.constraints = new HashMap<>();
            }
            this.constraints.put(Objects.requireNonNull(key, "key"), value);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = new HashMap<>(Objects.requireNonNull(metadata, "metadata"));
            return this;
        }

        public Builder addMetadata(String key, Object value) {
            if (this.metadata == null) {
                this.metadata = new HashMap<>();
            }
            this.metadata.put(Objects.requireNonNull(key, "key"), value);
            return this;
        }

        public Builder state(AgentModelActionPlanState state) {
            this.state = Objects.requireNonNull(state, "state");
            return this;
        }

        public Builder startedAt(@Nullable Instant startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder completedAt(@Nullable Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Builder estimatedDuration(@Nullable Duration estimatedDuration) {
            this.estimatedDuration = estimatedDuration;
            return this;
        }

        public Builder actualDuration(@Nullable Duration actualDuration) {
            this.actualDuration = actualDuration;
            return this;
        }

        public Builder isValid(boolean isValid) {
            this.isValid = isValid;
            return this;
        }

        public Builder validationMessage(@Nullable String validationMessage) {
            this.validationMessage = validationMessage;
            return this;
        }

        public Builder confidenceScore(double confidenceScore) {
            this.confidenceScore = confidenceScore;
            return this;
        }

        public Builder warnings(@Nullable List<String> warnings) {
            this.warnings = warnings != null ? new ArrayList<>(warnings) : null;
            return this;
        }

        public Builder addWarning(String warning) {
            if (this.warnings == null) {
                this.warnings = new ArrayList<>();
            }
            this.warnings.add(Objects.requireNonNull(warning, "warning"));
            return this;
        }

        public Builder performanceMetrics(@Nullable AgentModelActionPlanPerformanceMetrics performanceMetrics) {
            this.performanceMetrics = performanceMetrics;
            return this;
        }

        public AgentModelActionPlan build() {
            return new AgentModelActionPlan(this);
        }
    }
}
