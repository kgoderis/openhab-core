package org.openhab.core.ai.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class QualityIssue {
    private final String component;
    private final String operation;
    private final double quality;
    private final String details;
    private final String recommendation;

    public QualityIssue(String component, String operation, double quality, String details, String recommendation) {
        this.component = component;
        this.operation = operation;
        this.quality = quality;
        this.details = details;
        this.recommendation = recommendation;
    }

    public String getComponent() {
        return component;
    }

    public String getOperation() {
        return operation;
    }

    public double getQuality() {
        return quality;
    }

    public String getDetails() {
        return details;
    }

    public String getRecommendation() {
        return recommendation;
    }
}
