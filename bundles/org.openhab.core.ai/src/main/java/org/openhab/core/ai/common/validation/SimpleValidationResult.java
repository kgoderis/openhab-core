package org.openhab.core.ai.common.validation;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Simple concrete implementation of ValidationResult.
 * 
 * <p>
 * This class provides a basic implementation of the ValidationResult interface
 * that can be used when a simple validation result is needed.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SimpleValidationResult extends BaseValidationResult {

    /**
     * Create a new simple validation result.
     * 
     * @param valid Whether the validation was successful
     * @param errors List of error messages
     * @param warnings List of warning messages
     * @param details Additional validation details
     */
    public SimpleValidationResult(boolean valid, List<String> errors, List<String> warnings,
            Map<String, Object> details) {
        super(valid, errors, warnings, details, Instant.now());
    }

    /**
     * Create a new simple validation result with custom timestamp.
     * 
     * @param valid Whether the validation was successful
     * @param errors List of error messages
     * @param warnings List of warning messages
     * @param details Additional validation details
     * @param validationTime Timestamp of the validation
     */
    public SimpleValidationResult(boolean valid, List<String> errors, List<String> warnings,
            Map<String, Object> details, @Nullable Instant validationTime) {
        super(valid, errors, warnings, details, validationTime);
    }

    /**
     * Create a valid validation result.
     * 
     * @return a valid validation result
     */
    public static SimpleValidationResult valid() {
        return new SimpleValidationResult(true, List.of(), List.of(), Map.of());
    }

    /**
     * Create an invalid validation result with the given errors.
     * 
     * @param errors List of error messages
     * @return an invalid validation result
     */
    public static SimpleValidationResult invalid(List<String> errors) {
        return new SimpleValidationResult(false, errors, List.of(), Map.of());
    }

    /**
     * Create an invalid validation result with a single error.
     * 
     * @param error Error message
     * @return an invalid validation result
     */
    public static SimpleValidationResult invalid(String error) {
        return new SimpleValidationResult(false, List.of(error), List.of(), Map.of());
    }
}
