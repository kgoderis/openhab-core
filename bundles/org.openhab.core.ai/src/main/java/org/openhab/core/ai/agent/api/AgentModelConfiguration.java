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

    AgentModelConfiguration(AgentModelConfigurationBuilder builder) {
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

    public static AgentModelConfigurationBuilder builder() {
        return new AgentModelConfigurationBuilder();
    }
}
