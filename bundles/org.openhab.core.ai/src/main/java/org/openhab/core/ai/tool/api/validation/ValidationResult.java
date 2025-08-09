package org.openhab.core.ai.tool.api.validation;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of tool validation operations.
 * 
 * This class encapsulates the result of validation operations, including
 * success status, error messages, and validation details.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ValidationResult {

    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;
    private final Map<String, Object> details;

    /**
     * Create a new validation result.
     * 
     * @param valid whether the validation was successful
     * @param errors list of error messages
     * @param warnings list of warning messages
     * @param details additional validation details
     */
    public ValidationResult(boolean valid, List<String> errors, List<String> warnings, Map<String, Object> details) {
        this.valid = valid;
        this.errors = errors;
        this.warnings = warnings;
        this.details = details;
    }

    /**
     * Create a valid validation result.
     * 
     * @return valid validation result
     */
    public static ValidationResult valid() {
        return new ValidationResult(true, List.of(), List.of(), Map.of());
    }

    /**
     * Create an invalid validation result.
     * 
     * @param errors list of error messages
     * @return invalid validation result
     */
    public static ValidationResult invalid(List<String> errors) {
        return new ValidationResult(false, errors, List.of(), Map.of());
    }

    /**
     * Check if the validation was successful.
     * 
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Get the list of error messages.
     * 
     * @return list of error messages
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Get the list of warning messages.
     * 
     * @return list of warning messages
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Get additional validation details.
     * 
     * @return validation details
     */
    public Map<String, Object> getDetails() {
        return details;
    }

    // TODO: Implement validation result caching
    // TODO: Add support for validation result serialization
    // TODO: Implement validation result comparison methods
    // TODO: Add support for validation result metrics
}
