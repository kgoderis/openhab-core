package org.openhab.core.ai.reasoning.metrics;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.metrics.ReasoningPerformanceMetrics;

/**
 * Builder for performance metrics in the reasoning system.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */

@NonNullByDefault
public class PerformanceMetricsBuilder {
    private long totalSessions;
    private long successfulSessions;
    private long failedSessions;
    private long totalSteps;
    private long totalActions;
    private double averageSessionDuration;
    private long totalProcessingTime;
    private long averageResponseTime;

    public PerformanceMetricsBuilder totalSessions(long v) {
        this.totalSessions = v;
        return this;
    }

    public PerformanceMetricsBuilder successfulSessions(long v) {
        this.successfulSessions = v;
        return this;
    }

    public PerformanceMetricsBuilder failedSessions(long v) {
        this.failedSessions = v;
        return this;
    }

    public PerformanceMetricsBuilder totalSteps(long v) {
        this.totalSteps = v;
        return this;
    }

    public PerformanceMetricsBuilder totalActions(long v) {
        this.totalActions = v;
        return this;
    }

    public PerformanceMetricsBuilder averageSessionDuration(double v) {
        this.averageSessionDuration = v;
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
        return new ReasoningPerformanceMetrics(totalSessions, successfulSessions, failedSessions, totalSteps,
                totalActions, averageSessionDuration, totalProcessingTime, averageResponseTime, null);
    }
}
