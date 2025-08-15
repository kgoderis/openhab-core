package org.openhab.core.ai.events;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ResourceAnalytics {
    private final double overallUtilization;
    private final List<ResourceMetric> metrics;
    private final List<ResourceIssue> issues;
    private final List<ResourceOptimizationRecommendation> recommendations;
    private final Instant timestamp;

    public ResourceAnalytics(double overallUtilization, List<ResourceMetric> metrics, List<ResourceIssue> issues,
            List<ResourceOptimizationRecommendation> recommendations, Instant timestamp) {
        this.overallUtilization = overallUtilization;
        this.metrics = metrics;
        this.issues = issues;
        this.recommendations = recommendations;
        this.timestamp = timestamp;
    }

    public double getOverallUtilization() {
        return overallUtilization;
    }

    public List<ResourceMetric> getMetrics() {
        return metrics;
    }

    public List<ResourceIssue> getIssues() {
        return issues;
    }

    public List<ResourceOptimizationRecommendation> getRecommendations() {
        return recommendations;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
