package org.openhab.core.ai.agent.lifecycle.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration change listener interface.
 *
 * This interface defines the contract for configuration change listeners that can be
 * notified when configuration values change.
 *
 * Author: Karel Goderis - Initial Contribution
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
}
