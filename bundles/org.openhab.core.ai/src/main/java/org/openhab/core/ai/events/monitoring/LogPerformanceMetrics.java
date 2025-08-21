package org.openhab.core.ai.events.monitoring;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Consolidated log performance metrics that extends the unified monitoring framework.
 *
 * <p>
 * This class provides comprehensive performance metrics for log ingestion pipeline operations
 * including log processing, anomaly detection, and correlation analysis.
 * It implements CountsMetrics and LatencyMetrics capability interfaces.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class LogPerformanceMetrics extends AbstractMetrics implements CountsMetrics, LatencyMetrics {

    private final long totalLogLinesProcessed;
    private final long totalAnomaliesDetected;
    private final long totalCorrelationsFound;
    private final int recentLogsCount;
    private final int anomaliesCount;
    private final int correlationsCount;
    private final int activeMonitorsCount;

    /**
     * Create a new LogPerformanceMetrics instance.
     *
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param totalOperations total number of log processing operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param totalLogLinesProcessed total number of log lines processed
     * @param totalAnomaliesDetected total number of anomalies detected
     * @param totalCorrelationsFound total number of correlations found
     * @param recentLogsCount current recent logs count
     * @param anomaliesCount current anomalies count
     * @param correlationsCount current correlations count
     * @param activeMonitorsCount current active monitors count
     * @param data additional monitoring data
     */
    public LogPerformanceMetrics(String id, Instant timestamp, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, long totalLogLinesProcessed, long totalAnomaliesDetected,
            long totalCorrelationsFound, int recentLogsCount, int anomaliesCount, int correlationsCount,
            int activeMonitorsCount, @Nullable Map<String, Object> data) {
        super(id, timestamp, "events", "log-performance", "Log performance metrics", data, totalOperations,
                successfulOperations, failedOperations, totalProcessingTime, averageResponseTime, lastOperationTime);
        this.totalLogLinesProcessed = totalLogLinesProcessed;
        this.totalAnomaliesDetected = totalAnomaliesDetected;
        this.totalCorrelationsFound = totalCorrelationsFound;
        this.recentLogsCount = recentLogsCount;
        this.anomaliesCount = anomaliesCount;
        this.correlationsCount = correlationsCount;
        this.activeMonitorsCount = activeMonitorsCount;
    }

    /**
     * Create a new LogPerformanceMetrics instance with current timestamp.
     *
     * @param id unique identifier for this metrics instance
     * @param totalOperations total number of log processing operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param totalLogLinesProcessed total number of log lines processed
     * @param totalAnomaliesDetected total number of anomalies detected
     * @param totalCorrelationsFound total number of correlations found
     * @param recentLogsCount current recent logs count
     * @param anomaliesCount current anomalies count
     * @param correlationsCount current correlations count
     * @param activeMonitorsCount current active monitors count
     */
    public LogPerformanceMetrics(String id, long totalOperations, long successfulOperations, long failedOperations,
            long totalProcessingTime, double averageResponseTime, long totalLogLinesProcessed,
            long totalAnomaliesDetected, long totalCorrelationsFound, int recentLogsCount, int anomaliesCount,
            int correlationsCount, int activeMonitorsCount) {
        this(id, Instant.now(), totalOperations, successfulOperations, failedOperations, totalProcessingTime,
                averageResponseTime, null, totalLogLinesProcessed, totalAnomaliesDetected, totalCorrelationsFound,
                recentLogsCount, anomaliesCount, correlationsCount, activeMonitorsCount, null);
    }

    /**
     * Get the total number of log lines processed.
     *
     * @return total log lines processed
     */
    public long getTotalLogLinesProcessed() {
        return totalLogLinesProcessed;
    }

    /**
     * Get the total number of anomalies detected.
     *
     * @return total anomalies detected
     */
    public long getTotalAnomaliesDetected() {
        return totalAnomaliesDetected;
    }

    /**
     * Get the total number of correlations found.
     *
     * @return total correlations found
     */
    public long getTotalCorrelationsFound() {
        return totalCorrelationsFound;
    }

    /**
     * Get the current recent logs count.
     *
     * @return recent logs count
     */
    public int getRecentLogsCount() {
        return recentLogsCount;
    }

    /**
     * Get the current anomalies count.
     *
     * @return anomalies count
     */
    public int getAnomaliesCount() {
        return anomaliesCount;
    }

    /**
     * Get the current correlations count.
     *
     * @return correlations count
     */
    public int getCorrelationsCount() {
        return correlationsCount;
    }

    /**
     * Get the current active monitors count.
     *
     * @return active monitors count
     */
    public int getActiveMonitorsCount() {
        return activeMonitorsCount;
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
     * Get the anomaly detection rate as a percentage.
     *
     * @return anomaly detection rate percentage
     */
    public double getAnomalyDetectionRate() {
        return totalLogLinesProcessed > 0 ? (double) totalAnomaliesDetected / totalLogLinesProcessed : 0.0;
    }

    /**
     * Get the correlation detection rate as a percentage.
     *
     * @return correlation detection rate percentage
     */
    public double getCorrelationDetectionRate() {
        return totalLogLinesProcessed > 0 ? (double) totalCorrelationsFound / totalLogLinesProcessed : 0.0;
    }

    /**
     * Get the log processing efficiency score.
     *
     * @return log processing efficiency score between 0.0 and 1.0
     */
    public double getLogProcessingEfficiency() {
        double successRate = successRate();
        double anomalyEfficiency = getAnomalyDetectionRate() > 0.01 ? 1.0
                : getAnomalyDetectionRate() > 0.005 ? 0.8 : getAnomalyDetectionRate() > 0.001 ? 0.6 : 0.4;
        double correlationEfficiency = getCorrelationDetectionRate() > 0.05 ? 1.0
                : getCorrelationDetectionRate() > 0.02 ? 0.8 : getCorrelationDetectionRate() > 0.01 ? 0.6 : 0.4;
        double latencyScore = getAverageResponseTime() < 100 ? 1.0
                : getAverageResponseTime() < 500 ? 0.8 : getAverageResponseTime() < 1000 ? 0.6 : 0.4;

        return (successRate * 0.4) + (anomalyEfficiency * 0.3) + (correlationEfficiency * 0.2) + (latencyScore * 0.1);
    }

    /**
     * Check if log processing is performing well (high success rate, good anomaly detection).
     *
     * @return true if log processing is performing well
     */
    public boolean isPerformingWell() {
        return successRate() > 0.9 && getAnomalyDetectionRate() > 0.005 && getAverageResponseTime() < 500;
    }

    /**
     * Check if there are critical log processing issues (low success rate or poor anomaly detection).
     *
     * @return true if there are critical log processing issues
     */
    public boolean hasCriticalIssues() {
        return successRate() < 0.8 || getAnomalyDetectionRate() < 0.001 || getAverageResponseTime() > 2000;
    }
}
