package org.openhab.core.ai.reasoning.metrics;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Comprehensive safety performance metrics including validation performance and safety incidents.
 *
 * <p>
 * This class provides aggregated metrics for safety validation performance,
 * constraint violations, safety incidents, and related statistics.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SafetyPerformanceMetrics {
    private final long totalSafetyValidations;
    private final long successfulValidations;
    private final long failedValidations;
    private final long totalConstraintViolations;
    private final long totalSafetyIncidents;
    private final long totalSafetyOverrides;
    private final long totalValidationTimeMs;
    private final double averageValidationTimeMs;
    private final int safetyPolicyCount;
    private final int userConstraintCount;
    private final int constraintViolationCount;
    private final int safetyIncidentCount;

    /**
     * Create a new SafetyPerformanceMetrics instance.
     * 
     * @param totalSafetyValidations total number of safety validations performed
     * @param successfulValidations number of successful validations
     * @param failedValidations number of failed validations
     * @param totalConstraintViolations total number of constraint violations
     * @param totalSafetyIncidents total number of safety incidents
     * @param totalSafetyOverrides total number of safety overrides
     * @param totalValidationTimeMs total validation time in milliseconds
     * @param averageValidationTimeMs average validation time in milliseconds
     * @param safetyPolicyCount number of active safety policies
     * @param userConstraintCount number of user-defined constraints
     * @param constraintViolationCount number of constraint violations
     * @param safetyIncidentCount number of safety incidents
     */
    public SafetyPerformanceMetrics(long totalSafetyValidations, long successfulValidations, long failedValidations,
            long totalConstraintViolations, long totalSafetyIncidents, long totalSafetyOverrides,
            long totalValidationTimeMs, double averageValidationTimeMs, int safetyPolicyCount, int userConstraintCount,
            int constraintViolationCount, int safetyIncidentCount) {
        this.totalSafetyValidations = totalSafetyValidations;
        this.successfulValidations = successfulValidations;
        this.failedValidations = failedValidations;
        this.totalConstraintViolations = totalConstraintViolations;
        this.totalSafetyIncidents = totalSafetyIncidents;
        this.totalSafetyOverrides = totalSafetyOverrides;
        this.totalValidationTimeMs = totalValidationTimeMs;
        this.averageValidationTimeMs = averageValidationTimeMs;
        this.safetyPolicyCount = safetyPolicyCount;
        this.userConstraintCount = userConstraintCount;
        this.constraintViolationCount = constraintViolationCount;
        this.safetyIncidentCount = safetyIncidentCount;
    }

    /**
     * Get the total number of safety validations performed.
     * 
     * @return total safety validations
     */
    public long getTotalSafetyValidations() {
        return totalSafetyValidations;
    }

    /**
     * Get the number of successful validations.
     * 
     * @return successful validations
     */
    public long getSuccessfulValidations() {
        return successfulValidations;
    }

    /**
     * Get the number of failed validations.
     * 
     * @return failed validations
     */
    public long getFailedValidations() {
        return failedValidations;
    }

    /**
     * Get the total number of constraint violations.
     * 
     * @return total constraint violations
     */
    public long getTotalConstraintViolations() {
        return totalConstraintViolations;
    }

    /**
     * Get the total number of safety incidents.
     * 
     * @return total safety incidents
     */
    public long getTotalSafetyIncidents() {
        return totalSafetyIncidents;
    }

    /**
     * Get the total number of safety overrides.
     * 
     * @return total safety overrides
     */
    public long getTotalSafetyOverrides() {
        return totalSafetyOverrides;
    }

    /**
     * Get the total validation time in milliseconds.
     * 
     * @return total validation time
     */
    public long getTotalValidationTimeMs() {
        return totalValidationTimeMs;
    }

    /**
     * Get the average validation time in milliseconds.
     * 
     * @return average validation time
     */
    public double getAverageValidationTimeMs() {
        return averageValidationTimeMs;
    }

    /**
     * Get the number of active safety policies.
     * 
     * @return safety policy count
     */
    public int getSafetyPolicyCount() {
        return safetyPolicyCount;
    }

    /**
     * Get the number of user-defined constraints.
     * 
     * @return user constraint count
     */
    public int getUserConstraintCount() {
        return userConstraintCount;
    }

    /**
     * Get the number of constraint violations.
     * 
     * @return constraint violation count
     */
    public int getConstraintViolationCount() {
        return constraintViolationCount;
    }

    /**
     * Get the number of safety incidents.
     * 
     * @return safety incident count
     */
    public int getSafetyIncidentCount() {
        return safetyIncidentCount;
    }

    /**
     * Calculate the validation success rate.
     * 
     * @return success rate as a percentage (0.0 to 1.0)
     */
    public double getSuccessRate() {
        return totalSafetyValidations > 0 ? (double) successfulValidations / totalSafetyValidations : 0.0;
    }

    /**
     * Calculate the failure rate.
     * 
     * @return failure rate as a percentage (0.0 to 1.0)
     */
    public double getFailureRate() {
        return totalSafetyValidations > 0 ? (double) failedValidations / totalSafetyValidations : 0.0;
    }
}
