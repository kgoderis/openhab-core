package org.openhab.core.ai.events;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class PredictiveAnalytics {
    private final double predictedLoad;
    private final double predictedPerformance;
    private final double predictedQuality;
    private final List<PredictionAlert> alerts;
    private final Instant timestamp;

    public PredictiveAnalytics(double predictedLoad, double predictedPerformance, double predictedQuality,
            List<PredictionAlert> alerts, Instant timestamp) {
        this.predictedLoad = predictedLoad;
        this.predictedPerformance = predictedPerformance;
        this.predictedQuality = predictedQuality;
        this.alerts = alerts;
        this.timestamp = timestamp;
    }

    public double getPredictedLoad() { return predictedLoad; }
    public double getPredictedPerformance() { return predictedPerformance; }
    public double getPredictedQuality() { return predictedQuality; }
    public List<PredictionAlert> getAlerts() { return alerts; }
    public Instant getTimestamp() { return timestamp; }
}


