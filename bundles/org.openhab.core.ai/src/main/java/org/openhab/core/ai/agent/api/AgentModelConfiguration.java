package org.openhab.core.ai.agent.api;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Agent-specific model configuration settings
 * 
 * <p>
 * This class contains:
 * - Agent-specific model selection preferences
 * - Agent-specific parameter optimization settings
 * - Agent-specific prompt template configurations
 * - Agent-specific caching and performance settings
 * - Agent-specific security and access control settings
 * - Agent-specific monitoring and logging settings
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class AgentModelConfiguration {

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
        this.optimizationSettings = new HashMap<>(builder.optimizationSettings);
        this.enableSecurity = builder.enableSecurity;
        this.securitySettings = new HashMap<>(builder.securitySettings);
        this.enableMonitoring = builder.enableMonitoring;
        this.monitoringSettings = new HashMap<>(builder.monitoringSettings);
        this.promptTemplates = new HashMap<>(builder.promptTemplates);
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
        return new HashMap<>(optimizationSettings);
    }

    public boolean isEnableSecurity() {
        return enableSecurity;
    }

    public Map<String, Object> getSecuritySettings() {
        return new HashMap<>(securitySettings);
    }

    public boolean isEnableMonitoring() {
        return enableMonitoring;
    }

    public Map<String, Object> getMonitoringSettings() {
        return new HashMap<>(monitoringSettings);
    }

    public Map<String, String> getPromptTemplates() {
        return new HashMap<>(promptTemplates);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
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
        private Map<String, Object> optimizationSettings = new HashMap<>();
        private boolean enableSecurity = true;
        private Map<String, Object> securitySettings = new HashMap<>();
        private boolean enableMonitoring = true;
        private Map<String, Object> monitoringSettings = new HashMap<>();
        private Map<String, String> promptTemplates = new HashMap<>();

        public Builder agentId(String agentId) {
            this.agentId = agentId;
            return this;
        }

        public Builder preferredModel(String preferredModel) {
            this.preferredModel = preferredModel;
            return this;
        }

        public Builder fallbackModel(String fallbackModel) {
            this.fallbackModel = fallbackModel;
            return this;
        }

        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder retryDelay(Duration retryDelay) {
            this.retryDelay = retryDelay;
            return this;
        }

        public Builder enableCaching(boolean enableCaching) {
            this.enableCaching = enableCaching;
            return this;
        }

        public Builder cacheExpiration(Duration cacheExpiration) {
            this.cacheExpiration = cacheExpiration;
            return this;
        }

        public Builder maxCacheSize(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
            return this;
        }

        public Builder enableOptimization(boolean enableOptimization) {
            this.enableOptimization = enableOptimization;
            return this;
        }

        public Builder optimizationSettings(Map<String, Object> optimizationSettings) {
            this.optimizationSettings = new HashMap<>(optimizationSettings);
            return this;
        }

        public Builder enableSecurity(boolean enableSecurity) {
            this.enableSecurity = enableSecurity;
            return this;
        }

        public Builder securitySettings(Map<String, Object> securitySettings) {
            this.securitySettings = new HashMap<>(securitySettings);
            return this;
        }

        public Builder enableMonitoring(boolean enableMonitoring) {
            this.enableMonitoring = enableMonitoring;
            return this;
        }

        public Builder monitoringSettings(Map<String, Object> monitoringSettings) {
            this.monitoringSettings = new HashMap<>(monitoringSettings);
            return this;
        }

        public Builder promptTemplates(Map<String, String> promptTemplates) {
            this.promptTemplates = new HashMap<>(promptTemplates);
            return this;
        }

        public AgentModelConfiguration build() {
            return new AgentModelConfiguration(this);
        }
    }
}
