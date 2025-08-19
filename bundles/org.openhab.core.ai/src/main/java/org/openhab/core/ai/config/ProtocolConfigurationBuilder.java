package org.openhab.core.ai.config;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.AbstractBuilder;

/**
 * Builder for {@link ProtocolConfiguration}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ProtocolConfigurationBuilder extends AbstractBuilder<ProtocolConfiguration> {
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

    @Override
    public ProtocolConfiguration build() {
        return new ProtocolConfiguration(protocolName, enabled, endpoint, authenticationConfig, protocolSpecificConfig,
                timeoutSeconds, retryAttempts);
    }

    @Override
    protected void validate() {
        validateRequiredString(protocolName, "protocolName");
        validatePositive(timeoutSeconds, "timeoutSeconds");
        validateNonNegative(retryAttempts, "retryAttempts");
    }

    @Override
    protected void doReset() {
        // Note: protocolName is final, so we don't reset it
        enabled = true;
        endpoint = null;
        authenticationConfig = Map.of();
        protocolSpecificConfig = Map.of();
        timeoutSeconds = 30;
        retryAttempts = 3;
    }
}
