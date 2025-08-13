package org.openhab.core.ai.agent.infrastructure.config.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.infrastructure.config.CommunicationConfig;

/**
 * Interface for configuration presets.
 *
 * This interface defines the contract for configuration presets that can be applied
 * to existing configurations to modify their settings for specific environments
 * or use cases.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface ConfigurationPreset {

    /**
     * Get the preset ID.
     * 
     * @return The preset identifier
     */
    String getPresetId();

    /**
     * Get the preset description.
     * 
     * @return The preset description
     */
    String getDescription();

    /**
     * Apply this preset to an existing configuration.
     * 
     * @param config The configuration to apply the preset to
     * @return The modified configuration
     */
    CommunicationConfig applyTo(CommunicationConfig config);
}
