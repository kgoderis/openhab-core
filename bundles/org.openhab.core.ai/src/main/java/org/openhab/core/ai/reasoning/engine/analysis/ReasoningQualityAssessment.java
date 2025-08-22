package org.openhab.core.ai.reasoning.engine.analysis;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Assessment results for reasoning quality.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ReasoningQualityAssessment {

    private final double overallQualityScore;
    private final Map<String, Double> qualityByModel;
    private final List<String> qualityIssues;
    private final List<String> improvementSuggestions;
    private final Instant assessmentTime;

    private ReasoningQualityAssessment(Builder builder) {
        this.overallQualityScore = builder.overallQualityScore;
        this.qualityByModel = Map.copyOf(builder.qualityByModel);
        this.qualityIssues = List.copyOf(builder.qualityIssues);
        this.improvementSuggestions = List.copyOf(builder.improvementSuggestions);
        this.assessmentTime = builder.assessmentTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public double getOverallQualityScore() {
        return overallQualityScore;
    }

    public Map<String, Double> getQualityByModel() {
        return qualityByModel;
    }

    public List<String> getQualityIssues() {
        return qualityIssues;
    }

    public List<String> getImprovementSuggestions() {
        return improvementSuggestions;
    }

    public Instant getAssessmentTime() {
        return assessmentTime;
    }

    public static final class Builder {
        private double overallQualityScore = 0.0;
        private Map<String, Double> qualityByModel = Map.of();
        private List<String> qualityIssues = List.of();
        private List<String> improvementSuggestions = List.of();
        private Instant assessmentTime = Instant.now();

        public Builder withOverallQualityScore(double overallQualityScore) {
            this.overallQualityScore = overallQualityScore;
            return this;
        }

        public Builder withQualityByModel(Map<String, Double> qualityByModel) {
            this.qualityByModel = qualityByModel;
            return this;
        }

        public Builder withQualityIssues(List<String> qualityIssues) {
            this.qualityIssues = qualityIssues;
            return this;
        }

        public Builder withImprovementSuggestions(List<String> improvementSuggestions) {
            this.improvementSuggestions = improvementSuggestions;
            return this;
        }

        public Builder withAssessmentTime(Instant assessmentTime) {
            this.assessmentTime = assessmentTime;
            return this;
        }

        public ReasoningQualityAssessment build() {
            if (overallQualityScore < 0.0 || overallQualityScore > 1.0) {
                throw new IllegalArgumentException("overallQualityScore must be between 0.0 and 1.0");
            }
            return new ReasoningQualityAssessment(this);
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
        ReasoningQualityAssessment other = (ReasoningQualityAssessment) obj;
        return Double.compare(overallQualityScore, other.overallQualityScore) == 0
                && Objects.equals(qualityByModel, other.qualityByModel)
                && Objects.equals(qualityIssues, other.qualityIssues)
                && Objects.equals(improvementSuggestions, other.improvementSuggestions)
                && Objects.equals(assessmentTime, other.assessmentTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(overallQualityScore, qualityByModel, qualityIssues, improvementSuggestions, assessmentTime);
    }

    @Override
    public String toString() {
        return String.format("ReasoningQualityAssessment{quality=%.2f, issues=%d, suggestions=%d}", overallQualityScore,
                qualityIssues.size(), improvementSuggestions.size());
    }
}
