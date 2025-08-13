package org.openhab.core.ai.agent.infrastructure.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.infrastructure.config.api.ConfigurationPreset;

@NonNullByDefault
public class TestingPreset implements ConfigurationPreset {
    @Override
    public String getPresetId() { return "testing"; }

    @Override
    public String getDescription() { return "Testing preset optimized for automated testing"; }

    @Override
    public CommunicationConfig applyTo(CommunicationConfig config) {
        if (config.getMessagingConfig() != null) {
            config.getMessagingConfig().setTimeoutMs(5000);
            config.getMessagingConfig().setMaxRetries(1);
        }
        if (config.getSecurityConfig() != null) {
            config.getSecurityConfig().setEncryptionEnabled(false);
            config.getSecurityConfig().setAuthenticationRequired(false);
        }
        if (config.getPerformanceConfig() != null) {
            config.getPerformanceConfig().setMaxConcurrentConnections(1);
            config.getPerformanceConfig().setThreadPoolSize(1);
            config.getPerformanceConfig().setEnableCompression(false);
        }
        return config;
    }
}


