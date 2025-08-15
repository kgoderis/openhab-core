package org.openhab.core.ai.agent.infrastructure.config;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class CommunicationConfig {
    private String configId;
    private String version;
    private String description;
    private MessagingConfig messagingConfig;
    private SecurityConfig securityConfig;
    private PerformanceConfig performanceConfig;
    private Map<String, Object> customSettings;

    public CommunicationConfig() {
    }

    public CommunicationConfig(String configId, String version) {
        this.configId = configId;
        this.version = version;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MessagingConfig getMessagingConfig() {
        return messagingConfig;
    }

    public void setMessagingConfig(MessagingConfig messagingConfig) {
        this.messagingConfig = messagingConfig;
    }

    public SecurityConfig getSecurityConfig() {
        return securityConfig;
    }

    public void setSecurityConfig(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    public PerformanceConfig getPerformanceConfig() {
        return performanceConfig;
    }

    public void setPerformanceConfig(PerformanceConfig performanceConfig) {
        this.performanceConfig = performanceConfig;
    }

    public Map<String, Object> getCustomSettings() {
        return customSettings;
    }

    public void setCustomSettings(Map<String, Object> customSettings) {
        this.customSettings = customSettings;
    }
}
