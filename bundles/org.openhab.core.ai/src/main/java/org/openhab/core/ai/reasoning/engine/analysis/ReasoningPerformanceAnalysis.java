package org.openhab.core.ai.reasoning.engine.analysis;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Analysis results for reasoning performance.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ReasoningPerformanceAnalysis {

    private final double averageProcessingTime;
    private final double averageTokensPerStep;
    private final double averageCostPerStep;
    private final Map<String, Double> performanceByModel;
    private final List<String> optimizationSuggestions;
    private final Instant analysisTime;

    private ReasoningPerformanceAnalysis(Builder builder) {
        this.averageProcessingTime = builder.averageProcessingTime;
        this.averageTokensPerStep = builder.averageTokensPerStep;
        this.averageCostPerStep = builder.averageCostPerStep;
        this.performanceByModel = Map.copyOf(builder.performanceByModel);
        this.optimizationSuggestions = List.copyOf(builder.optimizationSuggestions);
        this.analysisTime = builder.analysisTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public double getAverageProcessingTime() {
        return averageProcessingTime;
    }

    public double getAverageTokensPerStep() {
        return averageTokensPerStep;
    }

    public double getAverageCostPerStep() {
        return averageCostPerStep;
    }

    public Map<String, Double> getPerformanceByModel() {
        return performanceByModel;
    }

    public List<String> getOptimizationSuggestions() {
        return optimizationSuggestions;
    }

    public Instant getAnalysisTime() {
        return analysisTime;
    }

    public static final class Builder {
        private double averageProcessingTime = 0.0;
        private double averageTokensPerStep = 0.0;
        private double averageCostPerStep = 0.0;
        private Map<String, Double> performanceByModel = Map.of();
        private List<String> optimizationSuggestions = List.of();
        private Instant analysisTime = Instant.now();

        public Builder withAverageProcessingTime(double averageProcessingTime) {
            this.averageProcessingTime = averageProcessingTime;
            return this;
        }

        public Builder withAverageTokensPerStep(double averageTokensPerStep) {
            this.averageTokensPerStep = averageTokensPerStep;
            return this;
        }

        public Builder withAverageCostPerStep(double averageCostPerStep) {
            this.averageCostPerStep = averageCostPerStep;
            return this;
        }

        public Builder withPerformanceByModel(Map<String, Double> performanceByModel) {
            this.performanceByModel = performanceByModel;
            return this;
        }

        public Builder withOptimizationSuggestions(List<String> optimizationSuggestions) {
            this.optimizationSuggestions = optimizationSuggestions;
            return this;
        }

        public Builder withAnalysisTime(Instant analysisTime) {
            this.analysisTime = analysisTime;
            return this;
        }

        public ReasoningPerformanceAnalysis build() {
            return new ReasoningPerformanceAnalysis(this);
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
        ReasoningPerformanceAnalysis other = (ReasoningPerformanceAnalysis) obj;
        return Double.compare(averageProcessingTime, other.averageProcessingTime) == 0
                && Double.compare(averageTokensPerStep, other.averageTokensPerStep) == 0
                && Double.compare(averageCostPerStep, other.averageCostPerStep) == 0
                && Objects.equals(performanceByModel, other.performanceByModel)
                && Objects.equals(optimizationSuggestions, other.optimizationSuggestions)
                && Objects.equals(analysisTime, other.analysisTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(averageProcessingTime, averageTokensPerStep, averageCostPerStep, performanceByModel,
                optimizationSuggestions, analysisTime);
    }

    @Override
    public String toString() {
        return String.format("ReasoningPerformanceAnalysis{avgTime=%.2fms, avgTokens=%.2f, avgCost=$%.4f}",
                averageProcessingTime, averageTokensPerStep, averageCostPerStep);
    }
}
