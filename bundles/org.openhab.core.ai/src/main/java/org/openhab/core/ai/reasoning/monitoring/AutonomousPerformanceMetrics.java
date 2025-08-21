package org.openhab.core.ai.reasoning.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Consolidated autonomous performance metrics that extends the unified monitoring framework.
 * 
 * <p>
 * This class provides comprehensive performance metrics for autonomous reasoning operations including
 * event processing, autonomous actions, pattern detection, safety violations, and user overrides.
 * It implements CountsMetrics and LatencyMetrics capability interfaces.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AutonomousPerformanceMetrics extends AbstractMetrics implements CountsMetrics, LatencyMetrics {

    private final long totalAutonomousActions;
    private final long totalPatternDetections;
    private final long totalSafetyViolations;
    private final long totalUserOverrides;
    private final int pendingActionCount;
    private final int patternCount;
    private final int preferenceCount;
    private final int constraintCount;

    /**
     * Create a new AutonomousPerformanceMetrics instance.
     * 
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param totalOperations total number of events processed
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param totalAutonomousActions total number of autonomous actions taken
     * @param totalPatternDetections total number of pattern detections
     * @param totalSafetyViolations total number of safety violations
     * @param totalUserOverrides total number of user overrides
     * @param pendingActionCount number of pending actions
     * @param patternCount number of patterns
     * @param preferenceCount number of preferences
     * @param constraintCount number of constraints
     * @param data additional monitoring data
     */
    public AutonomousPerformanceMetrics(String id, Instant timestamp, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, long totalAutonomousActions, long totalPatternDetections,
            long totalSafetyViolations, long totalUserOverrides, int pendingActionCount, int patternCount,
            int preferenceCount, int constraintCount, @Nullable Map<String, Object> data) {
        super(id, timestamp, "reasoning", "autonomous-performance", "Autonomous performance metrics", data,
                totalOperations, successfulOperations, failedOperations, totalProcessingTime, averageResponseTime,
                lastOperationTime);
        this.totalAutonomousActions = totalAutonomousActions;
        this.totalPatternDetections = totalPatternDetections;
        this.totalSafetyViolations = totalSafetyViolations;
        this.totalUserOverrides = totalUserOverrides;
        this.pendingActionCount = pendingActionCount;
        this.patternCount = patternCount;
        this.preferenceCount = preferenceCount;
        this.constraintCount = constraintCount;
    }

    /**
     * Create a new AutonomousPerformanceMetrics instance with current timestamp.
     * 
     * @param id unique identifier for this metrics instance
     * @param totalOperations total number of events processed
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param totalAutonomousActions total number of autonomous actions taken
     * @param totalPatternDetections total number of pattern detections
     * @param totalSafetyViolations total number of safety violations
     * @param totalUserOverrides total number of user overrides
     * @param pendingActionCount number of pending actions
     * @param patternCount number of patterns
     * @param preferenceCount number of preferences
     * @param constraintCount number of constraints
     */
    public AutonomousPerformanceMetrics(String id, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime, long totalAutonomousActions,
            long totalPatternDetections, long totalSafetyViolations, long totalUserOverrides, int pendingActionCount,
            int patternCount, int preferenceCount, int constraintCount) {
        this(id, Instant.now(), totalOperations, successfulOperations, failedOperations, totalProcessingTime,
                averageResponseTime, null, totalAutonomousActions, totalPatternDetections, totalSafetyViolations,
                totalUserOverrides, pendingActionCount, patternCount, preferenceCount, constraintCount, null);
    }

    /**
     * Get the total number of autonomous actions taken.
     * 
     * @return total autonomous actions
     */
    public long getTotalAutonomousActions() {
        return totalAutonomousActions;
    }

    /**
     * Get the total number of pattern detections.
     * 
     * @return total pattern detections
     */
    public long getTotalPatternDetections() {
        return totalPatternDetections;
    }

    /**
     * Get the total number of safety violations.
     * 
     * @return total safety violations
     */
    public long getTotalSafetyViolations() {
        return totalSafetyViolations;
    }

    /**
     * Get the total number of user overrides.
     * 
     * @return total user overrides
     */
    public long getTotalUserOverrides() {
        return totalUserOverrides;
    }

    /**
     * Get the number of pending actions.
     * 
     * @return pending action count
     */
    public int getPendingActionCount() {
        return pendingActionCount;
    }

    /**
     * Get the number of patterns.
     * 
     * @return pattern count
     */
    public int getPatternCount() {
        return patternCount;
    }

    /**
     * Get the number of preferences.
     * 
     * @return preference count
     */
    public int getPreferenceCount() {
        return preferenceCount;
    }

    /**
     * Get the number of constraints.
     * 
     * @return constraint count
     */
    public int getConstraintCount() {
        return constraintCount;
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
     * Get the autonomous action rate.
     * 
     * @return autonomous action rate as a percentage
     */
    public double getAutonomousActionRate() {
        return total() > 0 ? (double) totalAutonomousActions / total() * 100.0 : 0.0;
    }

    /**
     * Get the pattern detection rate.
     * 
     * @return pattern detection rate as a percentage
     */
    public double getPatternDetectionRate() {
        return total() > 0 ? (double) totalPatternDetections / total() * 100.0 : 0.0;
    }

    /**
     * Get the safety violation rate.
     * 
     * @return safety violation rate as a percentage
     */
    public double getSafetyViolationRate() {
        return total() > 0 ? (double) totalSafetyViolations / total() * 100.0 : 0.0;
    }

    /**
     * Get the user override rate.
     * 
     * @return user override rate as a percentage
     */
    public double getUserOverrideRate() {
        return total() > 0 ? (double) totalUserOverrides / total() * 100.0 : 0.0;
    }

    /**
     * Get the autonomous efficiency score.
     * 
     * @return autonomous efficiency score between 0.0 and 1.0
     */
    public double getAutonomousEfficiency() {
        double successRate = successRate();
        double actionRate = total() > 0 ? (double) totalAutonomousActions / total() : 0.0;
        double patternRate = total() > 0 ? (double) totalPatternDetections / total() : 0.0;
        double safetyPenalty = totalSafetyViolations > 0 ? Math.max(0.0, 1.0 - (double) totalSafetyViolations / total())
                : 1.0;
        double overridePenalty = totalUserOverrides > 0 ? Math.max(0.0, 1.0 - (double) totalUserOverrides / total())
                : 1.0;
        double latencyScore = getAverageResponseTime() < 1000 ? 1.0
                : getAverageResponseTime() < 3000 ? 0.8 : getAverageResponseTime() < 5000 ? 0.6 : 0.4;

        return (successRate * 0.3) + (actionRate * 0.2) + (patternRate * 0.15) + (safetyPenalty * 0.15)
                + (overridePenalty * 0.1) + (latencyScore * 0.1);
    }

    /**
     * Check if autonomous reasoning is performing well (high success rate, low violations).
     * 
     * @return true if autonomous reasoning is performing well
     */
    public boolean isPerformingWell() {
        return successRate() > 0.8 && getSafetyViolationRate() < 5.0 && getUserOverrideRate() < 10.0;
    }

    /**
     * Check if there are critical autonomous issues (high violation rate or overrides).
     * 
     * @return true if there are critical autonomous issues
     */
    public boolean hasCriticalIssues() {
        return successRate() < 0.7 || getSafetyViolationRate() > 10.0 || getUserOverrideRate() > 20.0;
    }

    /**
     * Check if the system is operating autonomously (high action rate, low overrides).
     * 
     * @return true if the system is operating autonomously
     */
    public boolean isOperatingAutonomously() {
        return getAutonomousActionRate() > 70.0 && getUserOverrideRate() < 15.0;
    }
}
