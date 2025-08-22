package org.openhab.core.ai.reasoning.engine.analysis;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Analysis results for reasoning optimization opportunities.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ReasoningOptimizationAnalysis {

    private final double overallOptimizationPotential;
    private final List<String> optimizationOpportunities;
    private final Map<String, Double> optimizationByModel;
    private final Map<String, Double> optimizationByType;
    private final List<String> recommendedActions;
    private final double estimatedImprovement;
    private final Instant analysisTime;

    private ReasoningOptimizationAnalysis(Builder builder) {
        this.overallOptimizationPotential = builder.overallOptimizationPotential;
        this.optimizationOpportunities = List.copyOf(builder.optimizationOpportunities);
        this.optimizationByModel = Map.copyOf(builder.optimizationByModel);
        this.optimizationByType = Map.copyOf(builder.optimizationByType);
        this.recommendedActions = List.copyOf(builder.recommendedActions);
        this.estimatedImprovement = builder.estimatedImprovement;
        this.analysisTime = builder.analysisTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public double getOverallOptimizationPotential() {
        return overallOptimizationPotential;
    }

    public List<String> getOptimizationOpportunities() {
        return optimizationOpportunities;
    }

    public Map<String, Double> getOptimizationByModel() {
        return optimizationByModel;
    }

    public Map<String, Double> getOptimizationByType() {
        return optimizationByType;
    }

    public List<String> getRecommendedActions() {
        return recommendedActions;
    }

    public double getEstimatedImprovement() {
        return estimatedImprovement;
    }

    public Instant getAnalysisTime() {
        return analysisTime;
    }

    public static final class Builder {
        private double overallOptimizationPotential = 0.0;
        private List<String> optimizationOpportunities = List.of();
        private Map<String, Double> optimizationByModel = Map.of();
        private Map<String, Double> optimizationByType = Map.of();
        private List<String> recommendedActions = List.of();
        private double estimatedImprovement = 0.0;
        private Instant analysisTime = Instant.now();

        public Builder withOverallOptimizationPotential(double overallOptimizationPotential) {
            this.overallOptimizationPotential = overallOptimizationPotential;
            return this;
        }

        public Builder withOptimizationOpportunities(List<String> optimizationOpportunities) {
            this.optimizationOpportunities = optimizationOpportunities;
            return this;
        }

        public Builder withOptimizationByModel(Map<String, Double> optimizationByModel) {
            this.optimizationByModel = optimizationByModel;
            return this;
        }

        public Builder withOptimizationByType(Map<String, Double> optimizationByType) {
            this.optimizationByType = optimizationByType;
            return this;
        }

        public Builder withRecommendedActions(List<String> recommendedActions) {
            this.recommendedActions = recommendedActions;
            return this;
        }

        public Builder withEstimatedImprovement(double estimatedImprovement) {
            this.estimatedImprovement = estimatedImprovement;
            return this;
        }

        public Builder withAnalysisTime(Instant analysisTime) {
            this.analysisTime = analysisTime;
            return this;
        }

        public ReasoningOptimizationAnalysis build() {
            if (overallOptimizationPotential < 0.0 || overallOptimizationPotential > 1.0) {
                throw new IllegalArgumentException("overallOptimizationPotential must be between 0.0 and 1.0");
            }
            if (estimatedImprovement < 0.0 || estimatedImprovement > 1.0) {
                throw new IllegalArgumentException("estimatedImprovement must be between 0.0 and 1.0");
            }
            return new ReasoningOptimizationAnalysis(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ReasoningOptimizationAnalysis other = (ReasoningOptimizationAnalysis) obj;
        return Double.compare(overallOptimizationPotential, other.overallOptimizationPotential) == 0
                && Objects.equals(optimizationOpportunities, other.optimizationOpportunities)
                && Objects.equals(optimizationByModel, other.optimizationByModel)
                && Objects.equals(optimizationByType, other.optimizationByType)
                && Objects.equals(recommendedActions, other.recommendedActions)
                && Double.compare(estimatedImprovement, other.estimatedImprovement) == 0
                && Objects.equals(analysisTime, other.analysisTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(overallOptimizationPotential, optimizationOpportunities, optimizationByModel,
                optimizationByType, recommendedActions, estimatedImprovement, analysisTime);
    }

    @Override
    public String toString() {
        return String.format("ReasoningOptimizationAnalysis{potential=%.2f, improvement=%.2f, opportunities=%d}",
                overallOptimizationPotential, estimatedImprovement, optimizationOpportunities.size());
    }
}
