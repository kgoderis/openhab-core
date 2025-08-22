package org.openhab.core.ai.config;

/**
 * Exception thrown when configuration validation fails.
 * 
 * This exception is used to indicate that a configuration value is invalid,
 * missing, or otherwise fails validation requirements.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
public class ConfigurationException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new ConfigurationException with the specified detail message.
     * 
     * @param message the detail message
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs a new ConfigurationException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
