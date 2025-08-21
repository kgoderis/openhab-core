package org.openhab.core.ai.reasoning.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Consolidated safety performance metrics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive performance metrics for safety validation operations including
 * validation counts, success rates, constraint violations, safety incidents, and latency metrics.
 * It implements CountsMetrics and LatencyMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SafetyPerformanceMetrics extends AbstractMetrics implements CountsMetrics, LatencyMetrics {

    private final long totalConstraintViolations;
    private final long totalSafetyIncidents;
    private final long totalSafetyOverrides;
    private final int safetyPolicyCount;
    private final int userConstraintCount;
    private final int constraintViolationCount;
    private final int safetyIncidentCount;

    /**
     * Create a new SafetyPerformanceMetrics instance.
     * 
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param totalOperations total number of safety validations performed
     * @param successfulOperations number of successful validations
     * @param failedOperations number of failed validations
     * @param totalProcessingTime total validation time in nanoseconds
     * @param averageResponseTime average validation time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param totalConstraintViolations total number of constraint violations
     * @param totalSafetyIncidents total number of safety incidents
     * @param totalSafetyOverrides total number of safety overrides
     * @param safetyPolicyCount number of active safety policies
     * @param userConstraintCount number of user-defined constraints
     * @param constraintViolationCount number of constraint violations
     * @param safetyIncidentCount number of safety incidents
     * @param data additional monitoring data
     */
    public SafetyPerformanceMetrics(String id, Instant timestamp, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, long totalConstraintViolations, long totalSafetyIncidents,
            long totalSafetyOverrides, int safetyPolicyCount, int userConstraintCount, int constraintViolationCount,
            int safetyIncidentCount, @Nullable Map<String, Object> data) {
        super(id, timestamp, "reasoning", "safety-performance", "Safety performance metrics", data, totalOperations,
                successfulOperations, failedOperations, totalProcessingTime, averageResponseTime, lastOperationTime);
        this.totalConstraintViolations = totalConstraintViolations;
        this.totalSafetyIncidents = totalSafetyIncidents;
        this.totalSafetyOverrides = totalSafetyOverrides;
        this.safetyPolicyCount = safetyPolicyCount;
        this.userConstraintCount = userConstraintCount;
        this.constraintViolationCount = constraintViolationCount;
        this.safetyIncidentCount = safetyIncidentCount;
    }

    /**
     * Create a new SafetyPerformanceMetrics instance with current timestamp.
     * 
     * @param id unique identifier for this metrics instance
     * @param totalOperations total number of safety validations performed
     * @param successfulOperations number of successful validations
     * @param failedOperations number of failed validations
     * @param totalProcessingTime total validation time in nanoseconds
     * @param averageResponseTime average validation time in milliseconds
     * @param totalConstraintViolations total number of constraint violations
     * @param totalSafetyIncidents total number of safety incidents
     * @param totalSafetyOverrides total number of safety overrides
     * @param safetyPolicyCount number of active safety policies
     * @param userConstraintCount number of user-defined constraints
     * @param constraintViolationCount number of constraint violations
     * @param safetyIncidentCount number of safety incidents
     */
    public SafetyPerformanceMetrics(String id, long totalOperations, long successfulOperations, long failedOperations,
            long totalProcessingTime, double averageResponseTime, long totalConstraintViolations,
            long totalSafetyIncidents, long totalSafetyOverrides, int safetyPolicyCount, int userConstraintCount,
            int constraintViolationCount, int safetyIncidentCount) {
        this(id, Instant.now(), totalOperations, successfulOperations, failedOperations, totalProcessingTime,
                averageResponseTime, null, totalConstraintViolations, totalSafetyIncidents, totalSafetyOverrides,
                safetyPolicyCount, userConstraintCount, constraintViolationCount, safetyIncidentCount, null);
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

    // CountsMetrics interface implementation
    @Override
    public long total() {
        return getTotalOperations();
    }

    @Override
    public long success() {
        return getSuccessfulOperations();
    }

    @Override
    public long failure() {
        return getFailedOperations();
    }

    // LatencyMetrics interface implementation
    @Override
    public long totalDurationNanos() {
        return getTotalProcessingTime();
    }

    /**
     * Get the safety validation success rate.
     * 
     * @return safety validation success rate as a percentage
     */
    public double getSafetyValidationSuccessRate() {
        return successRate() * 100.0;
    }

    /**
     * Get the safety efficiency score.
     * 
     * @return safety efficiency score between 0.0 and 1.0
     */
    public double getSafetyEfficiency() {
        double successRate = successRate();
        double latencyScore = getAverageResponseTime() < 500 ? 1.0
                : getAverageResponseTime() < 1000 ? 0.8 : getAverageResponseTime() < 2000 ? 0.6 : 0.4;
        double violationPenalty = totalConstraintViolations > 0
                ? Math.max(0.0, 1.0 - (double) totalConstraintViolations / total())
                : 1.0;
        double incidentPenalty = totalSafetyIncidents > 0 ? Math.max(0.0, 1.0 - (double) totalSafetyIncidents / total())
                : 1.0;

        return (successRate * 0.4) + (latencyScore * 0.2) + (violationPenalty * 0.2) + (incidentPenalty * 0.2);
    }

    /**
     * Check if safety validation is performing well (success rate > 90% and low violations).
     * 
     * @return true if safety validation is performing well
     */
    public boolean isPerformingWell() {
        return successRate() > 0.9 && totalConstraintViolations == 0 && totalSafetyIncidents == 0;
    }

    /**
     * Check if there are critical safety issues (high violation rate or incidents).
     * 
     * @return true if there are critical safety issues
     */
    public boolean hasCriticalIssues() {
        return successRate() < 0.8 || totalSafetyIncidents > 0;
    }
}
