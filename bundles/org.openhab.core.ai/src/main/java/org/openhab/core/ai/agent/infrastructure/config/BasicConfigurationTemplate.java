package org.openhab.core.ai.agent.infrastructure.config;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.infrastructure.config.api.ConfigurationTemplate;

@NonNullByDefault
public class BasicConfigurationTemplate implements ConfigurationTemplate {
    @Override
    public String getTemplateId() {
        return "basic";
    }

    @Override
    public String getDescription() {
        return "Basic configuration template for simple agent communication";
    }

    @Override
    public CommunicationConfig createConfiguration(String configId, Map<String, Object> parameters) {
        CommunicationConfig config = new CommunicationConfig(configId, "1.0.0");
        config.setDescription("Basic configuration created from template");

        MessagingConfig messaging = new MessagingConfig();
        messaging.setMaxMessageSize(1024 * 1024);
        messaging.setTimeoutMs(30000);
        messaging.setEnableRetry(true);
        messaging.setMaxRetries(3);
        config.setMessagingConfig(messaging);

        SecurityConfig security = new SecurityConfig();
        security.setEncryptionEnabled(false);
        security.setAuthenticationRequired(false);
        config.setSecurityConfig(security);

        PerformanceConfig performance = new PerformanceConfig();
        performance.setMaxConcurrentConnections(10);
        performance.setConnectionTimeoutMs(5000);
        performance.setThreadPoolSize(4);
        performance.setEnableCompression(false);
        config.setPerformanceConfig(performance);

        return config;
    }
}
