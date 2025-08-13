package org.openhab.core.ai.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class OptimizationRecommendation {
    private final String component;
    private final String operation;
    private final OptimizationType type;
    private final String recommendation;
    private final double priority;

    public OptimizationRecommendation(String component, String operation, OptimizationType type, String recommendation,
            double priority) {
        this.component = component;
        this.operation = operation;
        this.type = type;
        this.recommendation = recommendation;
        this.priority = priority;
    }

    public String getComponent() { return component; }
    public String getOperation() { return operation; }
    public OptimizationType getType() { return type; }
    public String getRecommendation() { return recommendation; }
    public double getPriority() { return priority; }
}


