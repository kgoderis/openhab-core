package org.openhab.core.ai.agent.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.AgentModelSnapshot;
import org.openhab.core.ai.model.api.ModelProviderType;

/**
 * Represents an agent model with its capabilities and configuration.
 * 
 * <p>
 * This class encapsulates all information about an agent model including
 * its provider, capabilities, performance metrics, and configuration options.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModel {

    private final String modelId;
    private final String name;
    private final String description;
    private final ModelProviderType providerType;
    private final String version;
    private final Set<String> capabilities;
    private final Map<String, Object> parameters;
    private final @Nullable MetricsService metricsService;
    private final Instant registrationTime;
    private final @Nullable String modelUrl;
    private final @Nullable String documentationUrl;

    private AgentModel(Builder b) {
        this.modelId = b.modelId;
        this.name = b.name;
        this.description = b.description;
        this.providerType = b.providerType;
        this.version = b.version;
        this.capabilities = Set.copyOf(b.capabilities);
        this.parameters = Map.copyOf(b.parameters);
        this.metricsService = b.metricsService;
        this.registrationTime = b.registrationTime;
        this.modelUrl = b.modelUrl;
        this.documentationUrl = b.documentationUrl;
    }

    public static Builder builder(String modelId, String name, ModelProviderType providerType) {
        return new Builder(modelId, name, providerType);
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public String getModelId() {
        return modelId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ModelProviderType getProviderType() {
        return providerType;
    }

    public String getVersion() {
        return version;
    }

    public Set<String> getCapabilities() {
        return capabilities;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public @Nullable AgentModelSnapshot getPerformanceMetrics() {
        return metricsService != null
                ? metricsService.getSnapshot(MetricKeys.agentModel(modelId), AgentModelSnapshot.class)
                : null;
    }

    public Instant getRegistrationTime() {
        return registrationTime;
    }

    public @Nullable String getModelUrl() {
        return modelUrl;
    }

    public @Nullable String getDocumentationUrl() {
        return documentationUrl;
    }

    public boolean hasCapability(String capability) {
        return capabilities.contains(capability);
    }

    public @Nullable Object getParameter(String key) {
        return parameters.get(key);
    }

    public static final class Builder {
        private String modelId;
        private String name;
        private String description = "";
        private ModelProviderType providerType;
        private String version = "1.0.0";
        private Set<String> capabilities = Set.of();
        private Map<String, Object> parameters = Map.of();
        private @Nullable MetricsService metricsService;
        private Instant registrationTime = Instant.now();
        private @Nullable String modelUrl;
        private @Nullable String documentationUrl;

        public Builder(String modelId, String name, ModelProviderType providerType) {
            this.modelId = Objects.requireNonNull(modelId, "modelId");
            this.name = Objects.requireNonNull(name, "name");
            this.providerType = Objects.requireNonNull(providerType, "providerType");
        }

        public Builder(AgentModel source) {
            this.modelId = source.modelId;
            this.name = source.name;
            this.description = source.description;
            this.providerType = source.providerType;
            this.version = source.version;
            this.capabilities = source.capabilities;
            this.parameters = source.parameters;
            this.metricsService = source.metricsService;
            this.registrationTime = source.registrationTime;
            this.modelUrl = source.modelUrl;
            this.documentationUrl = source.documentationUrl;
        }

        public Builder withDescription(String description) {
            this.description = Objects.requireNonNull(description, "description");
            return this;
        }

        public Builder withVersion(String version) {
            this.version = Objects.requireNonNull(version, "version");
            return this;
        }

        public Builder withCapabilities(Set<String> capabilities) {
            this.capabilities = Objects.requireNonNull(capabilities, "capabilities");
            return this;
        }

        public Builder withCapabilities(String... capabilities) {
            this.capabilities = Set.of(capabilities);
            return this;
        }

        public Builder withParameters(Map<String, Object> parameters) {
            this.parameters = Objects.requireNonNull(parameters, "parameters");
            return this;
        }

        public Builder withParameter(String key, Object value) {
            this.parameters = Map.copyOf(Map.of(key, value));
            return this;
        }

        public Builder withMetricsService(MetricsService metricsService) {
            this.metricsService = metricsService;
            return this;
        }

        public Builder withRegistrationTime(Instant registrationTime) {
            this.registrationTime = Objects.requireNonNull(registrationTime, "registrationTime");
            return this;
        }

        public Builder withModelUrl(@Nullable String modelUrl) {
            this.modelUrl = modelUrl;
            return this;
        }

        public Builder withDocumentationUrl(@Nullable String documentationUrl) {
            this.documentationUrl = documentationUrl;
            return this;
        }

        public AgentModel build() {
            if (modelId.isBlank()) {
                throw new IllegalArgumentException("modelId must not be blank");
            }
            if (name.isBlank()) {
                throw new IllegalArgumentException("name must not be blank");
            }
            return new AgentModel(this);
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
        AgentModel other = (AgentModel) obj;
        return Objects.equals(modelId, other.modelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelId);
    }

    @Override
    public String toString() {
        return "AgentModel{" + "modelId='" + modelId + '\'' + ", name='" + name + '\'' + ", providerType="
                + providerType + ", version='" + version + '\'' + ", capabilities=" + capabilities
                + ", registrationTime=" + registrationTime + '}';
    }
}
