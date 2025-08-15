package org.openhab.core.ai.tool.resources.api.validation;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Resource Validation Result for MCP Resources
 * 
 * This class defines validation results for MCP resource parameters
 * including validation status and error messages.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ResourceValidationResult {

    private final boolean valid;
    private final String message;

    /**
     * Create a new ResourceValidationResult instance
     * 
     * @param valid Whether the validation passed
     * @param message The validation message
     */
    public ResourceValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    /**
     * Create a successful validation result
     * 
     * @return A successful validation result
     */
    public static ResourceValidationResult success() {
        return new ResourceValidationResult(true, "Validation successful");
    }

    /**
     * Create a failed validation result
     * 
     * @param message The error message
     * @return A failed validation result
     */
    public static ResourceValidationResult failure(String message) {
        return new ResourceValidationResult(false, message);
    }

    /**
     * Check if the validation passed
     * 
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Get the validation message
     * 
     * @return The validation message
     */
    public String getMessage() {
        return message;
    }
}
