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
 * Rollback result for model-based action plans
 * 
 * <p>
 * This class provides:
 * - Plan rollback status and results implementing Response interface
 * - Step rollback results and outcomes
 * - Recovery metrics and timing information
 * - Error handling and failure details
 * - Rollback summary and statistics
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelActionPlanRollbackResult implements Response<AgentModelActionPlan> {

    private final String id;
    private final boolean success;
    private final @Nullable AgentModelActionPlan data;
    private final @Nullable String errorMessage;
    private final long timestamp;
    private final String planId;
    private final AgentModelActionPlanState originalState;
    private final AgentModelActionPlanState finalState;
    private final List<ActionResult> rollbackResults;
    private final long rollbackTimeMs;
    private final int rolledBackSteps;
    private final int totalStepsToRollback;
    private final @Nullable String rollbackFailureReason;
    private final Map<String, Object> rollbackMetadata;

    /**
     * Private constructor for internal use.
     */
    private AgentModelActionPlanRollbackResult(String id, boolean success, @Nullable AgentModelActionPlan data,
            @Nullable String errorMessage, long timestamp, String planId, AgentModelActionPlanState originalState,
            AgentModelActionPlanState finalState, List<ActionResult> rollbackResults, long rollbackTimeMs,
            int rolledBackSteps, int totalStepsToRollback, @Nullable String rollbackFailureReason,
            Map<String, Object> rollbackMetadata) {
        this.id = id;
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
        this.timestamp = timestamp;
        this.planId = planId;
        this.originalState = originalState;
        this.finalState = finalState;
        this.rollbackResults = List.copyOf(rollbackResults);
        this.rollbackTimeMs = rollbackTimeMs;
        this.rolledBackSteps = rolledBackSteps;
        this.totalStepsToRollback = totalStepsToRollback;
        this.rollbackFailureReason = rollbackFailureReason;
        this.rollbackMetadata = Map.copyOf(rollbackMetadata);
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

    // ===== Rollback Result Specific Methods =====

    /**
     * Get the plan identifier.
     * 
     * @return the plan ID
     */
    public String getPlanId() {
        return planId;
    }

    /**
     * Get the original state before rollback.
     * 
     * @return the original state
     */
    public AgentModelActionPlanState getOriginalState() {
        return originalState;
    }

    /**
     * Get the final state after rollback.
     * 
     * @return the final state
     */
    public AgentModelActionPlanState getFinalState() {
        return finalState;
    }

    /**
     * Get the rollback step results.
     * 
     * @return list of rollback results
     */
    public List<ActionResult> getRollbackResults() {
        return rollbackResults;
    }

    /**
     * Get the total rollback time in milliseconds.
     * 
     * @return rollback time in milliseconds
     */
    public long getRollbackTimeMs() {
        return rollbackTimeMs;
    }

    /**
     * Get the number of steps successfully rolled back.
     * 
     * @return rolled back step count
     */
    public int getRolledBackSteps() {
        return rolledBackSteps;
    }

    /**
     * Get the total number of steps that needed rollback.
     * 
     * @return total steps to rollback
     */
    public int getTotalStepsToRollback() {
        return totalStepsToRollback;
    }

    /**
     * Get the rollback failure reason if rollback failed.
     * 
     * @return rollback failure reason or null if successful
     */
    public @Nullable String getRollbackFailureReason() {
        return rollbackFailureReason;
    }

    /**
     * Get rollback metadata.
     * 
     * @return rollback metadata map
     */
    public Map<String, Object> getRollbackMetadata() {
        return rollbackMetadata;
    }

    /**
     * Calculate the rollback completion percentage.
     * 
     * @return rollback completion percentage (0.0-100.0)
     */
    public double getRollbackCompletionPercentage() {
        if (totalStepsToRollback == 0) {
            return 100.0; // Nothing to rollback
        }
        return (double) rolledBackSteps / totalStepsToRollback * 100.0;
    }

    /**
     * Check if rollback was partially completed.
     * 
     * @return true if some but not all steps were rolled back
     */
    public boolean isPartiallyRolledBack() {
        return rolledBackSteps > 0 && rolledBackSteps < totalStepsToRollback;
    }

    /**
     * Check if rollback was fully completed.
     * 
     * @return true if all steps were rolled back successfully
     */
    public boolean isFullyRolledBack() {
        return success && rolledBackSteps == totalStepsToRollback;
    }

    /**
     * Check if any rollback was attempted.
     * 
     * @return true if at least one step was attempted to be rolled back
     */
    public boolean hasRollbackAttempts() {
        return !rollbackResults.isEmpty();
    }

    /**
     * Get the rollback timestamp as an Instant.
     * 
     * @return rollback timestamp
     */
    public Instant getRollbackTimestamp() {
        return Instant.ofEpochMilli(timestamp);
    }

    /**
     * Builder for AgentModelActionPlanRollbackResult.
     */
    public static final class Builder {
        private String id = UUID.randomUUID().toString();
        private boolean success = false;
        private @Nullable AgentModelActionPlan data = null;
        private @Nullable String errorMessage = null;
        private long timestamp = System.currentTimeMillis();
        private String planId = "";
        private AgentModelActionPlanState originalState = AgentModelActionPlanState.FAILED;
        private AgentModelActionPlanState finalState = AgentModelActionPlanState.FAILED;
        private List<ActionResult> rollbackResults = List.of();
        private long rollbackTimeMs = 0;
        private int rolledBackSteps = 0;
        private int totalStepsToRollback = 0;
        private @Nullable String rollbackFailureReason = null;
        private Map<String, Object> rollbackMetadata = Map.of();

        public Builder() {
            // Default constructor
        }

        public Builder(AgentModelActionPlanRollbackResult source) {
            this.id = source.id;
            this.success = source.success;
            this.data = source.data;
            this.errorMessage = source.errorMessage;
            this.timestamp = source.timestamp;
            this.planId = source.planId;
            this.originalState = source.originalState;
            this.finalState = source.finalState;
            this.rollbackResults = source.rollbackResults;
            this.rollbackTimeMs = source.rollbackTimeMs;
            this.rolledBackSteps = source.rolledBackSteps;
            this.totalStepsToRollback = source.totalStepsToRollback;
            this.rollbackFailureReason = source.rollbackFailureReason;
            this.rollbackMetadata = source.rollbackMetadata;
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

        public Builder withOriginalState(AgentModelActionPlanState originalState) {
            this.originalState = Objects.requireNonNull(originalState, "originalState");
            return this;
        }

        public Builder withFinalState(AgentModelActionPlanState finalState) {
            this.finalState = Objects.requireNonNull(finalState, "finalState");
            return this;
        }

        public Builder withRollbackResults(List<ActionResult> rollbackResults) {
            this.rollbackResults = List.copyOf(Objects.requireNonNull(rollbackResults, "rollbackResults"));
            return this;
        }

        public Builder withRollbackTimeMs(long rollbackTimeMs) {
            this.rollbackTimeMs = rollbackTimeMs;
            return this;
        }

        public Builder withRolledBackSteps(int rolledBackSteps) {
            this.rolledBackSteps = rolledBackSteps;
            return this;
        }

        public Builder withTotalStepsToRollback(int totalStepsToRollback) {
            this.totalStepsToRollback = totalStepsToRollback;
            return this;
        }

        public Builder withRollbackFailureReason(@Nullable String rollbackFailureReason) {
            this.rollbackFailureReason = rollbackFailureReason;
            return this;
        }

        public Builder withRollbackMetadata(Map<String, Object> rollbackMetadata) {
            this.rollbackMetadata = Map.copyOf(Objects.requireNonNull(rollbackMetadata, "rollbackMetadata"));
            return this;
        }

        public AgentModelActionPlanRollbackResult build() {
            validate();
            return new AgentModelActionPlanRollbackResult(id, success, data, errorMessage, timestamp, planId,
                    originalState, finalState, rollbackResults, rollbackTimeMs, rolledBackSteps, totalStepsToRollback,
                    rollbackFailureReason, rollbackMetadata);
        }

        private void validate() {
            if (id.trim().isEmpty()) {
                throw new IllegalArgumentException("id must not be empty");
            }
            if (planId.trim().isEmpty()) {
                throw new IllegalArgumentException("planId must not be empty");
            }
            if (rollbackTimeMs < 0) {
                throw new IllegalArgumentException("rollbackTimeMs must be non-negative");
            }
            if (rolledBackSteps < 0) {
                throw new IllegalArgumentException("rolledBackSteps must be non-negative");
            }
            if (totalStepsToRollback < 0) {
                throw new IllegalArgumentException("totalStepsToRollback must be non-negative");
            }
            if (rolledBackSteps > totalStepsToRollback) {
                throw new IllegalArgumentException("rolledBackSteps cannot exceed totalStepsToRollback");
            }
        }
    }

    /**
     * Create a successful rollback result.
     * 
     * @param planId the plan identifier
     * @param rolledBackPlan the successfully rolled back plan
     * @param rollbackResults the rollback step results
     * @param rollbackTimeMs the rollback time
     * @return a successful rollback result
     */
    public static AgentModelActionPlanRollbackResult success(String planId, AgentModelActionPlan rolledBackPlan,
            List<ActionResult> rollbackResults, long rollbackTimeMs) {
        return builder().withSuccess(true).withPlanId(planId).withData(rolledBackPlan)
                .withFinalState(AgentModelActionPlanState.ROLLED_BACK).withRollbackResults(rollbackResults)
                .withRollbackTimeMs(rollbackTimeMs).withRolledBackSteps(rollbackResults.size())
                .withTotalStepsToRollback(rollbackResults.size()).build();
    }

    /**
     * Create a failed rollback result.
     * 
     * @param planId the plan identifier
     * @param failureReason the reason for rollback failure
     * @param rollbackResults the partial rollback results
     * @param rollbackTimeMs the rollback time
     * @return a failed rollback result
     */
    public static AgentModelActionPlanRollbackResult failure(String planId, String failureReason,
            List<ActionResult> rollbackResults, long rollbackTimeMs) {
        return builder().withSuccess(false).withPlanId(planId).withErrorMessage(failureReason)
                .withRollbackFailureReason(failureReason).withFinalState(AgentModelActionPlanState.ROLLBACK_FAILED)
                .withRollbackResults(rollbackResults).withRollbackTimeMs(rollbackTimeMs)
                .withRolledBackSteps(rollbackResults.size()).build();
    }

    /**
     * Create a new builder for AgentModelActionPlanRollbackResult.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new builder for AgentModelActionPlanRollbackResult from an existing instance.
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
        AgentModelActionPlanRollbackResult other = (AgentModelActionPlanRollbackResult) obj;
        return Objects.equals(id, other.id) && success == other.success && Objects.equals(data, other.data)
                && Objects.equals(errorMessage, other.errorMessage) && timestamp == other.timestamp
                && Objects.equals(planId, other.planId) && originalState == other.originalState
                && finalState == other.finalState && Objects.equals(rollbackResults, other.rollbackResults)
                && rollbackTimeMs == other.rollbackTimeMs && rolledBackSteps == other.rolledBackSteps
                && totalStepsToRollback == other.totalStepsToRollback
                && Objects.equals(rollbackFailureReason, other.rollbackFailureReason)
                && Objects.equals(rollbackMetadata, other.rollbackMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, success, data, errorMessage, timestamp, planId, originalState, finalState,
                rollbackResults, rollbackTimeMs, rolledBackSteps, totalStepsToRollback, rollbackFailureReason,
                rollbackMetadata);
    }

    @Override
    public String toString() {
        return String.format(
                "AgentModelActionPlanRollbackResult{id='%s', planId='%s', success=%s, "
                        + "originalState=%s, finalState=%s, rolledBackSteps=%d/%d, rollbackTimeMs=%d, timestamp=%d}",
                id, planId, success, originalState, finalState, rolledBackSteps, totalStepsToRollback, rollbackTimeMs,
                timestamp);
    }
}
