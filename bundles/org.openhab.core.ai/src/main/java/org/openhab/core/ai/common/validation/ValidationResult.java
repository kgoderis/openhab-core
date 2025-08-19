package org.openhab.core.ai.common.validation;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base interface for all validation results in the openHAB AI system.
 * 
 * <p>
 * This interface provides a unified contract for all validation result objects,
 * ensuring consistent behavior across different validation domains.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ValidationResult {

    /**
     * Check if the validation was successful.
     * 
     * @return true if valid, false otherwise
     */
    boolean isValid();

    /**
     * Get the list of error messages.
     * 
     * @return list of error messages
     */
    List<String> getErrors();

    /**
     * Get the list of warning messages.
     * 
     * @return list of warning messages
     */
    List<String> getWarnings();

    /**
     * Get additional validation details.
     * 
     * @return validation details
     */
    Map<String, Object> getDetails();

    /**
     * Get the timestamp of the validation.
     * 
     * @return validation time, or null if not set
     */
    @Nullable
    Instant getValidationTime();

    /**
     * Get the number of errors.
     * 
     * @return number of errors
     */
    default int getErrorCount() {
        return getErrors().size();
    }

    /**
     * Get the number of warnings.
     * 
     * @return number of warnings
     */
    default int getWarningCount() {
        return getWarnings().size();
    }

    /**
     * Check if there are any errors.
     * 
     * @return true if there are errors, false otherwise
     */
    default boolean hasErrors() {
        return !getErrors().isEmpty();
    }

    /**
     * Check if there are any warnings.
     * 
     * @return true if there are warnings, false otherwise
     */
    default boolean hasWarnings() {
        return !getWarnings().isEmpty();
    }

    /**
     * Get a summary of the validation result.
     * 
     * @return validation summary
     */
    default String getSummary() {
        if (isValid()) {
            if (hasWarnings()) {
                return String.format("Valid with %d warning(s)", getWarningCount());
            } else {
                return "Valid";
            }
        } else {
            return String.format("Invalid with %d error(s)", getErrorCount());
        }
    }
}
