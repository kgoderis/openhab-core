package org.openhab.core.ai.agent.planning;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.ActionValidationResult;
import org.openhab.core.ai.common.validation.BaseValidationResult;

/**
 * Validation result for agent model action plans.
 *
 * <p>
 * This class extends BaseValidationResult to provide validation results for action plans,
 * including validation status, errors, warnings, and detailed information about the
 * validation process.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelActionPlanValidationResult extends BaseValidationResult {

    /**
     * Create a new validation result.
     *
     * @param valid whether the validation passed
     * @param errors list of validation errors
     * @param warnings list of validation warnings
     * @param details additional validation details
     * @param validationTime timestamp of the validation
     */
    public AgentModelActionPlanValidationResult(boolean valid, List<String> errors, List<String> warnings,
            Map<String, Object> details, @Nullable Instant validationTime) {
        super(valid, errors, warnings, details, validationTime);
    }

    /**
     * Create a new validation result with current timestamp.
     *
     * @param valid whether the validation passed
     * @param errors list of validation errors
     * @param warnings list of validation warnings
     * @param details additional validation details
     */
    public AgentModelActionPlanValidationResult(boolean valid, List<String> errors, List<String> warnings,
            Map<String, Object> details) {
        super(valid, errors, warnings, details, Instant.now());
    }

    /**
     * Create a valid validation result.
     *
     * @return valid validation result
     */
    public static AgentModelActionPlanValidationResult valid() {
        return new AgentModelActionPlanValidationResult(true, List.of(), List.of(), Map.of());
    }

    /**
     * Create a valid validation result with details.
     *
     * @param details validation details
     * @return valid validation result
     */
    public static AgentModelActionPlanValidationResult valid(Map<String, Object> details) {
        return new AgentModelActionPlanValidationResult(true, List.of(), List.of(), details);
    }

    /**
     * Create an invalid validation result.
     *
     * @param errors list of validation errors
     * @return invalid validation result
     */
    public static AgentModelActionPlanValidationResult invalid(List<String> errors) {
        return new AgentModelActionPlanValidationResult(false, errors, List.of(), Map.of());
    }

    /**
     * Create an invalid validation result with details.
     *
     * @param errors list of validation errors
     * @param details validation details
     * @return invalid validation result
     */
    public static AgentModelActionPlanValidationResult invalid(List<String> errors, Map<String, Object> details) {
        return new AgentModelActionPlanValidationResult(false, errors, List.of(), details);
    }

    /**
     * Get all validation issues (errors and warnings combined).
     *
     * @return list of all validation issues
     */
    public List<String> getIssues() {
        List<String> issues = new ArrayList<>();
        issues.addAll(getErrors());
        issues.addAll(getWarnings());
        return issues;
    }

    /**
     * Convert to base ActionValidationResult for compatibility.
     *
     * @return ActionValidationResult representation
     */
    public ActionValidationResult toActionValidationResult() {
        if (isValid()) {
            return hasWarnings() ? ActionValidationResult.validWithWarnings(getDetails(), getWarnings())
                    : ActionValidationResult.valid(getDetails());
        } else {
            return hasWarnings() ? ActionValidationResult.invalidWithWarnings(getErrors(), getWarnings())
                    : ActionValidationResult.invalid(getErrors());
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        return true; // No additional fields to compare
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode()); // No additional fields to hash
    }

    @Override
    public String toString() {
        return String.format("AgentModelActionPlanValidationResult{valid=%s, errors=%d, warnings=%d, summary='%s'}",
                isValid(), getErrorCount(), getWarningCount(), getSummary());
    }
}
