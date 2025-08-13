package org.openhab.core.ai.events;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class QualityAnalytics {
    private final double overallQuality;
    private final List<QualityMetric> metrics;
    private final List<QualityIssue> issues;
    private final List<QualityImprovementRecommendation> recommendations;
    private final long totalErrors;
    private final long totalWarnings;
    private final Instant timestamp;

    public QualityAnalytics(double overallQuality, List<QualityMetric> metrics, List<QualityIssue> issues,
            List<QualityImprovementRecommendation> recommendations, long totalErrors, long totalWarnings,
            Instant timestamp) {
        this.overallQuality = overallQuality;
        this.metrics = metrics;
        this.issues = issues;
        this.recommendations = recommendations;
        this.totalErrors = totalErrors;
        this.totalWarnings = totalWarnings;
        this.timestamp = timestamp;
    }

    public double getOverallQuality() { return overallQuality; }
    public List<QualityMetric> getMetrics() { return metrics; }
    public List<QualityIssue> getIssues() { return issues; }
    public List<QualityImprovementRecommendation> getRecommendations() { return recommendations; }
    public long getTotalErrors() { return totalErrors; }
    public long getTotalWarnings() { return totalWarnings; }
    public Instant getTimestamp() { return timestamp; }
}


