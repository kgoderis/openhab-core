package org.openhab.core.ai.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ResourceOptimizationRecommendation {
    private final String resource;
    private final String operation;
    private final String recommendation;
    private final double priority;

    public ResourceOptimizationRecommendation(String resource, String operation, String recommendation,
            double priority) {
        this.resource = resource;
        this.operation = operation;
        this.recommendation = recommendation;
        this.priority = priority;
    }

    public String getResource() {
        return resource;
    }

    public String getOperation() {
        return operation;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public double getPriority() {
        return priority;
    }
}
