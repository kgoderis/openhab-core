package org.openhab.core.ai.tool.api.validation;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Service for validating tool configurations and specifications.
 * 
 * This service provides comprehensive validation capabilities for tool configurations,
 * ensuring that tools meet the required specifications and constraints before
 * being registered or executed.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ValidationService {

    /**
     * Validate a tool configuration.
     * 
     * @param configuration the tool configuration to validate
     * @return validation result
     */
    ValidationResult validateConfiguration(Map<String, Object> configuration);

    /**
     * Validate tool parameters.
     * 
     * @param parameters the parameters to validate
     * @return validation result
     */
    ValidationResult validateParameters(Map<String, Object> parameters);

    /**
     * Validate tool schema.
     * 
     * @param schema the schema to validate
     * @return validation result
     */
    ValidationResult validateSchema(Map<String, Object> schema);

    /**
     * Check if a tool meets all validation requirements.
     * 
     * @param toolId the tool ID to check
     * @return true if the tool is valid, false otherwise
     */
    boolean isToolValid(String toolId);

    /**
     * Get validation rules for a specific tool type.
     * 
     * @param toolType the tool type
     * @return validation rules
     */
    Map<String, Object> getValidationRules(String toolType);

    // TODO: Implement validation logic for tool configurations
    // TODO: Add support for custom validation rules
    // TODO: Implement validation caching for performance
    // TODO: Add validation metrics and monitoring
}
