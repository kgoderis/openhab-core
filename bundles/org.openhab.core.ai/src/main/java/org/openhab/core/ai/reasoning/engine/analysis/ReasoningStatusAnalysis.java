package org.openhab.core.ai.reasoning.engine.analysis;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Analysis of reasoning step statuses.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ReasoningStatusAnalysis {

    private final Map<String, Integer> statusDistribution;
    private final Map<String, Double> statusPerformance;
    private final List<String> statusInsights;
    private final String mostCommonStatus;
    private final double completionRate;
    private final Instant analysisTime;

    private ReasoningStatusAnalysis(Builder builder) {
        this.statusDistribution = Map.copyOf(builder.statusDistribution);
        this.statusPerformance = Map.copyOf(builder.statusPerformance);
        this.statusInsights = List.copyOf(builder.statusInsights);
        this.mostCommonStatus = builder.mostCommonStatus;
        this.completionRate = builder.completionRate;
        this.analysisTime = builder.analysisTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, Integer> getStatusDistribution() {
        return statusDistribution;
    }

    public Map<String, Double> getStatusPerformance() {
        return statusPerformance;
    }

    public List<String> getStatusInsights() {
        return statusInsights;
    }

    public String getMostCommonStatus() {
        return mostCommonStatus;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public Instant getAnalysisTime() {
        return analysisTime;
    }

    public static final class Builder {
        private Map<String, Integer> statusDistribution = Map.of();
        private Map<String, Double> statusPerformance = Map.of();
        private List<String> statusInsights = List.of();
        private String mostCommonStatus = "";
        private double completionRate = 0.0;
        private Instant analysisTime = Instant.now();

        public Builder withStatusDistribution(Map<String, Integer> statusDistribution) {
            this.statusDistribution = statusDistribution;
            return this;
        }

        public Builder withStatusPerformance(Map<String, Double> statusPerformance) {
            this.statusPerformance = statusPerformance;
            return this;
        }

        public Builder withStatusInsights(List<String> statusInsights) {
            this.statusInsights = statusInsights;
            return this;
        }

        public Builder withMostCommonStatus(String mostCommonStatus) {
            this.mostCommonStatus = mostCommonStatus;
            return this;
        }

        public Builder withCompletionRate(double completionRate) {
            this.completionRate = completionRate;
            return this;
        }

        public Builder withAnalysisTime(Instant analysisTime) {
            this.analysisTime = analysisTime;
            return this;
        }

        public ReasoningStatusAnalysis build() {
            if (completionRate < 0.0 || completionRate > 1.0) {
                throw new IllegalArgumentException("completionRate must be between 0.0 and 1.0");
            }
            return new ReasoningStatusAnalysis(this);
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
        ReasoningStatusAnalysis other = (ReasoningStatusAnalysis) obj;
        return Objects.equals(statusDistribution, other.statusDistribution)
                && Objects.equals(statusPerformance, other.statusPerformance)
                && Objects.equals(statusInsights, other.statusInsights)
                && Objects.equals(mostCommonStatus, other.mostCommonStatus)
                && Double.compare(completionRate, other.completionRate) == 0
                && Objects.equals(analysisTime, other.analysisTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusDistribution, statusPerformance, statusInsights, mostCommonStatus, completionRate,
                analysisTime);
    }

    @Override
    public String toString() {
        return String.format("ReasoningStatusAnalysis{mostCommon='%s', completion=%.2f}", mostCommonStatus,
                completionRate);
    }
}
