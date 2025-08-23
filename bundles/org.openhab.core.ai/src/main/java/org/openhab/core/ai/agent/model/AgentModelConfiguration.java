package org.openhab.core.ai.agent.model;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.config.common.BaseConfiguration;

/**
 * Unified Agent Model Configuration for building agent-specific configurations.
 * 
 * <p>
 * This class provides a comprehensive configuration for agent operations including:
 * - Agent identification and model preferences
 * - Model parameters and optimization settings
 * - Caching and performance configurations
 * - Security and monitoring settings
 * - Prompt template configurations
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelConfiguration extends BaseConfiguration {

    private final String agentId;
    private final String preferredModel;
    private final String fallbackModel;
    private final double temperature;
    private final int maxTokens;
    private final Duration timeout;
    private final int maxRetries;
    private final Duration retryDelay;
    private final boolean enableCaching;
    private final Duration cacheExpiration;
    private final int maxCacheSize;
    private final boolean enableOptimization;
    private final Map<String, Object> optimizationSettings;
    private final boolean enableSecurity;
    private final Map<String, Object> securitySettings;
    private final boolean enableMonitoring;
    private final Map<String, Object> monitoringSettings;
    private final Map<String, String> promptTemplates;

    private AgentModelConfiguration(Builder builder) {
        super(builder.id, builder.enabled, builder.name, builder.version, builder.customOptions);
        this.agentId = builder.agentId;
        this.preferredModel = builder.preferredModel;
        this.fallbackModel = builder.fallbackModel;
        this.temperature = builder.temperature;
        this.maxTokens = builder.maxTokens;
        this.timeout = builder.timeout;
        this.maxRetries = builder.maxRetries;
        this.retryDelay = builder.retryDelay;
        this.enableCaching = builder.enableCaching;
        this.cacheExpiration = builder.cacheExpiration;
        this.maxCacheSize = builder.maxCacheSize;
        this.enableOptimization = builder.enableOptimization;
        this.optimizationSettings = Map.copyOf(builder.optimizationSettings);
        this.enableSecurity = builder.enableSecurity;
        this.securitySettings = Map.copyOf(builder.securitySettings);
        this.enableMonitoring = builder.enableMonitoring;
        this.monitoringSettings = Map.copyOf(builder.monitoringSettings);
        this.promptTemplates = Map.copyOf(builder.promptTemplates);
    }

    public String getAgentId() {
        return agentId;
    }

    public String getPreferredModel() {
        return preferredModel;
    }

    public String getFallbackModel() {
        return fallbackModel;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public Duration getRetryDelay() {
        return retryDelay;
    }

    public boolean isEnableCaching() {
        return enableCaching;
    }

    public Duration getCacheExpiration() {
        return cacheExpiration;
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public boolean isEnableOptimization() {
        return enableOptimization;
    }

    public Map<String, Object> getOptimizationSettings() {
        return optimizationSettings;
    }

    public boolean isEnableSecurity() {
        return enableSecurity;
    }

    public Map<String, Object> getSecuritySettings() {
        return securitySettings;
    }

    public boolean isEnableMonitoring() {
        return enableMonitoring;
    }

    public Map<String, Object> getMonitoringSettings() {
        return monitoringSettings;
    }

    public Map<String, String> getPromptTemplates() {
        return promptTemplates;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        AgentModelConfiguration other = (AgentModelConfiguration) obj;
        return Objects.equals(agentId, other.agentId) && Objects.equals(preferredModel, other.preferredModel)
                && Objects.equals(fallbackModel, other.fallbackModel)
                && Double.compare(temperature, other.temperature) == 0 && maxTokens == other.maxTokens
                && Objects.equals(timeout, other.timeout) && maxRetries == other.maxRetries
                && Objects.equals(retryDelay, other.retryDelay) && enableCaching == other.enableCaching
                && Objects.equals(cacheExpiration, other.cacheExpiration) && maxCacheSize == other.maxCacheSize
                && enableOptimization == other.enableOptimization
                && Objects.equals(optimizationSettings, other.optimizationSettings)
                && enableSecurity == other.enableSecurity && Objects.equals(securitySettings, other.securitySettings)
                && enableMonitoring == other.enableMonitoring
                && Objects.equals(monitoringSettings, other.monitoringSettings)
                && Objects.equals(promptTemplates, other.promptTemplates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), agentId, preferredModel, fallbackModel, temperature, maxTokens, timeout,
                maxRetries, retryDelay, enableCaching, cacheExpiration, maxCacheSize, enableOptimization,
                optimizationSettings, enableSecurity, securitySettings, enableMonitoring, monitoringSettings,
                promptTemplates);
    }

    @Override
    public String toString() {
        return String.format(
                "AgentModelConfiguration{id='%s', agentId='%s', preferredModel='%s', fallbackModel='%s', temperature=%.2f, maxTokens=%d, enabled=%s}",
                getId(), agentId, preferredModel, fallbackModel, temperature, maxTokens, isEnabled());
    }

    public static final class Builder {
        private String id = "";
        private boolean enabled = true;
        private String name = "Agent Model Configuration";
        private String version = "1.0.0";
        private final Map<String, Object> customOptions = new HashMap<>();
        private String agentId = "";
        private String preferredModel = "gpt-4";
        private String fallbackModel = "gpt-3.5-turbo";
        private double temperature = 0.7;
        private int maxTokens = 1000;
        private Duration timeout = Duration.ofSeconds(30);
        private int maxRetries = 3;
        private Duration retryDelay = Duration.ofSeconds(5);
        private boolean enableCaching = true;
        private Duration cacheExpiration = Duration.ofMinutes(30);
        private int maxCacheSize = 1000;
        private boolean enableOptimization = true;
        private final Map<String, Object> optimizationSettings = new HashMap<>();
        private boolean enableSecurity = true;
        private final Map<String, Object> securitySettings = new HashMap<>();
        private boolean enableMonitoring = true;
        private final Map<String, Object> monitoringSettings = new HashMap<>();
        private final Map<String, String> promptTemplates = new HashMap<>();

        public Builder() {
        }

        public Builder(AgentModelConfiguration source) {
            this.id = source.getId();
            this.enabled = source.isEnabled();
            this.name = source.getName();
            this.version = source.getVersion();
            this.customOptions.putAll(source.getCustomOptions());
            this.agentId = source.agentId;
            this.preferredModel = source.preferredModel;
            this.fallbackModel = source.fallbackModel;
            this.temperature = source.temperature;
            this.maxTokens = source.maxTokens;
            this.timeout = source.timeout;
            this.maxRetries = source.maxRetries;
            this.retryDelay = source.retryDelay;
            this.enableCaching = source.enableCaching;
            this.cacheExpiration = source.cacheExpiration;
            this.maxCacheSize = source.maxCacheSize;
            this.enableOptimization = source.enableOptimization;
            this.optimizationSettings.putAll(source.optimizationSettings);
            this.enableSecurity = source.enableSecurity;
            this.securitySettings.putAll(source.securitySettings);
            this.enableMonitoring = source.enableMonitoring;
            this.monitoringSettings.putAll(source.monitoringSettings);
            this.promptTemplates.putAll(source.promptTemplates);
        }

        public Builder withId(String id) {
            this.id = Objects.requireNonNull(id, "id");
            return this;
        }

        public Builder withEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder withName(String name) {
            this.name = Objects.requireNonNull(name, "name");
            return this;
        }

        public Builder withVersion(String version) {
            this.version = Objects.requireNonNull(version, "version");
            return this;
        }

        public Builder withCustomOption(String key, Object value) {
            this.customOptions.put(Objects.requireNonNull(key, "key"), value);
            return this;
        }

        public Builder withAgentId(String agentId) {
            this.agentId = Objects.requireNonNull(agentId, "agentId");
            return this;
        }

        public Builder withPreferredModel(String preferredModel) {
            this.preferredModel = Objects.requireNonNull(preferredModel, "preferredModel");
            return this;
        }

        public Builder withFallbackModel(String fallbackModel) {
            this.fallbackModel = Objects.requireNonNull(fallbackModel, "fallbackModel");
            return this;
        }

        public Builder withTemperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder withMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder withTimeout(Duration timeout) {
            this.timeout = Objects.requireNonNull(timeout, "timeout");
            return this;
        }

        public Builder withMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder withRetryDelay(Duration retryDelay) {
            this.retryDelay = Objects.requireNonNull(retryDelay, "retryDelay");
            return this;
        }

        public Builder withEnableCaching(boolean enableCaching) {
            this.enableCaching = enableCaching;
            return this;
        }

        public Builder withCacheExpiration(Duration cacheExpiration) {
            this.cacheExpiration = Objects.requireNonNull(cacheExpiration, "cacheExpiration");
            return this;
        }

        public Builder withMaxCacheSize(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
            return this;
        }

        public Builder withEnableOptimization(boolean enableOptimization) {
            this.enableOptimization = enableOptimization;
            return this;
        }

        public Builder withOptimizationSettings(Map<String, Object> optimizationSettings) {
            this.optimizationSettings.clear();
            this.optimizationSettings.putAll(Objects.requireNonNull(optimizationSettings, "optimizationSettings"));
            return this;
        }

        public Builder withOptimizationSetting(String key, Object value) {
            this.optimizationSettings.put(Objects.requireNonNull(key, "key"), value);
            return this;
        }

        public Builder withEnableSecurity(boolean enableSecurity) {
            this.enableSecurity = enableSecurity;
            return this;
        }

        public Builder withSecuritySettings(Map<String, Object> securitySettings) {
            this.securitySettings.clear();
            this.securitySettings.putAll(Objects.requireNonNull(securitySettings, "securitySettings"));
            return this;
        }

        public Builder withSecuritySetting(String key, Object value) {
            this.securitySettings.put(Objects.requireNonNull(key, "key"), value);
            return this;
        }

        public Builder withEnableMonitoring(boolean enableMonitoring) {
            this.enableMonitoring = enableMonitoring;
            return this;
        }

        public Builder withMonitoringSettings(Map<String, Object> monitoringSettings) {
            this.monitoringSettings.clear();
            this.monitoringSettings.putAll(Objects.requireNonNull(monitoringSettings, "monitoringSettings"));
            return this;
        }

        public Builder withMonitoringSetting(String key, Object value) {
            this.monitoringSettings.put(Objects.requireNonNull(key, "key"), value);
            return this;
        }

        public Builder withPromptTemplates(Map<String, String> promptTemplates) {
            this.promptTemplates.clear();
            this.promptTemplates.putAll(Objects.requireNonNull(promptTemplates, "promptTemplates"));
            return this;
        }

        public Builder withPromptTemplate(String key, String value) {
            this.promptTemplates.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
            return this;
        }

        public AgentModelConfiguration build() {
            validate();
            return new AgentModelConfiguration(this);
        }

        private void validate() {
            if (id.isBlank()) {
                throw new IllegalArgumentException("id must not be blank");
            }
            if (name.isBlank()) {
                throw new IllegalArgumentException("name must not be blank");
            }
            if (version.isBlank()) {
                throw new IllegalArgumentException("version must not be blank");
            }
            if (agentId.isBlank()) {
                throw new IllegalArgumentException("agentId must not be blank");
            }
            if (preferredModel.isBlank()) {
                throw new IllegalArgumentException("preferredModel must not be blank");
            }
            if (fallbackModel.isBlank()) {
                throw new IllegalArgumentException("fallbackModel must not be blank");
            }
            if (temperature < 0.0 || temperature > 2.0) {
                throw new IllegalArgumentException("temperature must be between 0.0 and 2.0");
            }
            if (maxTokens <= 0) {
                throw new IllegalArgumentException("maxTokens must be positive");
            }
            if (maxRetries < 0) {
                throw new IllegalArgumentException("maxRetries must be non-negative");
            }
            if (maxCacheSize < 0) {
                throw new IllegalArgumentException("maxCacheSize must be non-negative");
            }
        }
    }
}
