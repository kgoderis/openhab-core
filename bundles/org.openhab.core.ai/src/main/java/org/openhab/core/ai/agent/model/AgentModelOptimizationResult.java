package org.openhab.core.ai.agent.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.model.api.ModelProviderType;

/**
 * Result of agent model optimization.
 * 
 * <p>
 * This class encapsulates the comprehensive optimization result for an agent model,
 * including performance improvements, configuration changes, and resource optimizations.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModelOptimizationResult {

    private final String modelId;
    private final String modelName;
    private final ModelProviderType providerType;
    private final AgentModelOptimizationStatus status;
    private final double overallImprovement;
    private final @Nullable AgentModelPerformanceOptimization performanceOptimization;
    private final @Nullable AgentModelConfigurationOptimization configurationOptimization;
    private final @Nullable AgentModelResourceOptimization resourceOptimization;
    private final Instant optimizationTime;
    private final @Nullable String errorMessage;
    private final Map<String, Object> metadata;

    private AgentModelOptimizationResult(Builder b) {
        this.modelId = Objects.requireNonNull(b.modelId, "modelId");
        this.modelName = Objects.requireNonNull(b.modelName, "modelName");
        this.providerType = Objects.requireNonNull(b.providerType, "providerType");
        this.status = Objects.requireNonNull(b.status, "status");
        this.overallImprovement = b.overallImprovement;
        this.performanceOptimization = b.performanceOptimization;
        this.configurationOptimization = b.configurationOptimization;
        this.resourceOptimization = b.resourceOptimization;
        this.optimizationTime = Objects.requireNonNull(b.optimizationTime, "optimizationTime");
        this.errorMessage = b.errorMessage;
        this.metadata = Map.copyOf(b.metadata);
    }

    public static Builder builder(String modelId) {
        return new Builder(modelId);
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public String getModelId() {
        return modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public ModelProviderType getProviderType() {
        return providerType;
    }

    public AgentModelOptimizationStatus getStatus() {
        return status;
    }

    public double getOverallImprovement() {
        return overallImprovement;
    }

    public @Nullable AgentModelPerformanceOptimization getPerformanceOptimization() {
        return performanceOptimization;
    }

    public @Nullable AgentModelConfigurationOptimization getConfigurationOptimization() {
        return configurationOptimization;
    }

    public @Nullable AgentModelResourceOptimization getResourceOptimization() {
        return resourceOptimization;
    }

    public Instant getOptimizationTime() {
        return optimizationTime;
    }

    public @Nullable String getErrorMessage() {
        return errorMessage;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public boolean isSuccessful() {
        return status.isSuccessful();
    }

    public boolean hasImprovement() {
        return status.hasImprovement();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AgentModelOptimizationResult other = (AgentModelOptimizationResult) obj;
        return Objects.equals(modelId, other.modelId) && Objects.equals(modelName, other.modelName)
                && providerType == other.providerType && status == other.status
                && Double.compare(overallImprovement, other.overallImprovement) == 0
                && Objects.equals(performanceOptimization, other.performanceOptimization)
                && Objects.equals(configurationOptimization, other.configurationOptimization)
                && Objects.equals(resourceOptimization, other.resourceOptimization)
                && Objects.equals(optimizationTime, other.optimizationTime)
                && Objects.equals(errorMessage, other.errorMessage) && Objects.equals(metadata, other.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelId, modelName, providerType, status, overallImprovement, performanceOptimization,
                configurationOptimization, resourceOptimization, optimizationTime, errorMessage, metadata);
    }

    @Override
    public String toString() {
        return "AgentModelOptimizationResult{" + "modelId='" + modelId + '\'' + ", modelName='" + modelName + '\''
                + ", providerType=" + providerType + ", status=" + status + ", overallImprovement=" + overallImprovement
                + ", optimizationTime=" + optimizationTime + ", errorMessage='" + errorMessage + '\'' + '}';
    }

    public static final class Builder {
        private String modelId;
        private String modelName = "";
        private ModelProviderType providerType = ModelProviderType.OPENAI;
        private AgentModelOptimizationStatus status = AgentModelOptimizationStatus.PENDING;
        private double overallImprovement = 0.0;
        private @Nullable AgentModelPerformanceOptimization performanceOptimization;
        private @Nullable AgentModelConfigurationOptimization configurationOptimization;
        private @Nullable AgentModelResourceOptimization resourceOptimization;
        private Instant optimizationTime = Instant.now();
        private @Nullable String errorMessage;
        private Map<String, Object> metadata = Map.of();

        public Builder(String modelId) {
            this.modelId = Objects.requireNonNull(modelId, "modelId");
        }

        public Builder(AgentModelOptimizationResult source) {
            this.modelId = source.modelId;
            this.modelName = source.modelName;
            this.providerType = source.providerType;
            this.status = source.status;
            this.overallImprovement = source.overallImprovement;
            this.performanceOptimization = source.performanceOptimization;
            this.configurationOptimization = source.configurationOptimization;
            this.resourceOptimization = source.resourceOptimization;
            this.optimizationTime = source.optimizationTime;
            this.errorMessage = source.errorMessage;
            this.metadata = source.metadata;
        }

        public Builder withModelName(String modelName) {
            this.modelName = Objects.requireNonNull(modelName, "modelName");
            return this;
        }

        public Builder withProviderType(ModelProviderType providerType) {
            this.providerType = Objects.requireNonNull(providerType, "providerType");
            return this;
        }

        public Builder withStatus(AgentModelOptimizationStatus status) {
            this.status = Objects.requireNonNull(status, "status");
            return this;
        }

        public Builder withOverallImprovement(double overallImprovement) {
            this.overallImprovement = overallImprovement;
            return this;
        }

        public Builder withPerformanceOptimization(
                @Nullable AgentModelPerformanceOptimization performanceOptimization) {
            this.performanceOptimization = performanceOptimization;
            return this;
        }

        public Builder withConfigurationOptimization(
                @Nullable AgentModelConfigurationOptimization configurationOptimization) {
            this.configurationOptimization = configurationOptimization;
            return this;
        }

        public Builder withResourceOptimization(@Nullable AgentModelResourceOptimization resourceOptimization) {
            this.resourceOptimization = resourceOptimization;
            return this;
        }

        public Builder withOptimizationTime(Instant optimizationTime) {
            this.optimizationTime = Objects.requireNonNull(optimizationTime, "optimizationTime");
            return this;
        }

        public Builder withErrorMessage(@Nullable String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder withMetadata(Map<String, Object> metadata) {
            this.metadata = Objects.requireNonNull(metadata, "metadata");
            return this;
        }

        public Builder withMetadata(String key, Object value) {
            this.metadata = Map.copyOf(Map.of(key, value));
            return this;
        }

        public AgentModelOptimizationResult build() {
            if (modelId.isBlank()) {
                throw new IllegalArgumentException("modelId must not be blank");
            }
            if (overallImprovement < 0.0 || overallImprovement > 1.0) {
                throw new IllegalArgumentException("overallImprovement must be between 0.0 and 1.0");
            }
            return new AgentModelOptimizationResult(this);
        }
    }
}
