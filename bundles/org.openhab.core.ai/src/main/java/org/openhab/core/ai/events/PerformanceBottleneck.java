package org.openhab.core.ai.events;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class PerformanceBottleneck {
    private final String component;
    private final String operation;
    private final Duration averageDuration;
    private final double successRate;
    private final String recommendation;

    public PerformanceBottleneck(String component, String operation, Duration averageDuration, double successRate,
            String recommendation) {
        this.component = component;
        this.operation = operation;
        this.averageDuration = averageDuration;
        this.successRate = successRate;
        this.recommendation = recommendation;
    }

    public String getComponent() {
        return component;
    }

    public String getOperation() {
        return operation;
    }

    public Duration getAverageDuration() {
        return averageDuration;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public String getRecommendation() {
        return recommendation;
    }
}
