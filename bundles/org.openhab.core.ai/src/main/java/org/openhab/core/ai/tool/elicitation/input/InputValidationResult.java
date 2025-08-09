package org.openhab.core.ai.tool.elicitation.input;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of input validation operations.
 * 
 * This class encapsulates the result of input validation operations, including
 * success status, error messages, and validation details.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class InputValidationResult {

    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;
    private final Map<String, Object> details;

    /**
     * Create a new input validation result.
     * 
     * @param valid whether the input is valid
     * @param errors list of error messages
     * @param warnings list of warning messages
     * @param details additional validation details
     */
    public InputValidationResult(boolean valid, List<String> errors, List<String> warnings,
            Map<String, Object> details) {
        this.valid = valid;
        this.errors = errors;
        this.warnings = warnings;
        this.details = details;
    }

    /**
     * Create a valid input validation result.
     * 
     * @return valid input validation result
     */
    public static InputValidationResult valid() {
        return new InputValidationResult(true, List.of(), List.of(), Map.of());
    }

    /**
     * Create an invalid input validation result.
     * 
     * @param errors list of error messages
     * @return invalid input validation result
     */
    public static InputValidationResult invalid(List<String> errors) {
        return new InputValidationResult(false, errors, List.of(), Map.of());
    }

    /**
     * Check if the input is valid.
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

    // TODO: Implement input validation result caching
    // TODO: Add support for input validation result serialization
    // TODO: Implement input validation result comparison
    // TODO: Add support for input validation result metrics
}
