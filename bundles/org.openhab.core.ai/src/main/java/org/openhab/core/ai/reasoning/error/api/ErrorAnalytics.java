package org.openhab.core.ai.reasoning.error.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Aggregated error analytics for reasoning components.
 *
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ErrorAnalytics {
    private final long totalErrors;
    private final long criticalErrors;
    private final long recoverableErrors;
    private final double averageRecoveryTime;
    private final String[] topErrorTypes;

    public ErrorAnalytics(long totalErrors, long criticalErrors, long recoverableErrors, double averageRecoveryTime,
            String[] topErrorTypes) {
        this.totalErrors = totalErrors;
        this.criticalErrors = criticalErrors;
        this.recoverableErrors = recoverableErrors;
        this.averageRecoveryTime = averageRecoveryTime;
        this.topErrorTypes = topErrorTypes;
    }

    public long getTotalErrors() {
        return totalErrors;
    }

    public long getCriticalErrors() {
        return criticalErrors;
    }

    public long getRecoverableErrors() {
        return recoverableErrors;
    }

    public double getAverageRecoveryTime() {
        return averageRecoveryTime;
    }

    public String[] getTopErrorTypes() {
        return topErrorTypes;
    }
}
