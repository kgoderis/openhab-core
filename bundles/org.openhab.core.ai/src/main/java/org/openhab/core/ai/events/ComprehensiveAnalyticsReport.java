package org.openhab.core.ai.events;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class ComprehensiveAnalyticsReport {
    private final double systemHealthScore;
    private final @Nullable PerformanceAnalytics performanceAnalytics;
    private final @Nullable QualityAnalytics qualityAnalytics;
    private final @Nullable ResourceAnalytics resourceAnalytics;
    private final @Nullable PredictiveAnalytics predictiveAnalytics;
    private final List<AnalyticsEvent> events;
    private final Instant timestamp;

    public ComprehensiveAnalyticsReport(double systemHealthScore, @Nullable PerformanceAnalytics performanceAnalytics,
            @Nullable QualityAnalytics qualityAnalytics, @Nullable ResourceAnalytics resourceAnalytics,
            @Nullable PredictiveAnalytics predictiveAnalytics, List<AnalyticsEvent> events, Instant timestamp) {
        this.systemHealthScore = systemHealthScore;
        this.performanceAnalytics = performanceAnalytics;
        this.qualityAnalytics = qualityAnalytics;
        this.resourceAnalytics = resourceAnalytics;
        this.predictiveAnalytics = predictiveAnalytics;
        this.events = events;
        this.timestamp = timestamp;
    }

    public double getSystemHealthScore() {
        return systemHealthScore;
    }

    public @Nullable PerformanceAnalytics getPerformanceAnalytics() {
        return performanceAnalytics;
    }

    public @Nullable QualityAnalytics getQualityAnalytics() {
        return qualityAnalytics;
    }

    public @Nullable ResourceAnalytics getResourceAnalytics() {
        return resourceAnalytics;
    }

    public @Nullable PredictiveAnalytics getPredictiveAnalytics() {
        return predictiveAnalytics;
    }

    public List<AnalyticsEvent> getEvents() {
        return events;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
