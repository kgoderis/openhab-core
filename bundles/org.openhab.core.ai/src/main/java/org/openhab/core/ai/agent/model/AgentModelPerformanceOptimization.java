package org.openhab.core.ai.agent.model;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Performance optimization for agent models.
 * 
 * <p>
 * This class represents performance optimization recommendations and improvements
 * for agent model operations.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModelPerformanceOptimization {

    private final double responseTimeImprovement;
    private final double throughputImprovement;
    private final double successRateImprovement;
    private final double availabilityImprovement;
    private final double overallImprovement;
    private final Map<String, Object> recommendations;
    private final Map<String, Object> metadata;

    private AgentModelPerformanceOptimization(Builder b) {
        this.responseTimeImprovement = b.responseTimeImprovement;
        this.throughputImprovement = b.throughputImprovement;
        this.successRateImprovement = b.successRateImprovement;
        this.availabilityImprovement = b.availabilityImprovement;
        this.overallImprovement = b.overallImprovement;
        this.recommendations = Map.copyOf(b.recommendations);
        this.metadata = Map.copyOf(b.metadata);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public double getResponseTimeImprovement() {
        return responseTimeImprovement;
    }

    public double getThroughputImprovement() {
        return throughputImprovement;
    }

    public double getSuccessRateImprovement() {
        return successRateImprovement;
    }

    public double getAvailabilityImprovement() {
        return availabilityImprovement;
    }

    public double getOverallImprovement() {
        return overallImprovement;
    }

    public Map<String, Object> getRecommendations() {
        return recommendations;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AgentModelPerformanceOptimization other = (AgentModelPerformanceOptimization) obj;
        return Double.compare(responseTimeImprovement, other.responseTimeImprovement) == 0
                && Double.compare(throughputImprovement, other.throughputImprovement) == 0
                && Double.compare(successRateImprovement, other.successRateImprovement) == 0
                && Double.compare(availabilityImprovement, other.availabilityImprovement) == 0
                && Double.compare(overallImprovement, other.overallImprovement) == 0
                && Objects.equals(recommendations, other.recommendations) && Objects.equals(metadata, other.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(responseTimeImprovement, throughputImprovement, successRateImprovement,
                availabilityImprovement, overallImprovement, recommendations, metadata);
    }

    @Override
    public String toString() {
        return "AgentModelPerformanceOptimization{" + "responseTimeImprovement=" + responseTimeImprovement
                + ", throughputImprovement=" + throughputImprovement + ", successRateImprovement="
                + successRateImprovement + ", availabilityImprovement=" + availabilityImprovement
                + ", overallImprovement=" + overallImprovement + '}';
    }

    public static final class Builder {
        private double responseTimeImprovement = 0.0;
        private double throughputImprovement = 0.0;
        private double successRateImprovement = 0.0;
        private double availabilityImprovement = 0.0;
        private double overallImprovement = 0.0;
        private Map<String, Object> recommendations = Map.of();
        private Map<String, Object> metadata = Map.of();

        public Builder() {
        }

        public Builder(AgentModelPerformanceOptimization source) {
            this.responseTimeImprovement = source.responseTimeImprovement;
            this.throughputImprovement = source.throughputImprovement;
            this.successRateImprovement = source.successRateImprovement;
            this.availabilityImprovement = source.availabilityImprovement;
            this.overallImprovement = source.overallImprovement;
            this.recommendations = source.recommendations;
            this.metadata = source.metadata;
        }

        public Builder withResponseTimeImprovement(double responseTimeImprovement) {
            this.responseTimeImprovement = responseTimeImprovement;
            return this;
        }

        public Builder withThroughputImprovement(double throughputImprovement) {
            this.throughputImprovement = throughputImprovement;
            return this;
        }

        public Builder withSuccessRateImprovement(double successRateImprovement) {
            this.successRateImprovement = successRateImprovement;
            return this;
        }

        public Builder withAvailabilityImprovement(double availabilityImprovement) {
            this.availabilityImprovement = availabilityImprovement;
            return this;
        }

        public Builder withOverallImprovement(double overallImprovement) {
            this.overallImprovement = overallImprovement;
            return this;
        }

        public Builder withRecommendations(Map<String, Object> recommendations) {
            this.recommendations = Objects.requireNonNull(recommendations, "recommendations");
            return this;
        }

        public Builder withMetadata(Map<String, Object> metadata) {
            this.metadata = Objects.requireNonNull(metadata, "metadata");
            return this;
        }

        public AgentModelPerformanceOptimization build() {
            if (responseTimeImprovement < 0.0 || responseTimeImprovement > 1.0) {
                throw new IllegalArgumentException("responseTimeImprovement must be between 0.0 and 1.0");
            }
            if (throughputImprovement < 0.0 || throughputImprovement > 1.0) {
                throw new IllegalArgumentException("throughputImprovement must be between 0.0 and 1.0");
            }
            if (successRateImprovement < 0.0 || successRateImprovement > 1.0) {
                throw new IllegalArgumentException("successRateImprovement must be between 0.0 and 1.0");
            }
            if (availabilityImprovement < 0.0 || availabilityImprovement > 1.0) {
                throw new IllegalArgumentException("availabilityImprovement must be between 0.0 and 1.0");
            }
            if (overallImprovement < 0.0 || overallImprovement > 1.0) {
                throw new IllegalArgumentException("overallImprovement must be between 0.0 and 1.0");
            }
            return new AgentModelPerformanceOptimization(this);
        }
    }
}
