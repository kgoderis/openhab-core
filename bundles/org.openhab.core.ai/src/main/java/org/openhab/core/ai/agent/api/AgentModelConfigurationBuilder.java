package org.openhab.core.ai.agent.api;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public final class AgentModelConfigurationBuilder {
    String agentId = "";
    String preferredModel = "gpt-4";
    String fallbackModel = "gpt-3.5-turbo";
    double temperature = 0.7;
    int maxTokens = 1000;
    Duration timeout = Duration.ofSeconds(30);
    int maxRetries = 3;
    Duration retryDelay = Duration.ofSeconds(5);
    boolean enableCaching = true;
    Duration cacheExpiration = Duration.ofMinutes(30);
    int maxCacheSize = 1000;
    boolean enableOptimization = true;
    Map<String, Object> optimizationSettings = new HashMap<>();
    boolean enableSecurity = true;
    Map<String, Object> securitySettings = new HashMap<>();
    boolean enableMonitoring = true;
    Map<String, Object> monitoringSettings = new HashMap<>();
    Map<String, String> promptTemplates = new HashMap<>();

    public AgentModelConfigurationBuilder agentId(String agentId) { this.agentId = agentId; return this; }
    public AgentModelConfigurationBuilder preferredModel(String preferredModel) { this.preferredModel = preferredModel; return this; }
    public AgentModelConfigurationBuilder fallbackModel(String fallbackModel) { this.fallbackModel = fallbackModel; return this; }
    public AgentModelConfigurationBuilder temperature(double temperature) { this.temperature = temperature; return this; }
    public AgentModelConfigurationBuilder maxTokens(int maxTokens) { this.maxTokens = maxTokens; return this; }
    public AgentModelConfigurationBuilder timeout(Duration timeout) { this.timeout = timeout; return this; }
    public AgentModelConfigurationBuilder maxRetries(int maxRetries) { this.maxRetries = maxRetries; return this; }
    public AgentModelConfigurationBuilder retryDelay(Duration retryDelay) { this.retryDelay = retryDelay; return this; }
    public AgentModelConfigurationBuilder enableCaching(boolean enableCaching) { this.enableCaching = enableCaching; return this; }
    public AgentModelConfigurationBuilder cacheExpiration(Duration cacheExpiration) { this.cacheExpiration = cacheExpiration; return this; }
    public AgentModelConfigurationBuilder maxCacheSize(int maxCacheSize) { this.maxCacheSize = maxCacheSize; return this; }
    public AgentModelConfigurationBuilder enableOptimization(boolean enableOptimization) { this.enableOptimization = enableOptimization; return this; }
    public AgentModelConfigurationBuilder optimizationSettings(Map<String, Object> optimizationSettings) { this.optimizationSettings = new HashMap<>(optimizationSettings); return this; }
    public AgentModelConfigurationBuilder enableSecurity(boolean enableSecurity) { this.enableSecurity = enableSecurity; return this; }
    public AgentModelConfigurationBuilder securitySettings(Map<String, Object> securitySettings) { this.securitySettings = new HashMap<>(securitySettings); return this; }
    public AgentModelConfigurationBuilder enableMonitoring(boolean enableMonitoring) { this.enableMonitoring = enableMonitoring; return this; }
    public AgentModelConfigurationBuilder monitoringSettings(Map<String, Object> monitoringSettings) { this.monitoringSettings = new HashMap<>(monitoringSettings); return this; }
    public AgentModelConfigurationBuilder promptTemplates(Map<String, String> promptTemplates) { this.promptTemplates = new HashMap<>(promptTemplates); return this; }

    public AgentModelConfiguration build() { return new AgentModelConfiguration(this); }
}


