package org.openhab.core.ai.events;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Performance analytics snapshot for the event processing pipeline.
 *
 * Aggregates overall performance score, detailed metrics, detected bottlenecks,
 * and optimization recommendations with a timestamp and totals.
 *
 * Author: Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class PerformanceAnalytics {
    private final double overallPerformance;
    private final List<PerformanceMetric> metrics;
    private final List<PerformanceBottleneck> bottlenecks;
    private final List<OptimizationRecommendation> recommendations;
    private final long totalEventsProcessed;
    private final long totalProcessingTime;
    private final Instant timestamp;

    public PerformanceAnalytics(double overallPerformance, List<PerformanceMetric> metrics,
            List<PerformanceBottleneck> bottlenecks, List<OptimizationRecommendation> recommendations,
            long totalEventsProcessed, long totalProcessingTime, Instant timestamp) {
        this.overallPerformance = overallPerformance;
        this.metrics = metrics;
        this.bottlenecks = bottlenecks;
        this.recommendations = recommendations;
        this.totalEventsProcessed = totalEventsProcessed;
        this.totalProcessingTime = totalProcessingTime;
        this.timestamp = timestamp;
    }

    public double getOverallPerformance() { return overallPerformance; }
    public List<PerformanceMetric> getMetrics() { return metrics; }
    public List<PerformanceBottleneck> getBottlenecks() { return bottlenecks; }
    public List<OptimizationRecommendation> getRecommendations() { return recommendations; }
    public long getTotalEventsProcessed() { return totalEventsProcessed; }
    public long getTotalProcessingTime() { return totalProcessingTime; }
    public Instant getTimestamp() { return timestamp; }
}


