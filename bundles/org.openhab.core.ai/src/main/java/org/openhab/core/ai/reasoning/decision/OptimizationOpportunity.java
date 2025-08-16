package org.openhab.core.ai.reasoning.decision;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents an optimization opportunity in decision making.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class OptimizationOpportunity {
    private final String opportunityId;
    private final String description;
    private final String category;
    private final double potentialImpact;
    private final double confidence;
    private final String recommendation;

    public OptimizationOpportunity(String opportunityId, String description, String category, double potentialImpact,
            double confidence, String recommendation) {
        this.opportunityId = opportunityId;
        this.description = description;
        this.category = category;
        this.potentialImpact = potentialImpact;
        this.confidence = confidence;
        this.recommendation = recommendation;
    }

    public String getOpportunityId() {
        return opportunityId;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public double getPotentialImpact() {
        return potentialImpact;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getRecommendation() {
        return recommendation;
    }
}
