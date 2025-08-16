package org.openhab.core.ai.reasoning.engine;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Performance metrics for the reasoning engine.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class PerformanceMetrics {
    private final long totalReasoningSessions;
    private final long successfulSessions;
    private final long failedSessions;
    private final long totalProcessingTime;
    private final long averageResponseTime;

    public PerformanceMetrics(long totalReasoningSessions, long successfulSessions, long failedSessions,
            long totalProcessingTime, long averageResponseTime) {
        this.totalReasoningSessions = totalReasoningSessions;
        this.successfulSessions = successfulSessions;
        this.failedSessions = failedSessions;
        this.totalProcessingTime = totalProcessingTime;
        this.averageResponseTime = averageResponseTime;
    }

    public long getTotalReasoningSessions() {
        return totalReasoningSessions;
    }

    public long getSuccessfulSessions() {
        return successfulSessions;
    }

    public long getFailedSessions() {
        return failedSessions;
    }

    public long getTotalProcessingTime() {
        return totalProcessingTime;
    }

    public long getAverageResponseTime() {
        return averageResponseTime;
    }
}
