package org.openhab.core.ai.config.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

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
     * Private constructor for builder pattern.
     */
    private ProtocolConfiguration(Builder builder) {
        super(builder.protocolName, builder.enabled, builder.protocolName, "1.0.0",
                createCustomOptions(builder.authenticationConfig, builder.protocolSpecificConfig));
        this.endpoint = builder.endpoint;
        this.authenticationConfig = Map.copyOf(builder.authenticationConfig);
        this.protocolSpecificConfig = Map.copyOf(builder.protocolSpecificConfig);
        this.timeoutSeconds = Math.max(1, builder.timeoutSeconds);
        this.retryAttempts = Math.max(0, builder.retryAttempts);
    }

    /**
     * Create custom options map from authentication and protocol-specific configs.
     */
    private static Map<String, Object> createCustomOptions(Map<String, String> authenticationConfig,
            Map<String, Object> protocolSpecificConfig) {
        Map<String, Object> customOptions = new HashMap<>();
        customOptions.putAll(authenticationConfig);
        customOptions.putAll(protocolSpecificConfig);
        return customOptions;
    }

    /**
     * Create a new builder for this configuration.
     * 
     * @param protocolName The protocol name (cannot be null or blank)
     * @return A new builder instance
     */
    public static Builder builder(String protocolName) {
        return new Builder(protocolName);
    }

    /**
     * Create a new builder from this configuration.
     * 
     * @return a new builder with current values
     */
    public Builder toBuilder() {
        return new Builder(this);
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

    /**
     * Builder for ProtocolConfiguration.
     * 
     * This builder provides a fluent API for creating ProtocolConfiguration instances.
     * The builder is not thread-safe and should be used for single-threaded construction.
     * 
     * @author Karel Goderis - Initial Contribution
     * @since 1.0.0
     */
    public static final class Builder {
        private final String protocolName;
        private boolean enabled = true;
        private @Nullable String endpoint;
        private Map<String, String> authenticationConfig = new HashMap<>();
        private Map<String, Object> protocolSpecificConfig = new HashMap<>();
        private int timeoutSeconds = 30;
        private int retryAttempts = 3;

        /**
         * Create a new builder with the specified protocol name.
         * 
         * @param protocolName the protocol name (cannot be null or blank)
         */
        public Builder(String protocolName) {
            this.protocolName = Objects.requireNonNull(protocolName, "protocolName");
        }

        /**
         * Create a new builder from an existing ProtocolConfiguration.
         * 
         * @param source the source configuration to copy from
         */
        public Builder(ProtocolConfiguration source) {
            this.protocolName = source.getProtocolName();
            this.enabled = source.isEnabled();
            this.endpoint = source.endpoint;
            this.authenticationConfig = new HashMap<>(source.authenticationConfig);
            this.protocolSpecificConfig = new HashMap<>(source.protocolSpecificConfig);
            this.timeoutSeconds = source.timeoutSeconds;
            this.retryAttempts = source.retryAttempts;
        }

        /**
         * Set whether the protocol is enabled.
         * 
         * @param enabled whether the protocol is enabled
         * @return this builder
         */
        public Builder withEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Set the protocol endpoint.
         * 
         * @param endpoint the protocol endpoint URL (can be null)
         * @return this builder
         */
        public Builder withEndpoint(@Nullable String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        /**
         * Set the authentication configuration.
         * 
         * @param authenticationConfig the authentication configuration map (cannot be null)
         * @return this builder
         */
        public Builder withAuthenticationConfig(Map<String, String> authenticationConfig) {
            this.authenticationConfig.clear();
            this.authenticationConfig.putAll(Objects.requireNonNull(authenticationConfig, "authenticationConfig"));
            return this;
        }

        /**
         * Add an authentication configuration entry.
         * 
         * @param key the configuration key (cannot be null)
         * @param value the configuration value (cannot be null)
         * @return this builder
         */
        public Builder withAuthenticationEntry(String key, String value) {
            this.authenticationConfig.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
            return this;
        }

        /**
         * Set the protocol-specific configuration.
         * 
         * @param protocolSpecificConfig the protocol-specific configuration map (cannot be null)
         * @return this builder
         */
        public Builder withProtocolSpecificConfig(Map<String, Object> protocolSpecificConfig) {
            this.protocolSpecificConfig.clear();
            this.protocolSpecificConfig
                    .putAll(Objects.requireNonNull(protocolSpecificConfig, "protocolSpecificConfig"));
            return this;
        }

        /**
         * Add a protocol-specific configuration entry.
         * 
         * @param key the configuration key (cannot be null)
         * @param value the configuration value (cannot be null)
         * @return this builder
         */
        public Builder withProtocolEntry(String key, Object value) {
            this.protocolSpecificConfig.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
            return this;
        }

        /**
         * Set the request timeout in seconds.
         * 
         * @param timeoutSeconds the timeout in seconds (must be positive)
         * @return this builder
         */
        public Builder withTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        /**
         * Set the number of retry attempts.
         * 
         * @param retryAttempts the number of retry attempts (must be non-negative)
         * @return this builder
         */
        public Builder withRetryAttempts(int retryAttempts) {
            this.retryAttempts = retryAttempts;
            return this;
        }

        /**
         * Build the ProtocolConfiguration instance.
         * 
         * @return the configured ProtocolConfiguration
         * @throws IllegalArgumentException if validation fails
         */
        public ProtocolConfiguration build() {
            if (protocolName.isBlank()) {
                throw new IllegalArgumentException("protocolName must not be blank");
            }
            if (timeoutSeconds <= 0) {
                throw new IllegalArgumentException("timeoutSeconds must be positive");
            }
            if (retryAttempts < 0) {
                throw new IllegalArgumentException("retryAttempts must be non-negative");
            }
            return new ProtocolConfiguration(this);
        }
    }
}
