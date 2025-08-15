package org.openhab.core.ai.tool.filter.validators;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Validator for tool filters.
 * 
 * This interface defines the contract for filter validators that can validate
 * filter configurations and expressions.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface FilterValidator {

    /**
     * Get the validator ID.
     * 
     * @return the validator ID
     */
    String getValidatorId();

    /**
     * Get the validator name.
     * 
     * @return the validator name
     */
    String getValidatorName();

    /**
     * Get the validator description.
     * 
     * @return the validator description
     */
    String getValidatorDescription();

    /**
     * Get the supported filter types.
     * 
     * @return list of supported filter types
     */
    String[] getSupportedFilterTypes();

    /**
     * Check if the validator is enabled.
     * 
     * @return true if the validator is enabled
     */
    boolean isEnabled();

    /**
     * Validate a filter configuration.
     * 
     * @param filterConfig the filter configuration to validate
     * @return validation result
     */
    FilterValidationResult validateFilter(Map<String, Object> filterConfig);

    /**
     * Validate a filter expression.
     * 
     * @param filterExpression the filter expression to validate
     * @return validation result
     */
    FilterValidationResult validateExpression(String filterExpression);

    /**
     * Get the validator configuration.
     * 
     * @return the validator configuration
     */
    Map<String, Object> getConfiguration();

    /**
     * Update the validator configuration.
     * 
     * @param configuration the new configuration
     */
    void updateConfiguration(Map<String, Object> configuration);

    /**
     * Register a custom filter type.
     * 
     * @param filterType the filter type to register
     * @param validator the validator for this filter type
     */
    void registerCustomFilterType(String filterType, FilterValidator validator);

    /**
     * Get performance metrics.
     * 
     * @return performance metrics
     */
    Map<String, Object> getPerformanceMetrics();

    /**
     * Clear validation cache.
     */
    void clearCache();

    // DefaultFilterValidator extracted to top-level org.openhab.core.ai.tool.filter.validators.DefaultFilterValidator
}
