package org.openhab.core.ai.tool.elicitation.input;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Validator for user input.
 * 
 * This interface defines the contract for input validators that can validate
 * user input against schemas and business rules.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface InputValidator {

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
     * Validate input against a schema.
     * 
     * @param input the input to validate
     * @param schema the validation schema
     * @return validation result
     */
    InputValidationResult validate(Map<String, Object> input, Map<String, Object> schema);

    /**
     * Check if the validator supports the given schema type.
     * 
     * @param schemaType the schema type to check
     * @return true if the validator supports the schema type
     */
    boolean supportsSchemaType(String schemaType);

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

    // TODO: Implement input validation logic
    // TODO: Add support for custom validation rules
    // TODO: Implement validation performance optimization
    // TODO: Add support for validation caching
}
