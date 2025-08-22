package org.openhab.core.ai.config.repo;

/**
 * Exception thrown when model preset repository operations fail.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
public class ModelPresetRepositoryException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new ModelPresetRepositoryException with the specified message.
     * 
     * @param message the error message
     */
    public ModelPresetRepositoryException(String message) {
        super(message);
    }

    /**
     * Creates a new ModelPresetRepositoryException with the specified message and cause.
     * 
     * @param message the error message
     * @param cause the cause of the exception
     */
    public ModelPresetRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
