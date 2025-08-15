package org.openhab.core.ai.reasoning.metrics;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Aggregated safety performance metrics.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SafetyPerformanceMetrics {
    private final long totalSafetyValidations;
    private final long totalConstraintViolations;
    private final long totalSafetyIncidents;
    private final long totalSafetyOverrides;
    private final int safetyPolicyCount;
    private final int userConstraintCount;
    private final int constraintViolationCount;
    private final int safetyIncidentCount;

    public SafetyPerformanceMetrics(long totalSafetyValidations, long totalConstraintViolations,
            long totalSafetyIncidents, long totalSafetyOverrides, int safetyPolicyCount, int userConstraintCount,
            int constraintViolationCount, int safetyIncidentCount) {
        this.totalSafetyValidations = totalSafetyValidations;
        this.totalConstraintViolations = totalConstraintViolations;
        this.totalSafetyIncidents = totalSafetyIncidents;
        this.totalSafetyOverrides = totalSafetyOverrides;
        this.safetyPolicyCount = safetyPolicyCount;
        this.userConstraintCount = userConstraintCount;
        this.constraintViolationCount = constraintViolationCount;
        this.safetyIncidentCount = safetyIncidentCount;
    }

    public long getTotalSafetyValidations() {
        return totalSafetyValidations;
    }

    public long getTotalConstraintViolations() {
        return totalConstraintViolations;
    }

    public long getTotalSafetyIncidents() {
        return totalSafetyIncidents;
    }

    public long getTotalSafetyOverrides() {
        return totalSafetyOverrides;
    }

    public int getSafetyPolicyCount() {
        return safetyPolicyCount;
    }

    public int getUserConstraintCount() {
        return userConstraintCount;
    }

    public int getConstraintViolationCount() {
        return constraintViolationCount;
    }

    public int getSafetyIncidentCount() {
        return safetyIncidentCount;
    }
}
