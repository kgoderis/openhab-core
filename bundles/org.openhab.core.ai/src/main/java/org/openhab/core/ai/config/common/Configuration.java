package org.openhab.core.ai.config.common;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base configuration interface for all AI components.
 * 
 * This interface provides a common foundation for all configuration classes
 * in the openHAB AI bundle, ensuring consistent patterns and behavior.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface Configuration {

    /**
     * Get the configuration identifier.
     * 
     * @return Unique identifier for this configuration
     */
    String getId();

    /**
     * Check if this configuration is enabled.
     * 
     * @return true if enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Get the configuration name.
     * 
     * @return Human-readable name for this configuration
     */
    String getName();

    /**
     * Get the configuration version.
     * 
     * @return Version string for this configuration
     */
    String getVersion();

    /**
     * Get custom configuration options.
     * 
     * @return Immutable map of custom configuration options
     */
    Map<String, Object> getCustomOptions();

    /**
     * Get a specific custom configuration option.
     * 
     * @param key The option key
     * @return The option value, or null if not found
     */
    @Nullable
    Object getCustomOption(String key);

    /**
     * Check if a custom option exists.
     * 
     * @param key The option key
     * @return true if the option exists, false otherwise
     */
    boolean hasCustomOption(String key);
}
