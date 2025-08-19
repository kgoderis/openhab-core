package org.openhab.core.ai.reasoning.engine;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.metrics.ReasoningPerformanceMetrics;

/**
 * Builder for performance metrics in the reasoning engine.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class PerformanceMetricsBuilder {
    private long totalReasoningSessions = 0;
    private long successfulSessions = 0;
    private long failedSessions = 0;
    private long totalProcessingTime = 0;
    private long averageResponseTime = 0;

    public PerformanceMetricsBuilder totalReasoningSessions(long sessions) {
        this.totalReasoningSessions = sessions;
        return this;
    }

    public PerformanceMetricsBuilder successfulSessions(long sessions) {
        this.successfulSessions = sessions;
        return this;
    }

    public PerformanceMetricsBuilder failedSessions(long sessions) {
        this.failedSessions = sessions;
        return this;
    }

    public PerformanceMetricsBuilder totalProcessingTime(long time) {
        this.totalProcessingTime = time;
        return this;
    }

    public PerformanceMetricsBuilder averageResponseTime(long time) {
        this.averageResponseTime = time;
        return this;
    }

    public ReasoningPerformanceMetrics build() {
        return new ReasoningPerformanceMetrics(totalReasoningSessions, successfulSessions, failedSessions, 0, 0, 0.0,
                totalProcessingTime, averageResponseTime, null);
    }
}
