package org.openhab.core.ai.agent.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.validation.BaseValidationResult;

/**
 * Validation result for model-based action plans
 * 
 * <p>
 * This class provides:
 * - Plan validation status and results extending BaseValidationResult
 * - Plan-specific validation errors and warnings
 * - Safety and security checks for action plans
 * - Compliance validation for agent behavior
 * - Recommendations for plan improvement
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelActionPlanValidationResult extends BaseValidationResult {

    private final String planId;
    private final int stepCount;
    private final boolean safetyCompliant;
    private final boolean securityCompliant;
    private final List<String> recommendations;

    /**
     * Private constructor for internal use.
     */
    private AgentModelActionPlanValidationResult(boolean valid, List<String> errors, List<String> warnings,
            Map<String, Object> details, @Nullable Instant validationTime, String planId, int stepCount,
            boolean safetyCompliant, boolean securityCompliant, List<String> recommendations) {
        super(valid, errors, warnings, details, validationTime);
        this.planId = planId;
        this.stepCount = stepCount;
        this.safetyCompliant = safetyCompliant;
        this.securityCompliant = securityCompliant;
        this.recommendations = List.copyOf(recommendations);
    }

    /**
     * Get the plan identifier.
     * 
     * @return the plan ID
     */
    public String getPlanId() {
        return planId;
    }

    /**
     * Get the number of steps in the plan.
     * 
     * @return the step count
     */
    public int getStepCount() {
        return stepCount;
    }

    /**
     * Check if the plan is safety compliant.
     * 
     * @return true if safety compliant
     */
    public boolean isSafetyCompliant() {
        return safetyCompliant;
    }

    /**
     * Check if the plan is security compliant.
     * 
     * @return true if security compliant
     */
    public boolean isSecurityCompliant() {
        return securityCompliant;
    }

    /**
     * Get validation recommendations.
     * 
     * @return list of recommendations
     */
    public List<String> getRecommendations() {
        return recommendations;
    }

    /**
     * Check if the plan requires attention based on compliance or recommendations.
     * 
     * @return true if the plan needs attention
     */
    public boolean requiresAttention() {
        return !safetyCompliant || !securityCompliant || !recommendations.isEmpty();
    }

    /**
     * Get the overall compliance score (0.0-1.0).
     * 
     * @return compliance score
     */
    public double getComplianceScore() {
        int totalChecks = 2; // safety and security
        int passedChecks = 0;
        if (safetyCompliant)
            passedChecks++;
        if (securityCompliant)
            passedChecks++;
        return (double) passedChecks / totalChecks;
    }

    /**
     * Builder for AgentModelActionPlanValidationResult.
     */
    public static final class Builder {
        private boolean valid = true;
        private List<String> errors = List.of();
        private List<String> warnings = List.of();
        private Map<String, Object> details = Map.of();
        private @Nullable Instant validationTime = null;
        private String planId = "";
        private int stepCount = 0;
        private boolean safetyCompliant = true;
        private boolean securityCompliant = true;
        private List<String> recommendations = List.of();

        public Builder() {
            // Default constructor
        }

        public Builder(AgentModelActionPlanValidationResult source) {
            this.valid = source.isValid();
            this.errors = source.getErrors();
            this.warnings = source.getWarnings();
            this.details = source.getDetails();
            this.validationTime = source.getValidationTime();
            this.planId = source.planId;
            this.stepCount = source.stepCount;
            this.safetyCompliant = source.safetyCompliant;
            this.securityCompliant = source.securityCompliant;
            this.recommendations = source.recommendations;
        }

        public Builder withValid(boolean valid) {
            this.valid = valid;
            return this;
        }

        public Builder withErrors(List<String> errors) {
            this.errors = List.copyOf(Objects.requireNonNull(errors, "errors"));
            return this;
        }

        public Builder withWarnings(List<String> warnings) {
            this.warnings = List.copyOf(Objects.requireNonNull(warnings, "warnings"));
            return this;
        }

        public Builder withDetails(Map<String, Object> details) {
            this.details = Map.copyOf(Objects.requireNonNull(details, "details"));
            return this;
        }

        public Builder withValidationTime(@Nullable Instant validationTime) {
            this.validationTime = validationTime;
            return this;
        }

        public Builder withPlanId(String planId) {
            this.planId = Objects.requireNonNull(planId, "planId");
            return this;
        }

        public Builder withStepCount(int stepCount) {
            this.stepCount = stepCount;
            return this;
        }

        public Builder withSafetyCompliant(boolean safetyCompliant) {
            this.safetyCompliant = safetyCompliant;
            return this;
        }

        public Builder withSecurityCompliant(boolean securityCompliant) {
            this.securityCompliant = securityCompliant;
            return this;
        }

        public Builder withRecommendations(List<String> recommendations) {
            this.recommendations = List.copyOf(Objects.requireNonNull(recommendations, "recommendations"));
            return this;
        }

        public AgentModelActionPlanValidationResult build() {
            validate();
            return new AgentModelActionPlanValidationResult(valid, errors, warnings, details, validationTime, planId,
                    stepCount, safetyCompliant, securityCompliant, recommendations);
        }

        private void validate() {
            if (planId.trim().isEmpty()) {
                throw new IllegalArgumentException("planId must not be empty");
            }
            if (stepCount < 0) {
                throw new IllegalArgumentException("stepCount must be non-negative");
            }
        }
    }

    /**
     * Create a successful validation result.
     * 
     * @param planId the plan identifier
     * @param stepCount the number of steps
     * @return a successful validation result
     */
    public static AgentModelActionPlanValidationResult success(String planId, int stepCount) {
        return builder().withValid(true).withPlanId(planId).withStepCount(stepCount).withValidationTime(Instant.now())
                .build();
    }

    /**
     * Create a failed validation result.
     * 
     * @param planId the plan identifier
     * @param stepCount the number of steps
     * @param errors the validation errors
     * @return a failed validation result
     */
    public static AgentModelActionPlanValidationResult failure(String planId, int stepCount, List<String> errors) {
        return builder().withValid(false).withPlanId(planId).withStepCount(stepCount).withErrors(errors)
                .withValidationTime(Instant.now()).build();
    }

    /**
     * Create a new builder for AgentModelActionPlanValidationResult.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new builder for AgentModelActionPlanValidationResult from an existing instance.
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
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        AgentModelActionPlanValidationResult other = (AgentModelActionPlanValidationResult) obj;
        return Objects.equals(planId, other.planId) && stepCount == other.stepCount
                && safetyCompliant == other.safetyCompliant && securityCompliant == other.securityCompliant
                && Objects.equals(recommendations, other.recommendations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), planId, stepCount, safetyCompliant, securityCompliant, recommendations);
    }

    @Override
    public String toString() {
        return String.format(
                "AgentModelActionPlanValidationResult{planId='%s', stepCount=%d, valid=%s, "
                        + "safetyCompliant=%s, securityCompliant=%s, errors=%d, warnings=%d, recommendations=%d}",
                planId, stepCount, isValid(), safetyCompliant, securityCompliant, getErrorCount(), getWarningCount(),
                recommendations.size());
    }
}
