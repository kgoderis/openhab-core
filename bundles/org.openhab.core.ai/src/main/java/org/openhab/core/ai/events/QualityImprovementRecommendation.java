package org.openhab.core.ai.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class QualityImprovementRecommendation {
    private final String component;
    private final String operation;
    private final String recommendation;
    private final double priority;

    public QualityImprovementRecommendation(String component, String operation, String recommendation, double priority) {
        this.component = component;
        this.operation = operation;
        this.recommendation = recommendation;
        this.priority = priority;
    }

    public String getComponent() { return component; }
    public String getOperation() { return operation; }
    public String getRecommendation() { return recommendation; }
    public double getPriority() { return priority; }
}
