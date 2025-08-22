package org.openhab.core.ai.reasoning.engine.analysis;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Results of anomaly detection in reasoning steps.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ReasoningAnomalyDetection {

    private final int totalAnomalies;
    private final List<String> anomalyTypes;
    private final Map<String, Integer> anomaliesByType;
    private final Map<String, Integer> anomaliesByModel;
    private final List<String> suspiciousPatterns;
    private final double anomalyRate;
    private final Instant detectionTime;

    private ReasoningAnomalyDetection(Builder builder) {
        this.totalAnomalies = builder.totalAnomalies;
        this.anomalyTypes = List.copyOf(builder.anomalyTypes);
        this.anomaliesByType = Map.copyOf(builder.anomaliesByType);
        this.anomaliesByModel = Map.copyOf(builder.anomaliesByModel);
        this.suspiciousPatterns = List.copyOf(builder.suspiciousPatterns);
        this.anomalyRate = builder.anomalyRate;
        this.detectionTime = builder.detectionTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getTotalAnomalies() {
        return totalAnomalies;
    }

    public List<String> getAnomalyTypes() {
        return anomalyTypes;
    }

    public Map<String, Integer> getAnomaliesByType() {
        return anomaliesByType;
    }

    public Map<String, Integer> getAnomaliesByModel() {
        return anomaliesByModel;
    }

    public List<String> getSuspiciousPatterns() {
        return suspiciousPatterns;
    }

    public double getAnomalyRate() {
        return anomalyRate;
    }

    public Instant getDetectionTime() {
        return detectionTime;
    }

    public static final class Builder {
        private int totalAnomalies = 0;
        private List<String> anomalyTypes = List.of();
        private Map<String, Integer> anomaliesByType = Map.of();
        private Map<String, Integer> anomaliesByModel = Map.of();
        private List<String> suspiciousPatterns = List.of();
        private double anomalyRate = 0.0;
        private Instant detectionTime = Instant.now();

        public Builder withTotalAnomalies(int totalAnomalies) {
            this.totalAnomalies = totalAnomalies;
            return this;
        }

        public Builder withAnomalyTypes(List<String> anomalyTypes) {
            this.anomalyTypes = anomalyTypes;
            return this;
        }

        public Builder withAnomaliesByType(Map<String, Integer> anomaliesByType) {
            this.anomaliesByType = anomaliesByType;
            return this;
        }

        public Builder withAnomaliesByModel(Map<String, Integer> anomaliesByModel) {
            this.anomaliesByModel = anomaliesByModel;
            return this;
        }

        public Builder withSuspiciousPatterns(List<String> suspiciousPatterns) {
            this.suspiciousPatterns = suspiciousPatterns;
            return this;
        }

        public Builder withAnomalyRate(double anomalyRate) {
            this.anomalyRate = anomalyRate;
            return this;
        }

        public Builder withDetectionTime(Instant detectionTime) {
            this.detectionTime = detectionTime;
            return this;
        }

        public ReasoningAnomalyDetection build() {
            if (totalAnomalies < 0) {
                throw new IllegalArgumentException("totalAnomalies must be non-negative");
            }
            if (anomalyRate < 0.0 || anomalyRate > 1.0) {
                throw new IllegalArgumentException("anomalyRate must be between 0.0 and 1.0");
            }
            return new ReasoningAnomalyDetection(this);
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
        ReasoningAnomalyDetection other = (ReasoningAnomalyDetection) obj;
        return totalAnomalies == other.totalAnomalies && Objects.equals(anomalyTypes, other.anomalyTypes)
                && Objects.equals(anomaliesByType, other.anomaliesByType)
                && Objects.equals(anomaliesByModel, other.anomaliesByModel)
                && Objects.equals(suspiciousPatterns, other.suspiciousPatterns)
                && Double.compare(anomalyRate, other.anomalyRate) == 0
                && Objects.equals(detectionTime, other.detectionTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalAnomalies, anomalyTypes, anomaliesByType, anomaliesByModel, suspiciousPatterns,
                anomalyRate, detectionTime);
    }

    @Override
    public String toString() {
        return String.format("ReasoningAnomalyDetection{total=%d, rate=%.2f, types=%d}", totalAnomalies, anomalyRate,
                anomalyTypes.size());
    }
}
