package org.openhab.core.ai.common.validation;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Tool-specific validation result implementation.
 * 
 * <p>
 * This class provides validation results specific to tool operations,
 * including caching support and tool-specific validation details.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolValidationResult extends BaseValidationResult {

    /**
     * Constructor for ToolValidationResult.
     * 
     * @param valid Whether the validation was successful
     * @param errors List of error messages
     * @param warnings List of warning messages
     * @param details Additional validation details
     * @param validationTime Timestamp of the validation
     */
    public ToolValidationResult(boolean valid, List<String> errors, List<String> warnings, Map<String, Object> details,
            @Nullable Instant validationTime) {
        super(valid, errors, warnings, details, validationTime);
    }

    /**
     * Create a valid validation result.
     * 
     * @return valid validation result
     */
    public static ToolValidationResult valid() {
        return new ToolValidationResult(true, List.of(), List.of(), Map.of(), Instant.now());
    }

    /**
     * Create an invalid validation result.
     * 
     * @param errors List of error messages
     * @return invalid validation result
     */
    public static ToolValidationResult invalid(List<String> errors) {
        return new ToolValidationResult(false, errors, List.of(), Map.of(), Instant.now());
    }

    /**
     * Create a validation result with warnings.
     * 
     * @param warnings List of warning messages
     * @return validation result with warnings
     */
    public static ToolValidationResult withWarnings(List<String> warnings) {
        return new ToolValidationResult(true, List.of(), warnings, Map.of(), Instant.now());
    }
}
