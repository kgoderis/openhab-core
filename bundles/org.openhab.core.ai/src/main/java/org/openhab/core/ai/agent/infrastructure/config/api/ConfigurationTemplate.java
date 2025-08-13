package org.openhab.core.ai.agent.infrastructure.config.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.infrastructure.config.CommunicationConfig;

/**
 * Interface for configuration templates.
 *
 * This interface defines the contract for configuration templates that can be used
 * to create new configurations with predefined settings and parameters.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface ConfigurationTemplate {

    /**
     * Get the template ID.
     * 
     * @return The template identifier
     */
    String getTemplateId();

    /**
     * Get the template description.
     * 
     * @return The template description
     */
    String getDescription();

    /**
     * Create a configuration from this template.
     * 
     * @param configId The configuration ID
     * @param parameters The parameters to use for configuration creation
     * @return The created configuration
     */
    CommunicationConfig createConfiguration(String configId, Map<String, Object> parameters);
}
