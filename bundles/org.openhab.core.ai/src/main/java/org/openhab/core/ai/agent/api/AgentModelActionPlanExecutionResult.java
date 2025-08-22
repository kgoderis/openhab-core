package org.openhab.core.ai.agent.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.common.response.Response;

/**
 * Execution result for model-based action plans
 * 
 * <p>
 * This class provides:
 * - Plan execution status and results implementing Response interface
 * - Step execution results and outcomes
 * - Performance metrics and timing information
 * - Error handling and recovery details
 * - Execution summary and statistics
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelActionPlanExecutionResult implements Response<AgentModelActionPlan> {

    private final String id;
    private final boolean success;
    private final @Nullable AgentModelActionPlan data;
    private final @Nullable String errorMessage;
    private final long timestamp;
    private final String planId;
    private final AgentModelActionPlanState finalState;
    private final List<ActionResult> stepResults;
    private final long executionTimeMs;
    private final int completedSteps;
    private final int totalSteps;
    private final @Nullable String failureReason;
    private final Map<String, Object> executionMetadata;

    /**
     * Private constructor for internal use.
     */
    private AgentModelActionPlanExecutionResult(String id, boolean success, @Nullable AgentModelActionPlan data,
            @Nullable String errorMessage, long timestamp, String planId, AgentModelActionPlanState finalState,
            List<ActionResult> stepResults, long executionTimeMs, int completedSteps, int totalSteps,
            @Nullable String failureReason, Map<String, Object> executionMetadata) {
        this.id = id;
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
        this.timestamp = timestamp;
        this.planId = planId;
        this.finalState = finalState;
        this.stepResults = List.copyOf(stepResults);
        this.executionTimeMs = executionTimeMs;
        this.completedSteps = completedSteps;
        this.totalSteps = totalSteps;
        this.failureReason = failureReason;
        this.executionMetadata = Map.copyOf(executionMetadata);
    }

    // ===== Response Interface Implementation =====

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public @Nullable AgentModelActionPlan getData() {
        return data;
    }

    @Override
    public @Nullable String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    // ===== Execution Result Specific Methods =====

    /**
     * Get the plan identifier.
     * 
     * @return the plan ID
     */
    public String getPlanId() {
        return planId;
    }

    /**
     * Get the final execution state.
     * 
     * @return the final state
     */
    public AgentModelActionPlanState getFinalState() {
        return finalState;
    }

    /**
     * Get the step execution results.
     * 
     * @return list of step results
     */
    public List<ActionResult> getStepResults() {
        return stepResults;
    }

    /**
     * Get the total execution time in milliseconds.
     * 
     * @return execution time in milliseconds
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    /**
     * Get the number of completed steps.
     * 
     * @return completed step count
     */
    public int getCompletedSteps() {
        return completedSteps;
    }

    /**
     * Get the total number of steps.
     * 
     * @return total step count
     */
    public int getTotalSteps() {
        return totalSteps;
    }

    /**
     * Get the failure reason if execution failed.
     * 
     * @return failure reason or null if successful
     */
    public @Nullable String getFailureReason() {
        return failureReason;
    }

    /**
     * Get execution metadata.
     * 
     * @return execution metadata map
     */
    public Map<String, Object> getExecutionMetadata() {
        return executionMetadata;
    }

    /**
     * Calculate the completion percentage.
     * 
     * @return completion percentage (0.0-100.0)
     */
    public double getCompletionPercentage() {
        if (totalSteps == 0) {
            return 0.0;
        }
        return (double) completedSteps / totalSteps * 100.0;
    }

    /**
     * Check if execution was partially completed.
     * 
     * @return true if some but not all steps were completed
     */
    public boolean isPartiallyCompleted() {
        return completedSteps > 0 && completedSteps < totalSteps;
    }

    /**
     * Check if execution was fully completed.
     * 
     * @return true if all steps were completed successfully
     */
    public boolean isFullyCompleted() {
        return success && completedSteps == totalSteps;
    }

    /**
     * Get the execution timestamp as an Instant.
     * 
     * @return execution timestamp
     */
    public Instant getExecutionTimestamp() {
        return Instant.ofEpochMilli(timestamp);
    }

    /**
     * Builder for AgentModelActionPlanExecutionResult.
     */
    public static final class Builder {
        private String id = UUID.randomUUID().toString();
        private boolean success = false;
        private @Nullable AgentModelActionPlan data = null;
        private @Nullable String errorMessage = null;
        private long timestamp = System.currentTimeMillis();
        private String planId = "";
        private AgentModelActionPlanState finalState = AgentModelActionPlanState.FAILED;
        private List<ActionResult> stepResults = List.of();
        private long executionTimeMs = 0;
        private int completedSteps = 0;
        private int totalSteps = 0;
        private @Nullable String failureReason = null;
        private Map<String, Object> executionMetadata = Map.of();

        public Builder() {
            // Default constructor
        }

        public Builder(AgentModelActionPlanExecutionResult source) {
            this.id = source.id;
            this.success = source.success;
            this.data = source.data;
            this.errorMessage = source.errorMessage;
            this.timestamp = source.timestamp;
            this.planId = source.planId;
            this.finalState = source.finalState;
            this.stepResults = source.stepResults;
            this.executionTimeMs = source.executionTimeMs;
            this.completedSteps = source.completedSteps;
            this.totalSteps = source.totalSteps;
            this.failureReason = source.failureReason;
            this.executionMetadata = source.executionMetadata;
        }

        public Builder withId(String id) {
            this.id = Objects.requireNonNull(id, "id");
            return this;
        }

        public Builder withSuccess(boolean success) {
            this.success = success;
            return this;
        }

        public Builder withData(@Nullable AgentModelActionPlan data) {
            this.data = data;
            return this;
        }

        public Builder withErrorMessage(@Nullable String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder withTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withPlanId(String planId) {
            this.planId = Objects.requireNonNull(planId, "planId");
            return this;
        }

        public Builder withFinalState(AgentModelActionPlanState finalState) {
            this.finalState = Objects.requireNonNull(finalState, "finalState");
            return this;
        }

        public Builder withStepResults(List<ActionResult> stepResults) {
            this.stepResults = List.copyOf(Objects.requireNonNull(stepResults, "stepResults"));
            return this;
        }

        public Builder withExecutionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }

        public Builder withCompletedSteps(int completedSteps) {
            this.completedSteps = completedSteps;
            return this;
        }

        public Builder withTotalSteps(int totalSteps) {
            this.totalSteps = totalSteps;
            return this;
        }

        public Builder withFailureReason(@Nullable String failureReason) {
            this.failureReason = failureReason;
            return this;
        }

        public Builder withExecutionMetadata(Map<String, Object> executionMetadata) {
            this.executionMetadata = Map.copyOf(Objects.requireNonNull(executionMetadata, "executionMetadata"));
            return this;
        }

        public AgentModelActionPlanExecutionResult build() {
            validate();
            return new AgentModelActionPlanExecutionResult(id, success, data, errorMessage, timestamp, planId,
                    finalState, stepResults, executionTimeMs, completedSteps, totalSteps, failureReason,
                    executionMetadata);
        }

        private void validate() {
            if (id.trim().isEmpty()) {
                throw new IllegalArgumentException("id must not be empty");
            }
            if (planId.trim().isEmpty()) {
                throw new IllegalArgumentException("planId must not be empty");
            }
            if (executionTimeMs < 0) {
                throw new IllegalArgumentException("executionTimeMs must be non-negative");
            }
            if (completedSteps < 0) {
                throw new IllegalArgumentException("completedSteps must be non-negative");
            }
            if (totalSteps < 0) {
                throw new IllegalArgumentException("totalSteps must be non-negative");
            }
            if (completedSteps > totalSteps) {
                throw new IllegalArgumentException("completedSteps cannot exceed totalSteps");
            }
        }
    }

    /**
     * Create a successful execution result.
     * 
     * @param planId the plan identifier
     * @param executedPlan the successfully executed plan
     * @param stepResults the step execution results
     * @param executionTimeMs the execution time
     * @return a successful execution result
     */
    public static AgentModelActionPlanExecutionResult success(String planId, AgentModelActionPlan executedPlan,
            List<ActionResult> stepResults, long executionTimeMs) {
        return builder().withSuccess(true).withPlanId(planId).withData(executedPlan)
                .withFinalState(AgentModelActionPlanState.COMPLETED).withStepResults(stepResults)
                .withExecutionTimeMs(executionTimeMs).withCompletedSteps(stepResults.size())
                .withTotalSteps(stepResults.size()).build();
    }

    /**
     * Create a failed execution result.
     * 
     * @param planId the plan identifier
     * @param failureReason the reason for failure
     * @param stepResults the partial step results
     * @param executionTimeMs the execution time
     * @return a failed execution result
     */
    public static AgentModelActionPlanExecutionResult failure(String planId, String failureReason,
            List<ActionResult> stepResults, long executionTimeMs) {
        return builder().withSuccess(false).withPlanId(planId).withErrorMessage(failureReason)
                .withFailureReason(failureReason).withFinalState(AgentModelActionPlanState.FAILED)
                .withStepResults(stepResults).withExecutionTimeMs(executionTimeMs)
                .withCompletedSteps(stepResults.size()).build();
    }

    /**
     * Create a new builder for AgentModelActionPlanExecutionResult.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new builder for AgentModelActionPlanExecutionResult from an existing instance.
     *
     * @return a new builder instance initialized with this instance's values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AgentModelActionPlanExecutionResult other = (AgentModelActionPlanExecutionResult) obj;
        return Objects.equals(id, other.id) && success == other.success && Objects.equals(data, other.data)
                && Objects.equals(errorMessage, other.errorMessage) && timestamp == other.timestamp
                && Objects.equals(planId, other.planId) && finalState == other.finalState
                && Objects.equals(stepResults, other.stepResults) && executionTimeMs == other.executionTimeMs
                && completedSteps == other.completedSteps && totalSteps == other.totalSteps
                && Objects.equals(failureReason, other.failureReason)
                && Objects.equals(executionMetadata, other.executionMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, success, data, errorMessage, timestamp, planId, finalState, stepResults,
                executionTimeMs, completedSteps, totalSteps, failureReason, executionMetadata);
    }

    @Override
    public String toString() {
        return String.format(
                "AgentModelActionPlanExecutionResult{id='%s', planId='%s', success=%s, "
                        + "finalState=%s, completedSteps=%d/%d, executionTimeMs=%d, timestamp=%d}",
                id, planId, success, finalState, completedSteps, totalSteps, executionTimeMs, timestamp);
    }
}
