package org.openhab.core.ai.common.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.configuration.ServerConfiguration;

/**
 * Builder for ServerConfiguration.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ServerConfigurationBuilder extends CommunicationBuilder<ServerConfiguration> {

    String host = "localhost";
    int port = 8080;
    @Nullable
    String protocol;
    @Nullable
    Map<String, Object> settings;
    boolean enabled = true;
    @Nullable
    String name;
    @Nullable
    String version;
    @Nullable
    String description;

    /**
     * Create a new ServerConfigurationBuilder.
     */
    public ServerConfigurationBuilder() {
        super();
    }

    /**
     * Create a new ServerConfigurationBuilder with default values.
     * 
     * @return a new builder instance
     */
    public static ServerConfigurationBuilder builder() {
        return new ServerConfigurationBuilder();
    }

    /**
     * Set the host.
     * 
     * @param host the host
     * @return this builder
     */
    public ServerConfigurationBuilder withHost(String host) {
        this.host = Objects.requireNonNull(host, "Host cannot be null");
        return this;
    }

    /**
     * Set the port.
     * 
     * @param port the port
     * @return this builder
     */
    public ServerConfigurationBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Set the protocol.
     * 
     * @param protocol the protocol
     * @return this builder
     */
    public ServerConfigurationBuilder withProtocol(@Nullable String protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Set the settings.
     * 
     * @param settings the settings
     * @return this builder
     */
    public ServerConfigurationBuilder withSettings(@Nullable Map<String, Object> settings) {
        this.settings = settings;
        return this;
    }

    /**
     * Set whether the server is enabled.
     * 
     * @param enabled whether the server is enabled
     * @return this builder
     */
    public ServerConfigurationBuilder withEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Set the name.
     * 
     * @param name the name
     * @return this builder
     */
    public ServerConfigurationBuilder withName(@Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Set the version.
     * 
     * @param version the version
     * @return this builder
     */
    public ServerConfigurationBuilder withVersion(@Nullable String version) {
        this.version = version;
        return this;
    }

    /**
     * Set the transport type.
     * 
     * @param transportType the transport type
     * @return this builder
     */
    public ServerConfigurationBuilder withTransportType(@Nullable String transportType) {
        // Store transport type in settings map
        if (settings == null) {
            settings = new HashMap<>();
        }
        settings.put("transportType", transportType);
        return this;
    }

    /**
     * Set the description.
     * 
     * @param description the description
     * @return this builder
     */
    public ServerConfigurationBuilder withDescription(@Nullable String description) {
        this.description = description;
        return this;
    }

    /**
     * Add a setting to the configuration.
     * 
     * @param key the setting key
     * @param value the setting value
     * @return this builder
     */
    public ServerConfigurationBuilder withSetting(String key, Object value) {
        if (settings == null) {
            settings = new HashMap<>();
        }
        settings.put(Objects.requireNonNull(key, "Setting key cannot be null"), value);
        return this;
    }

    @Override
    public ServerConfiguration build() {
        validate();
        return new ServerConfiguration(this);
    }

    @Override
    protected void validate() {
        if (host.isBlank()) {
            throw new IllegalArgumentException("Host cannot be blank");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
    }

    @Override
    protected void doReset() {
        host = "localhost";
        port = 8080;
        protocol = null;
        settings = null;
        enabled = true;
        name = null;
        version = null;
        description = null;
    }

    // Getter methods for ServerConfiguration constructor
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public @Nullable String getProtocol() {
        return protocol;
    }

    public @Nullable Map<String, Object> getSettings() {
        return settings;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public @Nullable String getName() {
        return name;
    }

    public @Nullable String getVersion() {
        return version;
    }

    public @Nullable String getDescription() {
        return description;
    }
}
