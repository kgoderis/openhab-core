package org.openhab.core.ai.reasoning.engine.analysis;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Analysis of reasoning model performance.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ReasoningModelAnalysis {

    private final Map<String, Integer> usageByModel;
    private final Map<String, Double> performanceByModel;
    private final Map<String, Double> qualityByModel;
    private final Map<String, Double> costByModel;
    private final List<String> modelRecommendations;
    private final String bestPerformingModel;
    private final Instant analysisTime;

    private ReasoningModelAnalysis(Builder builder) {
        this.usageByModel = Map.copyOf(builder.usageByModel);
        this.performanceByModel = Map.copyOf(builder.performanceByModel);
        this.qualityByModel = Map.copyOf(builder.qualityByModel);
        this.costByModel = Map.copyOf(builder.costByModel);
        this.modelRecommendations = List.copyOf(builder.modelRecommendations);
        this.bestPerformingModel = builder.bestPerformingModel;
        this.analysisTime = builder.analysisTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, Integer> getUsageByModel() {
        return usageByModel;
    }

    public Map<String, Double> getPerformanceByModel() {
        return performanceByModel;
    }

    public Map<String, Double> getQualityByModel() {
        return qualityByModel;
    }

    public Map<String, Double> getCostByModel() {
        return costByModel;
    }

    public List<String> getModelRecommendations() {
        return modelRecommendations;
    }

    public String getBestPerformingModel() {
        return bestPerformingModel;
    }

    public Instant getAnalysisTime() {
        return analysisTime;
    }

    public static final class Builder {
        private Map<String, Integer> usageByModel = Map.of();
        private Map<String, Double> performanceByModel = Map.of();
        private Map<String, Double> qualityByModel = Map.of();
        private Map<String, Double> costByModel = Map.of();
        private List<String> modelRecommendations = List.of();
        private String bestPerformingModel = "";
        private Instant analysisTime = Instant.now();

        public Builder withUsageByModel(Map<String, Integer> usageByModel) {
            this.usageByModel = usageByModel;
            return this;
        }

        public Builder withPerformanceByModel(Map<String, Double> performanceByModel) {
            this.performanceByModel = performanceByModel;
            return this;
        }

        public Builder withQualityByModel(Map<String, Double> qualityByModel) {
            this.qualityByModel = qualityByModel;
            return this;
        }

        public Builder withCostByModel(Map<String, Double> costByModel) {
            this.costByModel = costByModel;
            return this;
        }

        public Builder withModelRecommendations(List<String> modelRecommendations) {
            this.modelRecommendations = modelRecommendations;
            return this;
        }

        public Builder withBestPerformingModel(String bestPerformingModel) {
            this.bestPerformingModel = bestPerformingModel;
            return this;
        }

        public Builder withAnalysisTime(Instant analysisTime) {
            this.analysisTime = analysisTime;
            return this;
        }

        public ReasoningModelAnalysis build() {
            return new ReasoningModelAnalysis(this);
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
        ReasoningModelAnalysis other = (ReasoningModelAnalysis) obj;
        return Objects.equals(usageByModel, other.usageByModel)
                && Objects.equals(performanceByModel, other.performanceByModel)
                && Objects.equals(qualityByModel, other.qualityByModel)
                && Objects.equals(costByModel, other.costByModel)
                && Objects.equals(modelRecommendations, other.modelRecommendations)
                && Objects.equals(bestPerformingModel, other.bestPerformingModel)
                && Objects.equals(analysisTime, other.analysisTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usageByModel, performanceByModel, qualityByModel, costByModel, modelRecommendations,
                bestPerformingModel, analysisTime);
    }

    @Override
    public String toString() {
        return String.format("ReasoningModelAnalysis{models=%d, best='%s'}", usageByModel.size(), bestPerformingModel);
    }
}
