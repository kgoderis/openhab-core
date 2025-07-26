package org.openhab.core.ai.common.config;

/**
 * Listener interface for AI configuration changes.
 * 
 * Implementations of this interface will be notified when configuration
 * changes occur in the AI system, allowing for dynamic reconfiguration
 * of AI protocol services.
 * 
 * 
 */
public interface AIConfigurationChangeListener {

    /**
     * Called when a configuration value changes.
     * 
     * @param key The configuration key that changed
     * @param oldValue The previous value (null if key was added)
     * @param newValue The new value (null if key was removed)
     */
    void onConfigurationChanged(String key, String oldValue, String newValue);

    /**
     * Called when a protocol configuration changes.
     * 
     * @param protocolName The name of the protocol that changed
     * @param oldConfiguration The previous configuration (null if protocol was added)
     * @param newConfiguration The new configuration (null if protocol was removed)
     */
    void onProtocolConfigurationChanged(String protocolName, AIProtocolConfiguration oldConfiguration,
            AIProtocolConfiguration newConfiguration);

    /**
     * Called when the entire configuration is reloaded.
     */
    void onConfigurationReloaded();

    /**
     * Get the name of this listener (for identification purposes).
     * 
     * @return Listener name
     */
    default String getListenerName() {
        return this.getClass().getSimpleName();
    }
}
