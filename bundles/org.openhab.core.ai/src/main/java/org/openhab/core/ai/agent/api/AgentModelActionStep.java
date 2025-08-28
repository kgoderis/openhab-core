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
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;

/**
 * Individual action step within a model-based action plan
 * 
 * <p>
 * This class provides:
 * - Action step representation with execution details
 * - Step dependencies and ordering
 * - Step execution state tracking
 * - Step validation and safety information
 * - Step performance metrics and optimization data
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelActionStep {

    // Step identification
    private final String stepId;
    private final String stepName;
    private final String actionId;
    private final int stepOrder;
    private final String description;

    // Step execution
    private final Map<String, Object> parameters;
    private final Map<String, Object> context;
    private final List<String> dependencies;
    private final Map<String, Object> metadata;

    // Step state
    private final AgentModelActionStepState state;
    private final @Nullable Instant startedAt;
    private final @Nullable Instant completedAt;
    private final @Nullable Duration estimatedDuration;
    private final @Nullable Duration actualDuration;

    // Step validation
    private final boolean isValid;
    private final @Nullable String validationMessage;
    private final double confidenceScore;
    private final @Nullable List<String> warnings;

    // Step results
    private final @Nullable ActionResult result;
    private final @Nullable String errorMessage;
    private final @Nullable Exception exception;

    // Performance metrics key for MetricsService lookup
    private final @Nullable MetricKey performanceMetricsKey;

    private AgentModelActionStep(Builder builder) {
        this.stepId = builder.stepId;
        this.stepName = builder.stepName;
        this.actionId = builder.actionId;
        this.stepOrder = builder.stepOrder;
        this.description = builder.description;
        this.parameters = Map.copyOf(builder.parameters);
        this.context = Map.copyOf(builder.context);
        this.dependencies = List.copyOf(builder.dependencies);
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
        this.result = builder.result;
        this.errorMessage = builder.errorMessage;
        this.exception = builder.exception;
        this.performanceMetricsKey = builder.performanceMetricsKey;
    }

    /**
     * Create a new action step builder
     * 
     * @param stepName the step name
     * @param actionId the action ID
     * @param stepOrder the step order
     * @return the builder
     */
    public static Builder builder(String stepName, String actionId, int stepOrder) {
        return new Builder(stepName, actionId, stepOrder);
    }

    /**
     * Create a copy of this step with updated state
     * 
     * @param newState the new state
     * @return the updated step
     */
    public AgentModelActionStep withState(AgentModelActionStepState newState) {
        return new Builder(this).state(newState).build();
    }

    /**
     * Create a copy of this step with execution started
     * 
     * @return the updated step
     */
    public AgentModelActionStep withExecutionStarted() {
        return new Builder(this).state(AgentModelActionStepState.EXECUTING).startedAt(Instant.now()).build();
    }

    /**
     * Create a copy of this step with execution completed
     * 
     * @param result the execution result
     * @param actualDuration the actual execution duration
     * @return the updated step
     */
    public AgentModelActionStep withExecutionCompleted(ActionResult result, Duration actualDuration) {
        return new Builder(this).state(AgentModelActionStepState.COMPLETED).completedAt(Instant.now())
                .actualDuration(actualDuration).result(result).build();
    }

    /**
     * Create a copy of this step with execution failed
     * 
     * @param errorMessage the error message
     * @param exception the exception
     * @return the updated step
     */
    public AgentModelActionStep withExecutionFailed(String errorMessage, @Nullable Exception exception) {
        return new Builder(this).state(AgentModelActionStepState.FAILED).completedAt(Instant.now())
                .errorMessage(errorMessage).exception(exception).build();
    }

    // Getters
    public String getStepId() {
        return stepId;
    }

    public String getStepName() {
        return stepName;
    }

    public String getActionId() {
        return actionId;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public AgentModelActionStepState getState() {
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

    public @Nullable ActionResult getResult() {
        return result;
    }

    public @Nullable String getErrorMessage() {
        return errorMessage;
    }

    public @Nullable Exception getException() {
        return exception;
    }

    public @Nullable MetricKey getPerformanceMetricsKey() {
        return performanceMetricsKey;
    }

    /**
     * Get performance metrics from MetricsService using the stored key.
     * 
     * @param metricsService the metrics service to query
     * @return performance metrics snapshot, or null if key is not available
     */
    public @Nullable GenericMetricsSnapshot getPerformanceMetrics(@Nullable MetricsService metricsService) {
        if (performanceMetricsKey != null && metricsService != null) {
            return metricsService.getSnapshot(performanceMetricsKey, GenericMetricsSnapshot.class);
        }
        return null;
    }

    /**
     * Check if the step is ready for execution
     * 
     * @return true if ready for execution
     */
    public boolean isReadyForExecution() {
        return state == AgentModelActionStepState.READY && isValid;
    }

    /**
     * Check if the step is currently executing
     * 
     * @return true if currently executing
     */
    public boolean isExecuting() {
        return state == AgentModelActionStepState.EXECUTING;
    }

    /**
     * Check if the step has completed
     * 
     * @return true if completed
     */
    public boolean isCompleted() {
        return state == AgentModelActionStepState.COMPLETED;
    }

    /**
     * Check if the step has failed
     * 
     * @return true if failed
     */
    public boolean isFailed() {
        return state == AgentModelActionStepState.FAILED;
    }

    /**
     * Check if the step has dependencies
     * 
     * @return true if has dependencies
     */
    public boolean hasDependencies() {
        return !dependencies.isEmpty();
    }

    /**
     * Check if the step depends on another step
     * 
     * @param stepId the step ID to check
     * @return true if depends on the step
     */
    public boolean dependsOn(String stepId) {
        return dependencies.contains(stepId);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        AgentModelActionStep that = (AgentModelActionStep) obj;
        return Objects.equals(stepId, that.stepId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stepId);
    }

    @Override
    public String toString() {
        return String.format("AgentModelActionStep{stepId='%s', stepName='%s', actionId='%s', order=%d, state=%s}",
                stepId, stepName, actionId, stepOrder, state);
    }

    /**
     * Builder for AgentModelActionStep
     */
    public static final class Builder {
        private String stepId;
        private String stepName;
        private String actionId;
        private int stepOrder;
        private String description;
        private Map<String, Object> parameters;
        private Map<String, Object> context;
        private List<String> dependencies;
        private Map<String, Object> metadata;
        private AgentModelActionStepState state;
        private @Nullable Instant startedAt;
        private @Nullable Instant completedAt;
        private @Nullable Duration estimatedDuration;
        private @Nullable Duration actualDuration;
        private boolean isValid;
        private @Nullable String validationMessage;
        private double confidenceScore;
        private @Nullable List<String> warnings;
        private @Nullable ActionResult result;
        private @Nullable String errorMessage;
        private @Nullable Exception exception;
        private @Nullable MetricKey performanceMetricsKey;

        public Builder(String stepName, String actionId, int stepOrder) {
            this.stepId = "step_" + UUID.randomUUID().toString().replace("-", "");
            this.stepName = Objects.requireNonNull(stepName, "stepName");
            this.actionId = Objects.requireNonNull(actionId, "actionId");
            this.stepOrder = stepOrder;
            this.description = "";
            this.parameters = new HashMap<>();
            this.context = new HashMap<>();
            this.dependencies = new ArrayList<>();
            this.metadata = new HashMap<>();
            this.state = AgentModelActionStepState.CREATED;
            this.isValid = true;
            this.confidenceScore = 1.0;
        }

        public Builder(AgentModelActionStep source) {
            this.stepId = source.stepId;
            this.stepName = source.stepName;
            this.actionId = source.actionId;
            this.stepOrder = source.stepOrder;
            this.description = source.description;
            this.parameters = new HashMap<>(source.parameters);
            this.context = new HashMap<>(source.context);
            this.dependencies = new ArrayList<>(source.dependencies);
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
            this.result = source.result;
            this.errorMessage = source.errorMessage;
            this.exception = source.exception;
            this.performanceMetrics = source.performanceMetrics;
        }

        public Builder stepId(String stepId) {
            this.stepId = Objects.requireNonNull(stepId, "stepId");
            return this;
        }

        public Builder stepName(String stepName) {
            this.stepName = Objects.requireNonNull(stepName, "stepName");
            return this;
        }

        public Builder actionId(String actionId) {
            this.actionId = Objects.requireNonNull(actionId, "actionId");
            return this;
        }

        public Builder stepOrder(int stepOrder) {
            this.stepOrder = stepOrder;
            return this;
        }

        public Builder description(String description) {
            this.description = Objects.requireNonNull(description, "description");
            return this;
        }

        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = new HashMap<>(Objects.requireNonNull(parameters, "parameters"));
            return this;
        }

        public Builder addParameter(String key, Object value) {
            if (this.parameters == null) {
                this.parameters = new HashMap<>();
            }
            this.parameters.put(Objects.requireNonNull(key, "key"), value);
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

        public Builder dependencies(List<String> dependencies) {
            this.dependencies = new ArrayList<>(Objects.requireNonNull(dependencies, "dependencies"));
            return this;
        }

        public Builder addDependency(String dependency) {
            if (this.dependencies == null) {
                this.dependencies = new ArrayList<>();
            }
            this.dependencies.add(Objects.requireNonNull(dependency, "dependency"));
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

        public Builder state(AgentModelActionStepState state) {
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

        public Builder result(@Nullable ActionResult result) {
            this.result = result;
            return this;
        }

        public Builder errorMessage(@Nullable String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder exception(@Nullable Exception exception) {
            this.exception = exception;
            return this;
        }

        public Builder performanceMetricsKey(@Nullable MetricKey performanceMetricsKey) {
            this.performanceMetricsKey = performanceMetricsKey;
            return this;
        }

        public AgentModelActionStep build() {
            return new AgentModelActionStep(this);
        }
    }
}
