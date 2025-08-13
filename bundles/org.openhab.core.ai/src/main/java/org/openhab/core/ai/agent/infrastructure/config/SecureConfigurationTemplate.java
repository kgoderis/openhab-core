package org.openhab.core.ai.agent.infrastructure.config;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.infrastructure.config.api.ConfigurationTemplate;

@NonNullByDefault
public class SecureConfigurationTemplate implements ConfigurationTemplate {
    @Override
    public String getTemplateId() { return "secure"; }

    @Override
    public String getDescription() { return "Secure configuration template with encryption and authentication"; }

    @Override
    public CommunicationConfig createConfiguration(String configId, Map<String, Object> parameters) {
        CommunicationConfig config = new CommunicationConfig(configId, "1.0.0");
        config.setDescription("Secure configuration created from template");

        MessagingConfig messaging = new MessagingConfig();
        messaging.setMaxMessageSize(512 * 1024);
        messaging.setTimeoutMs(60000);
        messaging.setEnableRetry(true);
        messaging.setMaxRetries(5);
        config.setMessagingConfig(messaging);

        SecurityConfig security = new SecurityConfig();
        security.setEncryptionEnabled(true);
        security.setEncryptionKey("default-secure-key");
        security.setAuthenticationRequired(true);
        security.setAuthenticationMethod("token");
        config.setSecurityConfig(security);

        PerformanceConfig performance = new PerformanceConfig();
        performance.setMaxConcurrentConnections(5);
        performance.setConnectionTimeoutMs(10000);
        performance.setThreadPoolSize(2);
        performance.setEnableCompression(true);
        config.setPerformanceConfig(performance);

        return config;
    }
}


