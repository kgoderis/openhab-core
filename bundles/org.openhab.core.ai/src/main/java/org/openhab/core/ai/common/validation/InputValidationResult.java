package org.openhab.core.ai.common.validation;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified validation result for input validation operations.
 * 
 * <p>
 * This class consolidates input validation result functionality from across the
 * openHAB AI system, providing a consistent interface for input validation
 * operations in reasoning, tool elicitation, and other domains.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class InputValidationResult extends BaseValidationResult {

    private final @Nullable String reason;

    /**
     * Create a new input validation result.
     * 
     * @param valid whether the input is valid
     * @param errors list of error messages
     * @param warnings list of warning messages
     * @param details additional validation details
     * @param validationTime timestamp of the validation
     * @param reason optional reason for validation result
     */
    private InputValidationResult(boolean valid, List<String> errors, List<String> warnings,
            Map<String, Object> details, @Nullable Instant validationTime, @Nullable String reason) {
        super(valid, errors, warnings, details, validationTime);
        this.reason = reason;
    }

    /**
     * Create a valid input validation result.
     * 
     * @return valid input validation result
     */
    public static InputValidationResult valid() {
        return new InputValidationResult(true, List.of(), List.of(), Map.of(), Instant.now(), null);
    }

    /**
     * Create an invalid input validation result with a reason.
     * 
     * @param reason reason for invalidity
     * @return invalid input validation result
     */
    public static InputValidationResult invalid(String reason) {
        return new InputValidationResult(false, List.of(reason), List.of(), Map.of(), Instant.now(), reason);
    }

    /**
     * Create an invalid input validation result with multiple errors.
     * 
     * @param errors list of error messages
     * @return invalid input validation result
     */
    public static InputValidationResult invalid(List<String> errors) {
        return new InputValidationResult(false, errors, List.of(), Map.of(), Instant.now(), null);
    }

    /**
     * Create an input validation result with warnings.
     * 
     * @param warnings list of warning messages
     * @return input validation result with warnings
     */
    public static InputValidationResult withWarnings(List<String> warnings) {
        return new InputValidationResult(true, List.of(), warnings, Map.of(), Instant.now(), null);
    }

    /**
     * Create an input validation result with details.
     * 
     * @param details additional validation details
     * @return input validation result with details
     */
    public static InputValidationResult withDetails(Map<String, Object> details) {
        return new InputValidationResult(true, List.of(), List.of(), details, Instant.now(), null);
    }

    /**
     * Get the reason for the validation result.
     * 
     * @return reason for validation result, or null if not set
     */
    public @Nullable String getReason() {
        return reason;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        InputValidationResult other = (InputValidationResult) obj;
        return Objects.equals(reason, other.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), reason);
    }

    @Override
    public String toString() {
        return String.format(
                "InputValidationResult{valid=%s, errors=%s, warnings=%s, details=%s, validationTime=%s, reason=%s}",
                isValid(), getErrors(), getWarnings(), getDetails(), getValidationTime(), reason);
    }
}
