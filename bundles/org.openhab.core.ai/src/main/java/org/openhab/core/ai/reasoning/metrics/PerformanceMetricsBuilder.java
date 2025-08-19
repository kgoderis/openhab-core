package org.openhab.core.ai.reasoning.metrics;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.metrics.ReasoningPerformanceMetrics;

@NonNullByDefault
public class PerformanceMetricsBuilder {
    long totalSessions;
    long successfulSessions;
    long failedSessions;
    long totalSteps;
    long totalActions;
    double averageSessionDuration;

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

    public ReasoningPerformanceMetrics build() {
        return new ReasoningPerformanceMetrics(totalSessions, successfulSessions, failedSessions, totalSteps,
                totalActions, averageSessionDuration, 0, 0.0, null);
    }
}
