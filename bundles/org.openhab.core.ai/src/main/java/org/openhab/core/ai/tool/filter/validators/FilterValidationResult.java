package org.openhab.core.ai.tool.filter.validators;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of filter validation operations.
 * 
 * This class encapsulates the result of filter validation operations, including
 * success status, error messages, and validation details.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class FilterValidationResult {

    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;
    private final Map<String, Object> details;

    /**
     * Create a new filter validation result.
     * 
     * @param valid whether the filter is valid
     * @param errors list of error messages
     * @param warnings list of warning messages
     * @param details additional validation details
     */
    public FilterValidationResult(boolean valid, List<String> errors, List<String> warnings,
            Map<String, Object> details) {
        this.valid = valid;
        this.errors = errors;
        this.warnings = warnings;
        this.details = details;
    }

    /**
     * Create a valid filter validation result.
     * 
     * @return valid filter validation result
     */
    public static FilterValidationResult valid() {
        return new FilterValidationResult(true, List.of(), List.of(), Map.of());
    }

    /**
     * Create an invalid filter validation result.
     * 
     * @param errors list of error messages
     * @return invalid filter validation result
     */
    public static FilterValidationResult invalid(List<String> errors) {
        return new FilterValidationResult(false, errors, List.of(), Map.of());
    }

    /**
     * Check if the filter is valid.
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

    // TODO: Implement filter validation result caching
    // TODO: Add support for filter validation result serialization
    // TODO: Implement filter validation result comparison
    // TODO: Add support for filter validation result metrics
}
