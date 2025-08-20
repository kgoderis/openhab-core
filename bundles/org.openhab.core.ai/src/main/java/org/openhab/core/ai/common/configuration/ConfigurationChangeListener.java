package org.openhab.core.ai.common.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified configuration change listener interface for all AI system components.
 * 
 * This interface provides comprehensive configuration change notification capabilities
 * including general configuration changes, protocol-specific changes, and lifecycle events.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ConfigurationChangeListener {

    /**
     * Called when any configuration has changed.
     * Default implementation does nothing.
     */
    default void onConfigurationChanged() {
        // Default implementation does nothing
    }

    /**
     * Called when a specific configuration key has changed.
     * Default implementation calls the no-parameter version.
     * 
     * @param key The configuration key that changed
     * @param oldValue The previous value (may be null)
     * @param newValue The new value
     */
    default void onConfigurationChanged(String key, @Nullable Object oldValue, Object newValue) {
        // Default implementation calls the no-parameter version
        onConfigurationChanged();
    }

    /**
     * Called when a configuration value changes (string-based).
     * 
     * @param key The configuration key that changed
     * @param oldValue The previous value (null if key was added)
     * @param newValue The new value (null if key was removed)
     */
    default void onConfigurationChanged(String key, @Nullable String oldValue, @Nullable String newValue) {
        onConfigurationChanged(key, (Object) oldValue, newValue);
    }

    /**
     * Called when a protocol configuration changes.
     * 
     * @param protocolName The name of the protocol that changed
     * @param oldConfiguration The previous configuration (null if protocol was added)
     * @param newConfiguration The new configuration (null if protocol was removed)
     */
    default void onProtocolConfigurationChanged(String protocolName, @Nullable Object oldConfiguration,
            @Nullable Object newConfiguration) {
        // Default implementation does nothing
    }

    /**
     * Called when the entire configuration is reloaded.
     */
    default void onConfigurationReloaded() {
        // Default implementation does nothing
    }

    /**
     * Called when a configuration section changes.
     * 
     * @param section The configuration section that changed
     * @param oldConfiguration The previous configuration for the section
     * @param newConfiguration The new configuration for the section
     */
    default void onConfigurationSectionChanged(String section, @Nullable Object oldConfiguration,
            @Nullable Object newConfiguration) {
        // Default implementation does nothing
    }

    /**
     * Called when configuration validation fails.
     * 
     * @param key The configuration key that failed validation
     * @param value The invalid value
     * @param reason The reason for validation failure
     */
    default void onConfigurationValidationFailed(String key, @Nullable Object value, String reason) {
        // Default implementation does nothing
    }

    /**
     * Get the name of this listener (for identification purposes).
     * 
     * @return Listener name
     */
    default String getListenerName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Get the priority of this listener (lower values = higher priority).
     * 
     * @return Listener priority
     */
    default int getListenerPriority() {
        return 100; // Default priority
    }

    /**
     * Check if this listener should be notified for the given configuration key.
     * 
     * @param key The configuration key
     * @return true if this listener should be notified
     */
    default boolean shouldNotifyForKey(String key) {
        return true; // Default implementation notifies for all keys
    }
}
