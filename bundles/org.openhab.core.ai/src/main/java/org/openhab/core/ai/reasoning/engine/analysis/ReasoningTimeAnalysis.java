package org.openhab.core.ai.reasoning.engine.analysis;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Time-based analysis of reasoning steps.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ReasoningTimeAnalysis {

    private final double averageProcessingTime;
    private final double medianProcessingTime;
    private final double p95ProcessingTime;
    private final Map<String, Double> processingTimeByModel;
    private final Map<String, Double> processingTimeByType;
    private final List<String> timeInsights;
    private final Instant analysisTime;

    private ReasoningTimeAnalysis(Builder builder) {
        this.averageProcessingTime = builder.averageProcessingTime;
        this.medianProcessingTime = builder.medianProcessingTime;
        this.p95ProcessingTime = builder.p95ProcessingTime;
        this.processingTimeByModel = Map.copyOf(builder.processingTimeByModel);
        this.processingTimeByType = Map.copyOf(builder.processingTimeByType);
        this.timeInsights = List.copyOf(builder.timeInsights);
        this.analysisTime = builder.analysisTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public double getAverageProcessingTime() {
        return averageProcessingTime;
    }

    public double getMedianProcessingTime() {
        return medianProcessingTime;
    }

    public double getP95ProcessingTime() {
        return p95ProcessingTime;
    }

    public Map<String, Double> getProcessingTimeByModel() {
        return processingTimeByModel;
    }

    public Map<String, Double> getProcessingTimeByType() {
        return processingTimeByType;
    }

    public List<String> getTimeInsights() {
        return timeInsights;
    }

    public Instant getAnalysisTime() {
        return analysisTime;
    }

    public static final class Builder {
        private double averageProcessingTime = 0.0;
        private double medianProcessingTime = 0.0;
        private double p95ProcessingTime = 0.0;
        private Map<String, Double> processingTimeByModel = Map.of();
        private Map<String, Double> processingTimeByType = Map.of();
        private List<String> timeInsights = List.of();
        private Instant analysisTime = Instant.now();

        public Builder withAverageProcessingTime(double averageProcessingTime) {
            this.averageProcessingTime = averageProcessingTime;
            return this;
        }

        public Builder withMedianProcessingTime(double medianProcessingTime) {
            this.medianProcessingTime = medianProcessingTime;
            return this;
        }

        public Builder withP95ProcessingTime(double p95ProcessingTime) {
            this.p95ProcessingTime = p95ProcessingTime;
            return this;
        }

        public Builder withProcessingTimeByModel(Map<String, Double> processingTimeByModel) {
            this.processingTimeByModel = processingTimeByModel;
            return this;
        }

        public Builder withProcessingTimeByType(Map<String, Double> processingTimeByType) {
            this.processingTimeByType = processingTimeByType;
            return this;
        }

        public Builder withTimeInsights(List<String> timeInsights) {
            this.timeInsights = timeInsights;
            return this;
        }

        public Builder withAnalysisTime(Instant analysisTime) {
            this.analysisTime = analysisTime;
            return this;
        }

        public ReasoningTimeAnalysis build() {
            if (averageProcessingTime < 0.0) {
                throw new IllegalArgumentException("averageProcessingTime must be non-negative");
            }
            if (medianProcessingTime < 0.0) {
                throw new IllegalArgumentException("medianProcessingTime must be non-negative");
            }
            if (p95ProcessingTime < 0.0) {
                throw new IllegalArgumentException("p95ProcessingTime must be non-negative");
            }
            return new ReasoningTimeAnalysis(this);
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
        ReasoningTimeAnalysis other = (ReasoningTimeAnalysis) obj;
        return Double.compare(averageProcessingTime, other.averageProcessingTime) == 0
                && Double.compare(medianProcessingTime, other.medianProcessingTime) == 0
                && Double.compare(p95ProcessingTime, other.p95ProcessingTime) == 0
                && Objects.equals(processingTimeByModel, other.processingTimeByModel)
                && Objects.equals(processingTimeByType, other.processingTimeByType)
                && Objects.equals(timeInsights, other.timeInsights) && Objects.equals(analysisTime, other.analysisTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(averageProcessingTime, medianProcessingTime, p95ProcessingTime, processingTimeByModel,
                processingTimeByType, timeInsights, analysisTime);
    }

    @Override
    public String toString() {
        return String.format("ReasoningTimeAnalysis{avg=%.2f, median=%.2f, p95=%.2f}", averageProcessingTime,
                medianProcessingTime, p95ProcessingTime);
    }
}
