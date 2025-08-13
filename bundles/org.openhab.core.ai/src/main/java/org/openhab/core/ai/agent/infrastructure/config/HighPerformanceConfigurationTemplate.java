package org.openhab.core.ai.agent.infrastructure.config;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.infrastructure.config.api.ConfigurationTemplate;

@NonNullByDefault
public class HighPerformanceConfigurationTemplate implements ConfigurationTemplate {
    @Override
    public String getTemplateId() { return "high-performance"; }

    @Override
    public String getDescription() { return "High-performance configuration template optimized for speed"; }

    @Override
    public CommunicationConfig createConfiguration(String configId, Map<String, Object> parameters) {
        CommunicationConfig config = new CommunicationConfig(configId, "1.0.0");
        config.setDescription("High-performance configuration created from template");

        MessagingConfig messaging = new MessagingConfig();
        messaging.setMaxMessageSize(2 * 1024 * 1024);
        messaging.setTimeoutMs(15000);
        messaging.setEnableRetry(false);
        messaging.setMaxRetries(0);
        config.setMessagingConfig(messaging);

        SecurityConfig security = new SecurityConfig();
        security.setEncryptionEnabled(false);
        security.setAuthenticationRequired(false);
        config.setSecurityConfig(security);

        PerformanceConfig performance = new PerformanceConfig();
        performance.setMaxConcurrentConnections(50);
        performance.setConnectionTimeoutMs(2000);
        performance.setThreadPoolSize(16);
        performance.setEnableCompression(false);
        config.setPerformanceConfig(performance);

        return config;
    }
}


