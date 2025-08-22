package org.openhab.core.ai.reasoning.engine.analysis;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Analysis of reasoning step types.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ReasoningTypeAnalysis {

    private final Map<String, Integer> usageByType;
    private final Map<String, Double> performanceByType;
    private final Map<String, Double> qualityByType;
    private final List<String> typeInsights;
    private final String mostUsedType;
    private final String bestPerformingType;
    private final Instant analysisTime;

    private ReasoningTypeAnalysis(Builder builder) {
        this.usageByType = Map.copyOf(builder.usageByType);
        this.performanceByType = Map.copyOf(builder.performanceByType);
        this.qualityByType = Map.copyOf(builder.qualityByType);
        this.typeInsights = List.copyOf(builder.typeInsights);
        this.mostUsedType = builder.mostUsedType;
        this.bestPerformingType = builder.bestPerformingType;
        this.analysisTime = builder.analysisTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, Integer> getUsageByType() {
        return usageByType;
    }

    public Map<String, Double> getPerformanceByType() {
        return performanceByType;
    }

    public Map<String, Double> getQualityByType() {
        return qualityByType;
    }

    public List<String> getTypeInsights() {
        return typeInsights;
    }

    public String getMostUsedType() {
        return mostUsedType;
    }

    public String getBestPerformingType() {
        return bestPerformingType;
    }

    public Instant getAnalysisTime() {
        return analysisTime;
    }

    public static final class Builder {
        private Map<String, Integer> usageByType = Map.of();
        private Map<String, Double> performanceByType = Map.of();
        private Map<String, Double> qualityByType = Map.of();
        private List<String> typeInsights = List.of();
        private String mostUsedType = "";
        private String bestPerformingType = "";
        private Instant analysisTime = Instant.now();

        public Builder withUsageByType(Map<String, Integer> usageByType) {
            this.usageByType = usageByType;
            return this;
        }

        public Builder withPerformanceByType(Map<String, Double> performanceByType) {
            this.performanceByType = performanceByType;
            return this;
        }

        public Builder withQualityByType(Map<String, Double> qualityByType) {
            this.qualityByType = qualityByType;
            return this;
        }

        public Builder withTypeInsights(List<String> typeInsights) {
            this.typeInsights = typeInsights;
            return this;
        }

        public Builder withMostUsedType(String mostUsedType) {
            this.mostUsedType = mostUsedType;
            return this;
        }

        public Builder withBestPerformingType(String bestPerformingType) {
            this.bestPerformingType = bestPerformingType;
            return this;
        }

        public Builder withAnalysisTime(Instant analysisTime) {
            this.analysisTime = analysisTime;
            return this;
        }

        public ReasoningTypeAnalysis build() {
            return new ReasoningTypeAnalysis(this);
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
        ReasoningTypeAnalysis other = (ReasoningTypeAnalysis) obj;
        return Objects.equals(usageByType, other.usageByType)
                && Objects.equals(performanceByType, other.performanceByType)
                && Objects.equals(qualityByType, other.qualityByType)
                && Objects.equals(typeInsights, other.typeInsights) && Objects.equals(mostUsedType, other.mostUsedType)
                && Objects.equals(bestPerformingType, other.bestPerformingType)
                && Objects.equals(analysisTime, other.analysisTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usageByType, performanceByType, qualityByType, typeInsights, mostUsedType,
                bestPerformingType, analysisTime);
    }

    @Override
    public String toString() {
        return String.format("ReasoningTypeAnalysis{types=%d, mostUsed='%s', best='%s'}", usageByType.size(),
                mostUsedType, bestPerformingType);
    }
}
