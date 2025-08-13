package org.openhab.core.ai.agent.infrastructure.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.infrastructure.config.api.ConfigurationPreset;

@NonNullByDefault
public class DevelopmentPreset implements ConfigurationPreset {
    @Override
    public String getPresetId() { return "development"; }

    @Override
    public String getDescription() { return "Development preset with relaxed settings for debugging"; }

    @Override
    public CommunicationConfig applyTo(CommunicationConfig config) {
        if (config.getMessagingConfig() != null) {
            config.getMessagingConfig().setTimeoutMs(60000);
            config.getMessagingConfig().setMaxRetries(5);
        }
        if (config.getSecurityConfig() != null) {
            config.getSecurityConfig().setEncryptionEnabled(false);
            config.getSecurityConfig().setAuthenticationRequired(false);
        }
        if (config.getPerformanceConfig() != null) {
            config.getPerformanceConfig().setMaxConcurrentConnections(5);
            config.getPerformanceConfig().setThreadPoolSize(2);
        }
        return config;
    }
}


