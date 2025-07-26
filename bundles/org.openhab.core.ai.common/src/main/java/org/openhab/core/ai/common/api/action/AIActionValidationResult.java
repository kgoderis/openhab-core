package org.openhab.core.ai.common.api.action;

import java.util.List;
import java.util.Map;

/**
 * Result of parameter validation for AI actions.
 * 
 * 
 */
public class AIActionValidationResult {

    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;
    private final Map<String, Object> sanitizedParameters;

    private AIActionValidationResult(boolean valid, List<String> errors, List<String> warnings,
            Map<String, Object> sanitizedParameters) {
        this.valid = valid;
        this.errors = errors != null ? errors : List.of();
        this.warnings = warnings != null ? warnings : List.of();
        this.sanitizedParameters = sanitizedParameters != null ? sanitizedParameters : Map.of();
    }

    /**
     * Create a valid validation result.
     * 
     * @param sanitizedParameters the sanitized parameters
     * @return the validation result
     */
    public static AIActionValidationResult valid(Map<String, Object> sanitizedParameters) {
        return new AIActionValidationResult(true, List.of(), List.of(), sanitizedParameters);
    }

    /**
     * Create a valid validation result with warnings.
     * 
     * @param sanitizedParameters the sanitized parameters
     * @param warnings the warnings
     * @return the validation result
     */
    public static AIActionValidationResult validWithWarnings(Map<String, Object> sanitizedParameters,
            List<String> warnings) {
        return new AIActionValidationResult(true, List.of(), warnings, sanitizedParameters);
    }

    /**
     * Create an invalid validation result.
     * 
     * @param errors the validation errors
     * @return the validation result
     */
    public static AIActionValidationResult invalid(List<String> errors) {
        return new AIActionValidationResult(false, errors, List.of(), Map.of());
    }

    /**
     * Create an invalid validation result with warnings.
     * 
     * @param errors the validation errors
     * @param warnings the warnings
     * @return the validation result
     */
    public static AIActionValidationResult invalidWithWarnings(List<String> errors, List<String> warnings) {
        return new AIActionValidationResult(false, errors, warnings, Map.of());
    }

    // Getters
    public boolean isValid() {
        return valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public Map<String, Object> getSanitizedParameters() {
        return sanitizedParameters;
    }

    @Override
    public String toString() {
        return String.format("AIActionValidationResult{valid=%s, errors=%d, warnings=%d}", valid, errors.size(),
                warnings.size());
    }
}
