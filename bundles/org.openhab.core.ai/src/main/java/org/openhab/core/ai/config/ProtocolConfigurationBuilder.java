package org.openhab.core.ai.config;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class ProtocolConfigurationBuilder {
    private final String protocolName;
    private boolean enabled = true;
    private @Nullable String endpoint;
    private Map<String, String> authenticationConfig = Map.of();
    private Map<String, Object> protocolSpecificConfig = Map.of();
    private int timeoutSeconds = 30;
    private int retryAttempts = 3;

    public ProtocolConfigurationBuilder(String protocolName) {
        this.protocolName = protocolName;
    }

    public ProtocolConfigurationBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public ProtocolConfigurationBuilder endpoint(@Nullable String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public ProtocolConfigurationBuilder authenticationConfig(Map<String, String> authenticationConfig) {
        this.authenticationConfig = authenticationConfig;
        return this;
    }

    public ProtocolConfigurationBuilder protocolSpecificConfig(Map<String, Object> protocolSpecificConfig) {
        this.protocolSpecificConfig = protocolSpecificConfig;
        return this;
    }

    public ProtocolConfigurationBuilder timeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    public ProtocolConfigurationBuilder retryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
        return this;
    }

    public ProtocolConfiguration build() {
        return new ProtocolConfiguration(protocolName, enabled, endpoint, authenticationConfig, protocolSpecificConfig,
                timeoutSeconds, retryAttempts);
    }
}
