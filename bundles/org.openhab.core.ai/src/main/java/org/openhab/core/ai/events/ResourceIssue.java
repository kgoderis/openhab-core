package org.openhab.core.ai.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ResourceIssue {
    private final String resource;
    private final String operation;
    private final double utilization;
    private final String details;
    private final String recommendation;

    public ResourceIssue(String resource, String operation, double utilization, String details,
            String recommendation) {
        this.resource = resource;
        this.operation = operation;
        this.utilization = utilization;
        this.details = details;
        this.recommendation = recommendation;
    }

    public String getResource() { return resource; }
    public String getOperation() { return operation; }
    public double getUtilization() { return utilization; }
    public String getDetails() { return details; }
    public String getRecommendation() { return recommendation; }
}


