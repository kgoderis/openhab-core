package org.openhab.core.ai.agent.model;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration optimization for agent models.
 * 
 * <p>
 * This class represents configuration optimization recommendations and improvements
 * for agent model settings and parameters.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModelConfigurationOptimization {

    private final double maxConcurrentRequestsImprovement;
    private final double requestTimeoutImprovement;
    private final double maxTokensImprovement;
    private final double temperatureImprovement;
    private final double overallImprovement;
    private final Map<String, Object> recommendations;
    private final Map<String, Object> metadata;

    private AgentModelConfigurationOptimization(Builder b) {
        this.maxConcurrentRequestsImprovement = b.maxConcurrentRequestsImprovement;
        this.requestTimeoutImprovement = b.requestTimeoutImprovement;
        this.maxTokensImprovement = b.maxTokensImprovement;
        this.temperatureImprovement = b.temperatureImprovement;
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

    public double getMaxConcurrentRequestsImprovement() {
        return maxConcurrentRequestsImprovement;
    }

    public double getRequestTimeoutImprovement() {
        return requestTimeoutImprovement;
    }

    public double getMaxTokensImprovement() {
        return maxTokensImprovement;
    }

    public double getTemperatureImprovement() {
        return temperatureImprovement;
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
        AgentModelConfigurationOptimization other = (AgentModelConfigurationOptimization) obj;
        return Double.compare(maxConcurrentRequestsImprovement, other.maxConcurrentRequestsImprovement) == 0
                && Double.compare(requestTimeoutImprovement, other.requestTimeoutImprovement) == 0
                && Double.compare(maxTokensImprovement, other.maxTokensImprovement) == 0
                && Double.compare(temperatureImprovement, other.temperatureImprovement) == 0
                && Double.compare(overallImprovement, other.overallImprovement) == 0
                && Objects.equals(recommendations, other.recommendations) && Objects.equals(metadata, other.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxConcurrentRequestsImprovement, requestTimeoutImprovement, maxTokensImprovement,
                temperatureImprovement, overallImprovement, recommendations, metadata);
    }

    @Override
    public String toString() {
        return "AgentModelConfigurationOptimization{" + "maxConcurrentRequestsImprovement="
                + maxConcurrentRequestsImprovement + ", requestTimeoutImprovement=" + requestTimeoutImprovement
                + ", maxTokensImprovement=" + maxTokensImprovement + ", temperatureImprovement="
                + temperatureImprovement + ", overallImprovement=" + overallImprovement + '}';
    }

    public static final class Builder {
        private double maxConcurrentRequestsImprovement = 0.0;
        private double requestTimeoutImprovement = 0.0;
        private double maxTokensImprovement = 0.0;
        private double temperatureImprovement = 0.0;
        private double overallImprovement = 0.0;
        private Map<String, Object> recommendations = Map.of();
        private Map<String, Object> metadata = Map.of();

        public Builder() {
        }

        public Builder(AgentModelConfigurationOptimization source) {
            this.maxConcurrentRequestsImprovement = source.maxConcurrentRequestsImprovement;
            this.requestTimeoutImprovement = source.requestTimeoutImprovement;
            this.maxTokensImprovement = source.maxTokensImprovement;
            this.temperatureImprovement = source.temperatureImprovement;
            this.overallImprovement = source.overallImprovement;
            this.recommendations = source.recommendations;
            this.metadata = source.metadata;
        }

        public Builder withMaxConcurrentRequestsImprovement(double maxConcurrentRequestsImprovement) {
            this.maxConcurrentRequestsImprovement = maxConcurrentRequestsImprovement;
            return this;
        }

        public Builder withRequestTimeoutImprovement(double requestTimeoutImprovement) {
            this.requestTimeoutImprovement = requestTimeoutImprovement;
            return this;
        }

        public Builder withMaxTokensImprovement(double maxTokensImprovement) {
            this.maxTokensImprovement = maxTokensImprovement;
            return this;
        }

        public Builder withTemperatureImprovement(double temperatureImprovement) {
            this.temperatureImprovement = temperatureImprovement;
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

        public AgentModelConfigurationOptimization build() {
            if (maxConcurrentRequestsImprovement < 0.0 || maxConcurrentRequestsImprovement > 1.0) {
                throw new IllegalArgumentException("maxConcurrentRequestsImprovement must be between 0.0 and 1.0");
            }
            if (requestTimeoutImprovement < 0.0 || requestTimeoutImprovement > 1.0) {
                throw new IllegalArgumentException("requestTimeoutImprovement must be between 0.0 and 1.0");
            }
            if (maxTokensImprovement < 0.0 || maxTokensImprovement > 1.0) {
                throw new IllegalArgumentException("maxTokensImprovement must be between 0.0 and 1.0");
            }
            if (temperatureImprovement < 0.0 || temperatureImprovement > 1.0) {
                throw new IllegalArgumentException("temperatureImprovement must be between 0.0 and 1.0");
            }
            if (overallImprovement < 0.0 || overallImprovement > 1.0) {
                throw new IllegalArgumentException("overallImprovement must be between 0.0 and 1.0");
            }
            return new AgentModelConfigurationOptimization(this);
        }
    }
}
