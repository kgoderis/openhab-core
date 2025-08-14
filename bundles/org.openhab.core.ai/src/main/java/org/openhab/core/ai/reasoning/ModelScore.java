package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ModelScore {
    private final String modelId;
    private final double totalScore;
    private final double capabilityScore;
    private final double performanceScore;
    private final double availabilityScore;
    private final double costScore;
    private final double suitabilityScore;

    public ModelScore(String modelId, double totalScore, double capabilityScore, double performanceScore,
            double availabilityScore, double costScore, double suitabilityScore) {
        this.modelId = modelId;
        this.totalScore = totalScore;
        this.capabilityScore = capabilityScore;
        this.performanceScore = performanceScore;
        this.availabilityScore = availabilityScore;
        this.costScore = costScore;
        this.suitabilityScore = suitabilityScore;
    }

    public String getModelId() { return modelId; }
    public double getTotalScore() { return totalScore; }
    public double getCapabilityScore() { return capabilityScore; }
    public double getPerformanceScore() { return performanceScore; }
    public double getAvailabilityScore() { return availabilityScore; }
    public double getCostScore() { return costScore; }
    public double getSuitabilityScore() { return suitabilityScore; }
}


