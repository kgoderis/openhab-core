package org.openhab.core.ai.agent.model;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Resource optimization for agent models.
 * 
 * <p>
 * This class represents resource optimization recommendations and improvements
 * for agent model resource usage and allocation.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModelResourceOptimization {

    private final double memoryUsageImprovement;
    private final double cpuUsageImprovement;
    private final double networkUsageImprovement;
    private final double storageUsageImprovement;
    private final double overallImprovement;
    private final Map<String, Object> recommendations;
    private final Map<String, Object> metadata;

    private AgentModelResourceOptimization(Builder b) {
        this.memoryUsageImprovement = b.memoryUsageImprovement;
        this.cpuUsageImprovement = b.cpuUsageImprovement;
        this.networkUsageImprovement = b.networkUsageImprovement;
        this.storageUsageImprovement = b.storageUsageImprovement;
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

    public double getMemoryUsageImprovement() {
        return memoryUsageImprovement;
    }

    public double getCpuUsageImprovement() {
        return cpuUsageImprovement;
    }

    public double getNetworkUsageImprovement() {
        return networkUsageImprovement;
    }

    public double getStorageUsageImprovement() {
        return storageUsageImprovement;
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
        AgentModelResourceOptimization other = (AgentModelResourceOptimization) obj;
        return Double.compare(memoryUsageImprovement, other.memoryUsageImprovement) == 0
                && Double.compare(cpuUsageImprovement, other.cpuUsageImprovement) == 0
                && Double.compare(networkUsageImprovement, other.networkUsageImprovement) == 0
                && Double.compare(storageUsageImprovement, other.storageUsageImprovement) == 0
                && Double.compare(overallImprovement, other.overallImprovement) == 0
                && Objects.equals(recommendations, other.recommendations) && Objects.equals(metadata, other.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memoryUsageImprovement, cpuUsageImprovement, networkUsageImprovement,
                storageUsageImprovement, overallImprovement, recommendations, metadata);
    }

    @Override
    public String toString() {
        return "AgentModelResourceOptimization{" + "memoryUsageImprovement=" + memoryUsageImprovement
                + ", cpuUsageImprovement=" + cpuUsageImprovement + ", networkUsageImprovement="
                + networkUsageImprovement + ", storageUsageImprovement=" + storageUsageImprovement
                + ", overallImprovement=" + overallImprovement + '}';
    }

    public static final class Builder {
        private double memoryUsageImprovement = 0.0;
        private double cpuUsageImprovement = 0.0;
        private double networkUsageImprovement = 0.0;
        private double storageUsageImprovement = 0.0;
        private double overallImprovement = 0.0;
        private Map<String, Object> recommendations = Map.of();
        private Map<String, Object> metadata = Map.of();

        public Builder() {
        }

        public Builder(AgentModelResourceOptimization source) {
            this.memoryUsageImprovement = source.memoryUsageImprovement;
            this.cpuUsageImprovement = source.cpuUsageImprovement;
            this.networkUsageImprovement = source.networkUsageImprovement;
            this.storageUsageImprovement = source.storageUsageImprovement;
            this.overallImprovement = source.overallImprovement;
            this.recommendations = source.recommendations;
            this.metadata = source.metadata;
        }

        public Builder withMemoryUsageImprovement(double memoryUsageImprovement) {
            this.memoryUsageImprovement = memoryUsageImprovement;
            return this;
        }

        public Builder withCpuUsageImprovement(double cpuUsageImprovement) {
            this.cpuUsageImprovement = cpuUsageImprovement;
            return this;
        }

        public Builder withNetworkUsageImprovement(double networkUsageImprovement) {
            this.networkUsageImprovement = networkUsageImprovement;
            return this;
        }

        public Builder withStorageUsageImprovement(double storageUsageImprovement) {
            this.storageUsageImprovement = storageUsageImprovement;
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

        public AgentModelResourceOptimization build() {
            if (memoryUsageImprovement < 0.0 || memoryUsageImprovement > 1.0) {
                throw new IllegalArgumentException("memoryUsageImprovement must be between 0.0 and 1.0");
            }
            if (cpuUsageImprovement < 0.0 || cpuUsageImprovement > 1.0) {
                throw new IllegalArgumentException("cpuUsageImprovement must be between 0.0 and 1.0");
            }
            if (networkUsageImprovement < 0.0 || networkUsageImprovement > 1.0) {
                throw new IllegalArgumentException("networkUsageImprovement must be between 0.0 and 1.0");
            }
            if (storageUsageImprovement < 0.0 || storageUsageImprovement > 1.0) {
                throw new IllegalArgumentException("storageUsageImprovement must be between 0.0 and 1.0");
            }
            if (overallImprovement < 0.0 || overallImprovement > 1.0) {
                throw new IllegalArgumentException("overallImprovement must be between 0.0 and 1.0");
            }
            return new AgentModelResourceOptimization(this);
        }
    }
}
