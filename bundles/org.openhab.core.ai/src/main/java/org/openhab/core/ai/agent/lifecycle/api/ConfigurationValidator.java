package org.openhab.core.ai.agent.lifecycle.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration validator interface.
 *
 * This interface defines the contract for configuration validators that can be used
 * to validate configuration values before they are applied.
 *
 * Author: Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface ConfigurationValidator {

    /**
     * Validate a configuration value.
     * 
     * @param value The value to validate
     * @return true if the value is valid, false otherwise
     */
    boolean validate(Object value);
}
