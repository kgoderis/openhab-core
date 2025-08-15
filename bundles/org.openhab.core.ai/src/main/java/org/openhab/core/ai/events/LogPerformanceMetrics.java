package org.openhab.core.ai.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Performance metrics for the log ingestion pipeline.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class LogPerformanceMetrics {
    private final long totalLogLinesProcessed;
    private final long totalAnomaliesDetected;
    private final long totalCorrelationsFound;
    private final long totalProcessingTime;
    private final int recentLogsCount;
    private final int anomaliesCount;
    private final int correlationsCount;
    private final int activeMonitorsCount;

    public LogPerformanceMetrics(long totalLogLinesProcessed, long totalAnomaliesDetected, long totalCorrelationsFound,
            long totalProcessingTime, int recentLogsCount, int anomaliesCount, int correlationsCount,
            int activeMonitorsCount) {
        this.totalLogLinesProcessed = totalLogLinesProcessed;
        this.totalAnomaliesDetected = totalAnomaliesDetected;
        this.totalCorrelationsFound = totalCorrelationsFound;
        this.totalProcessingTime = totalProcessingTime;
        this.recentLogsCount = recentLogsCount;
        this.anomaliesCount = anomaliesCount;
        this.correlationsCount = correlationsCount;
        this.activeMonitorsCount = activeMonitorsCount;
    }

    public long getTotalLogLinesProcessed() {
        return totalLogLinesProcessed;
    }

    public long getTotalAnomaliesDetected() {
        return totalAnomaliesDetected;
    }

    public long getTotalCorrelationsFound() {
        return totalCorrelationsFound;
    }

    public long getTotalProcessingTime() {
        return totalProcessingTime;
    }

    public int getRecentLogsCount() {
        return recentLogsCount;
    }

    public int getAnomaliesCount() {
        return anomaliesCount;
    }

    public int getCorrelationsCount() {
        return correlationsCount;
    }

    public int getActiveMonitorsCount() {
        return activeMonitorsCount;
    }
}
