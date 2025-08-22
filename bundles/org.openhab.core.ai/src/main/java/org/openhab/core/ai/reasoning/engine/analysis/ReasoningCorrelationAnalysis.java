package org.openhab.core.ai.reasoning.engine.analysis;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Analysis results for reasoning correlations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ReasoningCorrelationAnalysis {

    private final Map<String, Double> stepTypeCorrelations;
    private final Map<String, Double> modelCorrelations;
    private final Map<String, Double> qualityCorrelations;
    private final List<String> significantCorrelations;
    private final Instant analysisTime;

    private ReasoningCorrelationAnalysis(Builder builder) {
        this.stepTypeCorrelations = Map.copyOf(builder.stepTypeCorrelations);
        this.modelCorrelations = Map.copyOf(builder.modelCorrelations);
        this.qualityCorrelations = Map.copyOf(builder.qualityCorrelations);
        this.significantCorrelations = List.copyOf(builder.significantCorrelations);
        this.analysisTime = builder.analysisTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, Double> getStepTypeCorrelations() {
        return stepTypeCorrelations;
    }

    public Map<String, Double> getModelCorrelations() {
        return modelCorrelations;
    }

    public Map<String, Double> getQualityCorrelations() {
        return qualityCorrelations;
    }

    public List<String> getSignificantCorrelations() {
        return significantCorrelations;
    }

    public Instant getAnalysisTime() {
        return analysisTime;
    }

    public static final class Builder {
        private Map<String, Double> stepTypeCorrelations = Map.of();
        private Map<String, Double> modelCorrelations = Map.of();
        private Map<String, Double> qualityCorrelations = Map.of();
        private List<String> significantCorrelations = List.of();
        private Instant analysisTime = Instant.now();

        public Builder withStepTypeCorrelations(Map<String, Double> stepTypeCorrelations) {
            this.stepTypeCorrelations = stepTypeCorrelations;
            return this;
        }

        public Builder withModelCorrelations(Map<String, Double> modelCorrelations) {
            this.modelCorrelations = modelCorrelations;
            return this;
        }

        public Builder withQualityCorrelations(Map<String, Double> qualityCorrelations) {
            this.qualityCorrelations = qualityCorrelations;
            return this;
        }

        public Builder withSignificantCorrelations(List<String> significantCorrelations) {
            this.significantCorrelations = significantCorrelations;
            return this;
        }

        public Builder withAnalysisTime(Instant analysisTime) {
            this.analysisTime = analysisTime;
            return this;
        }

        public ReasoningCorrelationAnalysis build() {
            return new ReasoningCorrelationAnalysis(this);
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
        ReasoningCorrelationAnalysis other = (ReasoningCorrelationAnalysis) obj;
        return Objects.equals(stepTypeCorrelations, other.stepTypeCorrelations)
                && Objects.equals(modelCorrelations, other.modelCorrelations)
                && Objects.equals(qualityCorrelations, other.qualityCorrelations)
                && Objects.equals(significantCorrelations, other.significantCorrelations)
                && Objects.equals(analysisTime, other.analysisTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stepTypeCorrelations, modelCorrelations, qualityCorrelations, significantCorrelations,
                analysisTime);
    }

    @Override
    public String toString() {
        return String.format("ReasoningCorrelationAnalysis{stepTypes=%d, models=%d, significant=%d}",
                stepTypeCorrelations.size(), modelCorrelations.size(), significantCorrelations.size());
    }
}
