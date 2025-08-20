package org.openhab.core.ai.tool.elicitation.input;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.validation.InputValidationResult;

/**
 * Service for eliciting input from users.
 * 
 * This interface defines the contract for input elicitation services that can
 * gather and validate user input for tool operations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface InputElicitationService {

    /**
     * Get the service ID.
     * 
     * @return the service ID
     */
    String getServiceId();

    /**
     * Get the service name.
     * 
     * @return the service name
     */
    String getServiceName();

    /**
     * Get the service description.
     * 
     * @return the service description
     */
    String getServiceDescription();

    /**
     * Elicit input from the user.
     * 
     * @param prompt the input prompt
     * @param schema the input schema
     * @return the elicited input
     */
    Map<String, Object> elicitInput(String prompt, Map<String, Object> schema);

    /**
     * Elicit input with validation.
     * 
     * @param prompt the input prompt
     * @param schema the input schema
     * @param validator the input validator
     * @return the elicited input
     */
    Map<String, Object> elicitInput(String prompt, Map<String, Object> schema, InputValidator validator);

    /**
     * Validate input against a schema.
     * 
     * @param input the input to validate
     * @param schema the validation schema
     * @return validation result
     */
    InputValidationResult validateInput(Map<String, Object> input, Map<String, Object> schema);

    /**
     * Get the service configuration.
     * 
     * @return the service configuration
     */
    Map<String, Object> getConfiguration();

    /**
     * Update the service configuration.
     * 
     * @param configuration the new configuration
     */
    void updateConfiguration(Map<String, Object> configuration);

    // TODO: Implement input elicitation logic
    // TODO: Add support for different input types
    // TODO: Implement input validation
    // TODO: Add support for input caching
}
