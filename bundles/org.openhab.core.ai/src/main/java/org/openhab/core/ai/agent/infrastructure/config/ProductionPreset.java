package org.openhab.core.ai.agent.infrastructure.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.infrastructure.config.api.ConfigurationPreset;

@NonNullByDefault
public class ProductionPreset implements ConfigurationPreset {
    @Override
    public String getPresetId() {
        return "production";
    }

    @Override
    public String getDescription() {
        return "Production preset with strict security and performance settings";
    }

    @Override
    public CommunicationConfig applyTo(CommunicationConfig config) {
        if (config.getMessagingConfig() != null) {
            config.getMessagingConfig().setTimeoutMs(30000);
            config.getMessagingConfig().setMaxRetries(3);
        }
        if (config.getSecurityConfig() != null) {
            config.getSecurityConfig().setEncryptionEnabled(true);
            config.getSecurityConfig().setAuthenticationRequired(true);
        }
        if (config.getPerformanceConfig() != null) {
            config.getPerformanceConfig().setMaxConcurrentConnections(20);
            config.getPerformanceConfig().setThreadPoolSize(8);
            config.getPerformanceConfig().setEnableCompression(true);
        }
        return config;
    }
}
