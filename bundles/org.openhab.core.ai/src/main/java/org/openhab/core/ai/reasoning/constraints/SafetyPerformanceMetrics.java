package org.openhab.core.ai.reasoning.constraints;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Performance metrics for safety constraint validation.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SafetyPerformanceMetrics {
    private final long totalValidations;
    private final long successfulValidations;
    private final long failedValidations;
    private final long totalValidationTimeMs;
    private final double averageValidationTimeMs;

    public SafetyPerformanceMetrics(long totalValidations, long successfulValidations, long failedValidations,
            long totalValidationTimeMs, double averageValidationTimeMs) {
        this.totalValidations = totalValidations;
        this.successfulValidations = successfulValidations;
        this.failedValidations = failedValidations;
        this.totalValidationTimeMs = totalValidationTimeMs;
        this.averageValidationTimeMs = averageValidationTimeMs;
    }

    public long getTotalValidations() {
        return totalValidations;
    }

    public long getSuccessfulValidations() {
        return successfulValidations;
    }

    public long getFailedValidations() {
        return failedValidations;
    }

    public long getTotalValidationTimeMs() {
        return totalValidationTimeMs;
    }

    public double getAverageValidationTimeMs() {
        return averageValidationTimeMs;
    }

    public double getSuccessRate() {
        return totalValidations > 0 ? (double) successfulValidations / totalValidations : 0.0;
    }
}
