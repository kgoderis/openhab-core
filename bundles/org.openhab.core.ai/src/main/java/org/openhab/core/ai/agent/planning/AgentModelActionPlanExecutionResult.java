package org.openhab.core.ai.agent.planning;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.action.api.ActionResult;

/**
 * Result of action plan execution.
 * 
 * <p>
 * This class extends the base ActionResult to provide action plan specific
 * execution results including individual action results and plan-level metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelActionPlanExecutionResult {

    private final String planId;
    private final List<ActionResult> actionResults;
    private final long totalDurationMs;
    private final int successCount;
    private final int failureCount;
    private final Instant completedAt;

    private AgentModelActionPlanExecutionResult(Builder builder) {
        this.planId = Objects.requireNonNull(builder.planId, "planId");
        this.actionResults = List.copyOf(builder.actionResults);
        this.totalDurationMs = builder.totalDurationMs;
        this.successCount = builder.successCount;
        this.failureCount = builder.failureCount;
        this.completedAt = Objects.requireNonNull(builder.completedAt, "completedAt");
    }

    /**
     * Create a new builder for AgentModelActionPlanExecutionResult.
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

    public List<ActionResult> getActionResults() {
        return actionResults;
    }

    public long getTotalDurationMs() {
        return totalDurationMs;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    /**
     * Check if the plan execution was successful.
     * 
     * @return true if all actions succeeded, false otherwise
     */
    public boolean isSuccess() {
        return failureCount == 0;
    }

    /**
     * Get the overall success rate.
     * 
     * @return success rate as a percentage (0.0 to 1.0)
     */
    public double getSuccessRate() {
        int total = actionResults.size();
        return total > 0 ? (double) successCount / total : 0.0;
    }

    /**
     * Convert to base ActionResult for compatibility.
     * 
     * @return ActionResult representation of this execution result
     */
    public ActionResult toActionResult() {
        Map<String, Object> metadata = Map.of("planId", planId, "totalActions", actionResults.size(), "successCount",
                successCount, "failureCount", failureCount, "successRate", getSuccessRate(), "completedAt",
                completedAt.toString());

        if (isSuccess()) {
            return ActionResult.success(actionResults, totalDurationMs, metadata);
        } else {
            return ActionResult.error("Plan execution completed with failures",
                    new org.openhab.core.ai.action.api.ActionError("PLAN_EXECUTION_FAILED",
                            String.format("Failed %d of %d actions", failureCount, actionResults.size())),
                    totalDurationMs, metadata);
        }
    }

    /**
     * Builder for AgentModelActionPlanExecutionResult.
     */
    public static final class Builder {
        private String planId = "";
        private List<ActionResult> actionResults = List.of();
        private long totalDurationMs = 0L;
        private int successCount = 0;
        private int failureCount = 0;
        private Instant completedAt = Instant.now();

        public Builder() {
        }

        public Builder(AgentModelActionPlanExecutionResult source) {
            this.planId = source.planId;
            this.actionResults = source.actionResults;
            this.totalDurationMs = source.totalDurationMs;
            this.successCount = source.successCount;
            this.failureCount = source.failureCount;
            this.completedAt = source.completedAt;
        }

        public Builder withPlanId(String planId) {
            this.planId = planId;
            return this;
        }

        public Builder withActionResults(List<ActionResult> actionResults) {
            this.actionResults = actionResults;
            return this;
        }

        public Builder withTotalDurationMs(long totalDurationMs) {
            this.totalDurationMs = totalDurationMs;
            return this;
        }

        public Builder withSuccessCount(int successCount) {
            this.successCount = successCount;
            return this;
        }

        public Builder withFailureCount(int failureCount) {
            this.failureCount = failureCount;
            return this;
        }

        public Builder withCompletedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public AgentModelActionPlanExecutionResult build() {
            if (totalDurationMs < 0) {
                throw new IllegalArgumentException("totalDurationMs must be non-negative");
            }
            if (successCount < 0) {
                throw new IllegalArgumentException("successCount must be non-negative");
            }
            if (failureCount < 0) {
                throw new IllegalArgumentException("failureCount must be non-negative");
            }
            return new AgentModelActionPlanExecutionResult(this);
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
        AgentModelActionPlanExecutionResult other = (AgentModelActionPlanExecutionResult) obj;
        return totalDurationMs == other.totalDurationMs && successCount == other.successCount
                && failureCount == other.failureCount && Objects.equals(planId, other.planId)
                && Objects.equals(actionResults, other.actionResults) && Objects.equals(completedAt, other.completedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId, actionResults, totalDurationMs, successCount, failureCount, completedAt);
    }

    @Override
    public String toString() {
        return String.format(
                "AgentModelActionPlanExecutionResult{planId='%s', actions=%d, success=%d, failure=%d, duration=%dms}",
                planId, actionResults.size(), successCount, failureCount, totalDurationMs);
    }
}
