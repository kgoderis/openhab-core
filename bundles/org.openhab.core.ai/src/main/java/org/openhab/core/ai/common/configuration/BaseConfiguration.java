package org.openhab.core.ai.common.configuration;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base configuration class implementing the Configuration interface.
 * 
 * This class provides common functionality for all configuration classes
 * in the openHAB AI bundle, including basic properties and utility methods.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class BaseConfiguration implements Configuration {

    private final String id;
    private final boolean enabled;
    private final String name;
    private final String version;
    private final Map<String, Object> customOptions;

    /**
     * Protected constructor for subclasses.
     * 
     * @param id Configuration identifier
     * @param enabled Whether the configuration is enabled
     * @param name Configuration name
     * @param version Configuration version
     * @param customOptions Custom configuration options
     */
    protected BaseConfiguration(String id, boolean enabled, String name, String version,
            @Nullable Map<String, Object> customOptions) {
        this.id = Objects.requireNonNull(id, "Configuration ID cannot be null");
        this.enabled = enabled;
        this.name = Objects.requireNonNull(name, "Configuration name cannot be null");
        this.version = Objects.requireNonNull(version, "Configuration version cannot be null");
        this.customOptions = customOptions != null ? Map.copyOf(customOptions) : Map.of();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Map<String, Object> getCustomOptions() {
        return customOptions;
    }

    @Override
    public @Nullable Object getCustomOption(String key) {
        return customOptions.get(key);
    }

    @Override
    public boolean hasCustomOption(String key) {
        return customOptions.containsKey(key);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BaseConfiguration other = (BaseConfiguration) obj;
        return Objects.equals(id, other.id) && enabled == other.enabled && Objects.equals(name, other.name)
                && Objects.equals(version, other.version) && Objects.equals(customOptions, other.customOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, enabled, name, version, customOptions);
    }

    @Override
    public String toString() {
        return String.format("BaseConfiguration{id='%s', name='%s', version='%s', enabled=%s}", id, name, version,
                enabled);
    }
}
