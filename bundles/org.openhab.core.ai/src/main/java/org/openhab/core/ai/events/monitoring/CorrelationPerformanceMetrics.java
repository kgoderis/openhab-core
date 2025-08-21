package org.openhab.core.ai.events.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Consolidated correlation performance metrics that extends the unified monitoring framework.
 *
 * <p>
 * This class provides comprehensive performance metrics for event-log correlation operations
 * including correlation creation, validation, and pattern analysis.
 * It implements CountsMetrics and LatencyMetrics capability interfaces.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CorrelationPerformanceMetrics extends AbstractMetrics implements CountsMetrics, LatencyMetrics {

    private final long totalCorrelationsCreated;
    private final long totalCorrelationsValidated;
    private final int correlationCount;
    private final int eventCorrelationCount;
    private final int logCorrelationCount;
    private final int patternCount;

    /**
     * Create a new CorrelationPerformanceMetrics instance.
     *
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param totalOperations total number of correlation operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param totalCorrelationsCreated total number of correlations created
     * @param totalCorrelationsValidated total number of correlations validated
     * @param correlationCount current correlation count
     * @param eventCorrelationCount current event correlation count
     * @param logCorrelationCount current log correlation count
     * @param patternCount current pattern count
     * @param data additional monitoring data
     */
    public CorrelationPerformanceMetrics(String id, Instant timestamp, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, long totalCorrelationsCreated, long totalCorrelationsValidated,
            int correlationCount, int eventCorrelationCount, int logCorrelationCount, int patternCount,
            @Nullable Map<String, Object> data) {
        super(id, timestamp, "events", "correlation-performance", "Correlation performance metrics", data,
                totalOperations, successfulOperations, failedOperations, totalProcessingTime, averageResponseTime,
                lastOperationTime);
        this.totalCorrelationsCreated = totalCorrelationsCreated;
        this.totalCorrelationsValidated = totalCorrelationsValidated;
        this.correlationCount = correlationCount;
        this.eventCorrelationCount = eventCorrelationCount;
        this.logCorrelationCount = logCorrelationCount;
        this.patternCount = patternCount;
    }

    /**
     * Create a new CorrelationPerformanceMetrics instance with current timestamp.
     *
     * @param id unique identifier for this metrics instance
     * @param totalOperations total number of correlation operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param totalCorrelationsCreated total number of correlations created
     * @param totalCorrelationsValidated total number of correlations validated
     * @param correlationCount current correlation count
     * @param eventCorrelationCount current event correlation count
     * @param logCorrelationCount current log correlation count
     * @param patternCount current pattern count
     */
    public CorrelationPerformanceMetrics(String id, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime, long totalCorrelationsCreated,
            long totalCorrelationsValidated, int correlationCount, int eventCorrelationCount, int logCorrelationCount,
            int patternCount) {
        this(id, Instant.now(), totalOperations, successfulOperations, failedOperations, totalProcessingTime,
                averageResponseTime, null, totalCorrelationsCreated, totalCorrelationsValidated, correlationCount,
                eventCorrelationCount, logCorrelationCount, patternCount, null);
    }

    /**
     * Get the total number of correlations created.
     *
     * @return total correlations created
     */
    public long getTotalCorrelationsCreated() {
        return totalCorrelationsCreated;
    }

    /**
     * Get the total number of correlations validated.
     *
     * @return total correlations validated
     */
    public long getTotalCorrelationsValidated() {
        return totalCorrelationsValidated;
    }

    /**
     * Get the current correlation count.
     *
     * @return correlation count
     */
    public int getCorrelationCount() {
        return correlationCount;
    }

    /**
     * Get the current event correlation count.
     *
     * @return event correlation count
     */
    public int getEventCorrelationCount() {
        return eventCorrelationCount;
    }

    /**
     * Get the current log correlation count.
     *
     * @return log correlation count
     */
    public int getLogCorrelationCount() {
        return logCorrelationCount;
    }

    /**
     * Get the current pattern count.
     *
     * @return pattern count
     */
    public int getPatternCount() {
        return patternCount;
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
     * Get the correlation validation rate as a percentage.
     *
     * @return correlation validation rate percentage
     */
    public double getCorrelationValidationRate() {
        return totalCorrelationsCreated > 0 ? (double) totalCorrelationsValidated / totalCorrelationsCreated : 0.0;
    }

    /**
     * Get the correlation efficiency score.
     *
     * @return correlation efficiency score between 0.0 and 1.0
     */
    public double getCorrelationEfficiency() {
        double successRate = successRate();
        double validationRate = getCorrelationValidationRate();
        double latencyScore = getAverageResponseTime() < 1000 ? 1.0
                : getAverageResponseTime() < 3000 ? 0.8 : getAverageResponseTime() < 5000 ? 0.6 : 0.4;
        double patternUtilization = patternCount > 0 ? Math.min(1.0, (double) correlationCount / patternCount) : 0.0;

        return (successRate * 0.4) + (validationRate * 0.3) + (latencyScore * 0.2) + (patternUtilization * 0.1);
    }

    /**
     * Check if correlation performance is performing well (high success rate, good validation).
     *
     * @return true if correlation performance is performing well
     */
    public boolean isPerformingWell() {
        return successRate() > 0.9 && getCorrelationValidationRate() > 0.8 && getAverageResponseTime() < 3000;
    }

    /**
     * Check if there are critical correlation issues (low success rate or poor validation).
     *
     * @return true if there are critical correlation issues
     */
    public boolean hasCriticalIssues() {
        return successRate() < 0.8 || getCorrelationValidationRate() < 0.6 || getAverageResponseTime() > 10000;
    }
}
