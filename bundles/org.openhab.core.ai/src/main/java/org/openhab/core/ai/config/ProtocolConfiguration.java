package org.openhab.core.ai.config;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.configuration.BaseConfiguration;

/**
 * Configuration for a specific AI protocol (MCP or A2A).
 * 
 * This class encapsulates protocol-specific configuration settings
 * including endpoints, authentication, and protocol-specific options.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ProtocolConfiguration extends BaseConfiguration {

    private final @Nullable String endpoint;
    private final Map<String, String> authenticationConfig;
    private final Map<String, Object> protocolSpecificConfig;
    private final int timeoutSeconds;
    private final int retryAttempts;

    /**
     * Create a new AI protocol configuration.
     * 
     * @param protocolName The name of the protocol ("mcp" or "a2a")
     * @param enabled Whether the protocol is enabled
     * @param endpoint The protocol endpoint URL
     * @param authenticationConfig Authentication configuration
     * @param protocolSpecificConfig Protocol-specific configuration options
     * @param timeoutSeconds Request timeout in seconds
     * @param retryAttempts Number of retry attempts for failed requests
     */
    public ProtocolConfiguration(String protocolName, boolean enabled, @Nullable String endpoint,
            Map<String, String> authenticationConfig, Map<String, Object> protocolSpecificConfig, int timeoutSeconds,
            int retryAttempts) {
        super(protocolName, enabled, protocolName, "1.0.0",
                createCustomOptions(authenticationConfig, protocolSpecificConfig));
        this.endpoint = endpoint;
        this.authenticationConfig = authenticationConfig != null ? Map.copyOf(authenticationConfig) : Map.of();
        this.protocolSpecificConfig = protocolSpecificConfig != null ? Map.copyOf(protocolSpecificConfig) : Map.of();
        this.timeoutSeconds = Math.max(1, timeoutSeconds);
        this.retryAttempts = Math.max(0, retryAttempts);
    }

    /**
     * Create custom options map from authentication and protocol-specific configs.
     */
    private static Map<String, Object> createCustomOptions(Map<String, String> authenticationConfig,
            Map<String, Object> protocolSpecificConfig) {
        Map<String, Object> customOptions = new java.util.HashMap<>();
        if (authenticationConfig != null) {
            customOptions.putAll(authenticationConfig);
        }
        if (protocolSpecificConfig != null) {
            customOptions.putAll(protocolSpecificConfig);
        }
        return customOptions;
    }

    /**
     * Get the protocol name.
     * 
     * @return Protocol name
     */
    public String getProtocolName() {
        return getId();
    }

    /**
     * Check if the protocol is enabled.
     * 
     * @return true if enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return super.isEnabled();
    }

    /**
     * Get the protocol endpoint.
     * 
     * @return Endpoint URL, or null if not configured
     */
    public @Nullable String getEndpoint() {
        return endpoint;
    }

    /**
     * Get authentication configuration.
     * 
     * @return Immutable map of authentication configuration
     */
    public Map<String, String> getAuthenticationConfig() {
        return authenticationConfig;
    }

    /**
     * Get a specific authentication configuration value.
     * 
     * @param key The configuration key
     * @return The configuration value, or empty if not found
     */
    public Optional<String> getAuthenticationValue(String key) {
        return Optional.ofNullable(authenticationConfig.get(key));
    }

    /**
     * Get protocol-specific configuration.
     * 
     * @return Immutable map of protocol-specific configuration
     */
    public Map<String, Object> getProtocolSpecificConfig() {
        return protocolSpecificConfig;
    }

    /**
     * Get a specific protocol configuration value.
     * 
     * @param key The configuration key
     * @return The configuration value, or empty if not found
     */
    public Optional<Object> getProtocolValue(String key) {
        return Optional.ofNullable(protocolSpecificConfig.get(key));
    }

    /**
     * Get a typed protocol configuration value.
     * 
     * @param <T> The type to cast to
     * @param key The configuration key
     * @param type The target type
     * @return The typed configuration value, or empty if not found or wrong type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getProtocolValue(String key, Class<T> type) {
        Object value = protocolSpecificConfig.get(key);
        if (value != null && type.isInstance(value)) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }

    /**
     * Get the request timeout in seconds.
     * 
     * @return Timeout in seconds
     */
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    /**
     * Get the number of retry attempts.
     * 
     * @return Number of retry attempts
     */
    public int getRetryAttempts() {
        return retryAttempts;
    }

    /**
     * Create a builder for this configuration.
     * 
     * @param protocolName The protocol name
     * @return A new builder instance
     */
    public static ProtocolConfigurationBuilder builder(String protocolName) {
        return new ProtocolConfigurationBuilder(protocolName);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        ProtocolConfiguration that = (ProtocolConfiguration) o;
        return timeoutSeconds == that.timeoutSeconds && retryAttempts == that.retryAttempts
                && Objects.equals(endpoint, that.endpoint)
                && Objects.equals(authenticationConfig, that.authenticationConfig)
                && Objects.equals(protocolSpecificConfig, that.protocolSpecificConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), endpoint, authenticationConfig, protocolSpecificConfig, timeoutSeconds,
                retryAttempts);
    }

    @Override
    public String toString() {
        String endpointStr = endpoint != null ? endpoint : "null";
        return "ProtocolConfiguration{" + "protocolName='" + getProtocolName() + '\'' + ", enabled=" + isEnabled()
                + ", endpoint='" + endpointStr + '\'' + ", timeoutSeconds=" + timeoutSeconds + ", retryAttempts="
                + retryAttempts + ", authConfigSize=" + authenticationConfig.size() + ", protocolConfigSize="
                + protocolSpecificConfig.size() + '}';
    }

    // Inner Builder extracted to top-level: org.openhab.core.ai.config.ProtocolConfigurationBuilder
}
