package org.openhab.core.ai.common.validation;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration validation result extending the base validation result.
 * 
 * <p>
 * This class provides configuration-specific validation functionality while
 * leveraging the common validation infrastructure.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConfigurationValidationResult extends BaseValidationResult {

    /**
     * Create a new configuration validation result.
     * 
     * @param valid Whether the configuration is valid
     * @param errors List of validation errors
     * @param warnings List of validation warnings
     * @param details Additional validation details
     * @param validationTime Timestamp of validation
     */
    public ConfigurationValidationResult(boolean valid, List<String> errors, List<String> warnings,
            Map<String, Object> details, Instant validationTime) {
        super(valid, errors, warnings, details, validationTime);
    }

    /**
     * Create a new configuration validation result with current timestamp.
     * 
     * @param valid Whether the configuration is valid
     * @param errors List of validation errors
     * @param warnings List of validation warnings
     * @param details Additional validation details
     */
    public ConfigurationValidationResult(boolean valid, List<String> errors, List<String> warnings,
            Map<String, Object> details) {
        super(valid, errors, warnings, details, Instant.now());
    }

    /**
     * Create a valid configuration validation result.
     * 
     * @return A valid configuration validation result
     */
    public static ConfigurationValidationResult valid() {
        return new ConfigurationValidationResult(true, List.of(), List.of(), Map.of());
    }

    /**
     * Create an invalid configuration validation result with errors.
     * 
     * @param errors List of validation errors
     * @return An invalid configuration validation result
     */
    public static ConfigurationValidationResult invalid(List<String> errors) {
        return new ConfigurationValidationResult(false, errors, List.of(), Map.of());
    }

    /**
     * Create an invalid configuration validation result with a single error.
     * 
     * @param error The validation error message
     * @return An invalid configuration validation result
     */
    public static ConfigurationValidationResult invalid(String error) {
        return new ConfigurationValidationResult(false, List.of(error), List.of(), Map.of());
    }

    /**
     * Create a configuration validation result with warnings.
     * 
     * @param warnings List of validation warnings
     * @return A configuration validation result with warnings
     */
    public static ConfigurationValidationResult withWarnings(List<String> warnings) {
        return new ConfigurationValidationResult(true, List.of(), warnings, Map.of());
    }

    /**
     * Create a configuration validation result with details.
     * 
     * @param valid Whether the configuration is valid
     * @param details Additional validation details
     * @return A configuration validation result with details
     */
    public static ConfigurationValidationResult withDetails(boolean valid, Map<String, Object> details) {
        return new ConfigurationValidationResult(valid, List.of(), List.of(), details);
    }
}
