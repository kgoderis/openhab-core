package org.openhab.core.ai.reasoning.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ModelPerformance {
    private final String modelId;
    private final double successRate;
    private final double averageResponseTime;
    private final long totalRequests;
    private final long successfulRequests;
    private final long failedRequests;
    private final long lastUpdated;

    public ModelPerformance(String modelId, double successRate, double averageResponseTime, long totalRequests,
            long successfulRequests, long failedRequests, long lastUpdated) {
        this.modelId = modelId;
        this.successRate = successRate;
        this.averageResponseTime = averageResponseTime;
        this.totalRequests = totalRequests;
        this.successfulRequests = successfulRequests;
        this.failedRequests = failedRequests;
        this.lastUpdated = lastUpdated;
    }

    public String getModelId() {
        return modelId;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public long getSuccessfulRequests() {
        return successfulRequests;
    }

    public long getFailedRequests() {
        return failedRequests;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }
}
